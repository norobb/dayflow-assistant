package com.dayflow.app.ai

import com.dayflow.app.core.util.DateResolver
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId

/** Prompt and response handling shared by every cloud model provider. */
object CloudPrompt {

    fun system(ctx: AnalysisContext): String {
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(ctx.now).atZone(zone)
        val vocabulary = if (ctx.aliases.isEmpty()) "" else
            "\nPersonal vocabulary of this user (term = meaning), use it to resolve places and names:\n" +
                ctx.aliases.entries.joinToString("\n") { "- ${it.key} = ${it.value}" }
        val contacts = if (ctx.contacts.isEmpty()) "" else
            "\nFrequent contacts: ${ctx.contacts.joinToString(", ")}"
        return """
            You are Dayflow, a personal organization assistant.
            Extract calendar events, tasks and reminders from the user's input (text, image, PDF or audio).
            Today is ${now.toLocalDate()} (${now.dayOfWeek}), current time ${"%02d:%02d".format(now.hour, now.minute)}, timezone ${zone.id}.
            Write titles in the input's language (user interface language: ${ctx.language}). Keep titles short (max 8 words).$vocabulary$contacts
            Return ONLY JSON:
            {"events":[{"title":"","date":"YYYY-MM-DD or null","time":"HH:mm or null","location":"or null"}],
             "tasks":[{"title":"","due_date":"YYYY-MM-DD or null","due_time":"HH:mm or null"}],
             "reminders":[{"title":"","date":"YYYY-MM-DD","time":"HH:mm"}],
             "summary":"1 sentence","insight":"one helpful, concrete hint or empty"}
            Resolve relative dates ("tomorrow", "next Monday") to absolute dates. Never invent dates or times that are not in the input; use null.
        """.trimIndent()
    }

    /** Parses the model's JSON (tolerates ```json fences) into a result with resolved timestamps. */
    fun parse(raw: String, type: SourceType, rawInput: String?, ctx: AnalysisContext): DayflowAnalysisResult {
        val json = JSONObject(extractJson(raw))
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(ctx.now).atZone(zone).toLocalDate()
        fun s(o: JSONObject, k: String, max: Int = 300) = GeminiProvider.sanitizeText(
            o.optString(k, "").takeUnless { it == "null" }, "", max
        )
        fun millis(date: String, time: String): Long? {
            val d = DateResolver.resolveLabel(date, today)
            val t = DateResolver.findTime(time)?.time
            if (d == null && t == null) return null
            return DateResolver.toMillis(d ?: today, t, zone)
        }
        fun label(date: String) = DateResolver.resolveLabel(date, today)
            ?.let { DateResolver.friendlyLabel(it, today, ctx.language) } ?: date

        val events = json.optJSONArray("events")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val title = s(o, "title").ifBlank { return@mapNotNull null }
                val date = s(o, "date", 40); val time = s(o, "time", 40)
                DayflowEvent(
                    title = title, time = time, dateLabel = label(date),
                    location = s(o, "location").ifBlank { null },
                    sourceType = type, startMillis = millis(date, time)
                )
            }
        }.orEmpty()
        val tasks = json.optJSONArray("tasks")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val title = s(o, "title").ifBlank { return@mapNotNull null }
                val date = s(o, "due_date", 40).ifBlank { s(o, "due", 40) }; val time = s(o, "due_time", 40)
                DayflowTask(
                    title = title,
                    dueDate = listOf(label(date), time).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { null },
                    sourceType = type, dueMillis = millis(date, time)
                )
            }
        }.orEmpty()
        val reminders = json.optJSONArray("reminders")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val title = s(o, "title").ifBlank { return@mapNotNull null }
                val date = s(o, "date", 40).ifBlank { s(o, "due", 40) }; val time = s(o, "time", 40)
                DayflowReminder(
                    title = title,
                    timeLabel = listOf(label(date), time).filter { it.isNotBlank() }.joinToString(" · "),
                    scheduledTimeMillis = millis(date, time) ?: (ctx.now + 3_600_000L),
                    sourceType = type
                )
            }
        }.orEmpty()
        val insight = s(json, "insight", 600)
        return DayflowAnalysisResult(
            sourceType = type,
            inputSnippet = rawInput.orEmpty().take(400),
            summaryText = s(json, "summary", 600),
            events = events, tasks = tasks, reminders = reminders,
            insight = if (insight.isBlank()) DayflowInsight("", "", false) else DayflowInsight("Dayflow", insight, true),
            confidence = 0.9f
        )
    }

    internal fun extractJson(raw: String): String {
        val start = raw.indexOf('{'); val end = raw.lastIndexOf('}')
        require(start >= 0 && end > start) { "No JSON object in model response" }
        return raw.substring(start, end + 1)
    }

    /** Minimal JSON POST. Returns (status, body). */
    fun post(url: String, body: JSONObject, headers: Map<String, String>, timeoutMs: Int = 180_000): Pair<Int, String> {
        // Overloaded / rate-limited providers usually recover within seconds: retry twice with backoff.
        var attempt = 0
        while (true) {
            val result = postOnce(url, body, headers, timeoutMs)
            if (result.first !in setOf(429, 503) || attempt == 2) return result
            Thread.sleep(2_000L * (attempt + 1))
            attempt++
        }
    }

    private fun postOnce(url: String, body: JSONObject, headers: Map<String, String>, timeoutMs: Int): Pair<Int, String> {
        val c = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = timeoutMs
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        try {
            OutputStreamWriter(c.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { r -> r.readText() } }.orEmpty()
            return code to text
        } finally {
            c.disconnect()
        }
    }

    fun error(type: SourceType, rawInput: String?, provider: String, message: String) = DayflowAnalysisResult(
        sourceType = type,
        inputSnippet = rawInput.orEmpty().take(400),
        summaryText = "$provider: $message",
        insight = DayflowInsight(provider, message, true),
        confidence = 0f,
        hasError = true,
        errorMessage = message
    )
}
