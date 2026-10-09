package com.dayflow.app.ai

import android.util.Base64
import com.dayflow.app.BuildConfig
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class GeminiProvider : AIProvider {
    override val name: String = "Gemini Multi-Modal API"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String
    ): DayflowAnalysisResult = withContext(Dispatchers.IO) {
        val effectiveKey = apiKey.ifBlank {
            try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: "" } catch (e: Exception) { "" }
        }

        if (effectiveKey.isBlank()) {
            return@withContext DayflowAnalysisResult(
                sourceType = type,
                inputSnippet = rawInput ?: "",
                summaryText = "Gemini API key is not configured.",
                events = emptyList(),
                tasks = emptyList(),
                reminders = emptyList(),
                insight = DayflowInsight(
                    title = "API Key Required",
                    text = "Please enter your Gemini API Key in Settings to enable real AI organization.",
                    visible = true
                ),
                confidence = 0f,
                hasError = true,
                errorMessage = "Gemini API key missing. Please configure it in Settings."
            )
        }

        val targetModel = sanitizeModelName(modelOverride)

        try {
            val systemInstruction = """
                You are Dayflow AI, an intelligent personal organization assistant.
                Analyze the user input (text, image, PDF, audio) and extract events, tasks, and reminders.
                Return ONLY valid JSON with this structure:
                {
                  "events": [
                    {
                      "title": "String",
                      "time": "String (e.g. 14:00 - 15:30)",
                      "date": "String (e.g. Today, Tomorrow, YYYY-MM-DD)",
                      "location": "String or null"
                    }
                  ],
                  "tasks": [
                    {
                      "title": "String",
                      "due": "String or null",
                      "completed": false
                    }
                  ],
                  "reminders": [
                    {
                      "title": "String",
                      "due": "String"
                    }
                  ],
                  "summary": "Short 1-2 sentence overview of extracted actions.",
                  "insight": "Actionable advice or pattern highlight if applicable."
                }
                NEVER fabricate dates/times if uncertain. Do not output markdown code blocks outside JSON.
            """.trimIndent()

            val requestBody = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPart = JSONObject().put("text", systemInstruction)
            sysInstructionObj.put("parts", JSONArray().put(sysPart))
            requestBody.put("systemInstruction", sysInstructionObj)

            // User parts
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            if (!rawInput.isNullOrBlank()) {
                partsArray.put(JSONObject().put("text", "Input text / description: $rawInput"))
            }

            if (mediaBytes != null && mediaBytes.isNotEmpty()) {
                val effectiveMime = mimeType ?: when (type) {
                    SourceType.SCREENSHOT -> "image/png"
                    SourceType.PDF -> "application/pdf"
                    SourceType.VOICE -> "audio/mp3"
                    else -> "application/octet-stream"
                }
                val base64Data = Base64.encodeToString(mediaBytes, Base64.NO_WRAP)
                val inlineData = JSONObject()
                    .put("mimeType", effectiveMime)
                    .put("data", base64Data)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            if (partsArray.length() == 0) {
                partsArray.put(JSONObject().put("text", "Organize my day"))
            }

            contentObj.put("role", "user")
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBody.put("contents", contentsArray)

            // Generation Config
            val genConfig = JSONObject()
                .put("temperature", 0.2)
                .put("responseMimeType", "application/json")
            requestBody.put("generationConfig", genConfig)

            val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$effectiveKey"
            val url = URL(endpointUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 20000

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            if (statusCode != 200) {
                val errorStream = connection.errorStream
                val errorMsg = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else "HTTP error $statusCode"

                return@withContext DayflowAnalysisResult(
                    sourceType = type,
                    inputSnippet = rawInput ?: "",
                    summaryText = "Gemini API request failed.",
                    events = emptyList(),
                    tasks = emptyList(),
                    reminders = emptyList(),
                    insight = DayflowInsight(
                        title = "Gemini Connection Error",
                        text = sanitizeError("Received HTTP $statusCode from Gemini API. Check your model ($targetModel) and key configuration.", effectiveKey),
                        visible = true
                    ),
                    confidence = 0f,
                    hasError = true,
                    errorMessage = sanitizeError("Gemini API error ($statusCode): $errorMsg", effectiveKey)
                )
            }

            val responseText = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val responseJson = JSONObject(responseText)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext DayflowAnalysisResult(
                    sourceType = type,
                    inputSnippet = rawInput ?: "",
                    summaryText = "No structured items detected.",
                    events = emptyList(),
                    tasks = emptyList(),
                    reminders = emptyList(),
                    insight = DayflowInsight("No Response", "Gemini returned an empty result.", true),
                    confidence = 0f
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text") ?: "{}"

            val parsedJson = JSONObject(textOutput)

            val eventsList = mutableListOf<DayflowEvent>()
            val tasksList = mutableListOf<DayflowTask>()
            val remindersList = mutableListOf<DayflowReminder>()

            parsedJson.optJSONArray("events")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    eventsList.add(
                        DayflowEvent(
                            id = UUID.randomUUID().toString(),
                            time = obj.optString("time", "12:00"),
                            title = obj.optString("title", "Event"),
                            location = obj.optString("location", "").ifBlank { null },
                            dateLabel = obj.optString("date", "Today"),
                            sourceType = type
                        )
                    )
                }
            }

            parsedJson.optJSONArray("tasks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    tasksList.add(
                        DayflowTask(
                            id = UUID.randomUUID().toString(),
                            title = obj.optString("title", "Task"),
                            completed = obj.optBoolean("completed", false),
                            dueDate = obj.optString("due", "").ifBlank { null },
                            sourceType = type
                        )
                    )
                }
            }

            parsedJson.optJSONArray("reminders")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    remindersList.add(
                        DayflowReminder(
                            id = UUID.randomUUID().toString(),
                            title = obj.optString("title", "Reminder"),
                            timeLabel = obj.optString("due", "18:00"),
                            sourceType = type
                        )
                    )
                }
            }

            val summaryText = parsedJson.optString("summary", "Extracted content from input.")
            val insightText = parsedJson.optString("insight", "")

            val insight = if (insightText.isNotBlank()) {
                DayflowInsight(
                    title = "AI Insight",
                    text = insightText,
                    visible = true
                )
            } else {
                DayflowInsight(title = "", text = "", visible = false)
            }

            DayflowAnalysisResult(
                sourceType = type,
                inputSnippet = rawInput ?: "",
                summaryText = summaryText,
                events = eventsList,
                tasks = tasksList,
                reminders = remindersList,
                insight = insight,
                confidence = 0.95f,
                hasError = false
            )
        } catch (e: Exception) {
            DayflowAnalysisResult(
                sourceType = type,
                inputSnippet = rawInput ?: "",
                summaryText = "Failed to parse content.",
                events = emptyList(),
                tasks = emptyList(),
                reminders = emptyList(),
                insight = DayflowInsight(
                    title = "Analysis Exception",
                    text = sanitizeError("An exception occurred while processing Gemini request: ${e.localizedMessage}", effectiveKey),
                    visible = true
                ),
                confidence = 0f,
                hasError = true,
                errorMessage = sanitizeError(e.localizedMessage, effectiveKey)
            )
        }
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Validate and sanitize model identifier format to prevent
         * URL path traversal or query parameter injection attacks. Defaults safely to gemini-3.6-flash.
         */
        fun sanitizeModelName(rawModel: String?): String {
            if (rawModel.isNullOrBlank()) return "gemini-3.6-flash"
            val trimmed = rawModel.trim()
            return if (trimmed.matches(Regex("^[a-zA-Z0-9._-]+$"))) trimmed else "gemini-3.6-flash"
        }

        /**
         * SECURITY ENHANCEMENT: Redact API keys and URL key query params from error messages
         * to prevent exposing credentials in UI cards or logs.
         */
        fun sanitizeError(message: String?, apiKey: String): String {
            if (message.isNullOrBlank()) return "Unknown error"
            var sanitized = message
            if (apiKey.isNotBlank()) {
                sanitized = sanitized.replace(apiKey, "[REDACTED]")
            }
            return sanitized.replace(Regex("key=[a-zA-Z0-9._-]+"), "key=[REDACTED]")
        }
    }
}
