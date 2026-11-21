package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.integrations.accessibility.context.CandidateKind
import com.dayflow.app.integrations.accessibility.context.ScreenContextAnalyzer
import java.util.Locale

/**
 * On-device understanding: rule-based extraction of events, tasks and
 * reminders from text (messages, OCR'd screenshots and PDFs, voice
 * transcripts) in English, German and Spanish. Nothing leaves the phone.
 */
class LocalProvider(
    private val parser: ScreenContextAnalyzer = ScreenContextAnalyzer()
) : AIProvider {
    override val name: String = "Dayflow On-Device"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String,
        context: AnalysisContext
    ): DayflowAnalysisResult {
        val text = sanitizeInput(rawInput)
        val lang = context.language.take(2).lowercase(Locale.ROOT)
        if (text.isBlank()) {
            return DayflowAnalysisResult(
                sourceType = type,
                summaryText = L.nothing(lang),
                confidence = 0f
            )
        }

        val candidates = parser.analyze(text.lines(), lang, context.now)
        val events = mutableListOf<DayflowEvent>()
        val tasks = mutableListOf<DayflowTask>()
        val reminders = mutableListOf<DayflowReminder>()

        candidates.forEach { c ->
            when (c.kind) {
                CandidateKind.EVENT -> events += DayflowEvent(
                    time = c.timeLabel.orEmpty(),
                    title = c.title,
                    location = resolveAlias(c.location, c.evidence, context.aliases),
                    dateLabel = c.dateLabel.orEmpty(),
                    sourceType = type,
                    startMillis = c.startMillis
                )
                CandidateKind.TASK -> tasks += DayflowTask(
                    title = c.title,
                    dueDate = c.scheduleLabel.ifBlank { null },
                    sourceType = type,
                    dueMillis = c.startMillis
                )
                CandidateKind.REMINDER -> {
                    val label = c.scheduleLabel.ifBlank { L.today(lang) }
                    reminders += DayflowReminder(
                        title = c.title,
                        timeLabel = if (c.timeLabel == null) "$label · 09:00" else label,
                        scheduledTimeMillis = c.startMillis ?: (context.now + 3_600_000L),
                        sourceType = type
                    )
                    // "Remind me Monday to submit X" is also something to do.
                    tasks += DayflowTask(
                        title = c.title,
                        dueDate = c.dateLabel,
                        sourceType = type,
                        dueMillis = c.startMillis
                    )
                }
            }
        }

        val total = events.size + tasks.size + reminders.size
        return DayflowAnalysisResult(
            sourceType = type,
            inputSnippet = text.take(400),
            summaryText = if (total == 0) L.nothing(lang) else L.summary(lang, events.size, tasks.size, reminders.size),
            events = events,
            tasks = tasks,
            reminders = reminders,
            insight = if (total == 0) DayflowInsight("", "", false)
            else DayflowInsight(L.insightTitle(lang), L.insightText(lang), true),
            confidence = if (candidates.isEmpty()) 0f else candidates.map { it.confidence }.average().toFloat()
        )
    }

    /** Applies personal vocabulary: "im Büro" -> the address the user saved for "Büro". */
    private fun resolveAlias(location: String?, evidence: String, aliases: Map<String, String>): String? {
        if (aliases.isEmpty()) return location
        aliases.forEach { (term, value) ->
            if (location != null && location.equals(term, ignoreCase = true)) return value
        }
        if (location == null) {
            aliases.forEach { (term, value) ->
                if (Regex("(?<![\\p{L}])${Regex.escape(term)}(?![\\p{L}])", RegexOption.IGNORE_CASE).containsMatchIn(evidence)) {
                    return value
                }
            }
        }
        return location
    }

    private object L {
        fun nothing(l: String) = when (l) {
            "de" -> "Keine Termine, Aufgaben oder Erinnerungen erkannt."
            "es" -> "No se detectaron citas, tareas ni recordatorios."
            else -> "No events, tasks or reminders recognized."
        }
        fun today(l: String) = when (l) { "de" -> "Heute"; "es" -> "Hoy"; else -> "Today" }
        fun summary(l: String, e: Int, t: Int, r: Int): String {
            val parts = when (l) {
                "de" -> listOf(e to ("Termin" to "Termine"), t to ("Aufgabe" to "Aufgaben"), r to ("Erinnerung" to "Erinnerungen"))
                "es" -> listOf(e to ("cita" to "citas"), t to ("tarea" to "tareas"), r to ("recordatorio" to "recordatorios"))
                else -> listOf(e to ("event" to "events"), t to ("task" to "tasks"), r to ("reminder" to "reminders"))
            }.filter { it.first > 0 }.joinToString(", ") { (n, w) -> "$n ${if (n == 1) w.first else w.second}" }
            return when (l) { "de" -> "Erkannt: $parts."; "es" -> "Detectado: $parts."; else -> "Recognized: $parts." }
        }
        fun insightTitle(l: String) = when (l) { "de" -> "Auf dem Gerät verstanden"; "es" -> "Entendido en el dispositivo"; else -> "Understood on device" }
        fun insightText(l: String) = when (l) {
            "de" -> "Analysiert ohne Cloud – nichts hat dein Telefon verlassen. Bitte prüfe die Angaben vor dem Speichern."
            "es" -> "Analizado sin la nube: nada salió de tu teléfono. Revisa los datos antes de guardar."
            else -> "Analyzed without the cloud — nothing left your phone. Please review before saving."
        }
    }

    companion object {
        /** Strips control characters (keeps newlines) and caps length. */
        fun sanitizeInput(rawInput: String?, maxLength: Int = 20_000): String {
            if (rawInput.isNullOrBlank()) return ""
            return rawInput
                .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
        }
    }
}
