package com.dayflow.app.integrations.accessibility.context

import java.util.Locale

/**
 * Deterministic, on-device understanding of a captured screen.
 *
 * The accessibility tree yields plain labels ("17:30", "Platform 4",
 * "Your appointment is at 14:30 with Dr. Stein"). This parser turns those
 * labels into structured candidates so the user can add them to their day
 * with one tap — no network, no model, works offline and in all three
 * shipped languages.
 *
 * Deliberately conservative: a candidate needs a real signal (a time, a
 * date, or an action verb) before it is offered. A wrong proposal is worse
 * than no proposal because the user has to review it anyway.
 */
class ScreenContextAnalyzer {

    fun analyze(
        lines: List<String>,
        language: String,
        now: Long = System.currentTimeMillis()
    ): List<ScreenCandidate> {
        val lang = language.lowercase(Locale.ROOT).take(2)
        val segments = lines.asSequence()
            .flatMap { splitSegments(it) }
            .map(::cleanSegment)
            .filter { it.length in MIN_SEGMENT_LENGTH..MAX_SEGMENT_LENGTH }
            .distinct()
            .take(MAX_SEGMENTS)
            .toList()

        val candidates = mutableListOf<ScreenCandidate>()
        segments.forEach { segment ->
            val candidate = classify(segment, lang) ?: return@forEach
            val duplicate = candidates.any {
                it.title.equals(candidate.title, ignoreCase = true)
            }
            if (!duplicate) candidates += candidate
        }
        return candidates.sortedByDescending { it.confidence }.take(MAX_CANDIDATES)
    }

    // ------------------------------------------------------------------
    // Classification
    // ------------------------------------------------------------------

    private fun classify(segment: String, lang: String): ScreenCandidate? {
        val lower = segment.lowercase(Locale.ROOT)
        val time = extractTime(segment)
        val date = extractDate(lower)
        val hasSchedule = time != null || date != null

        val isReminder = reminderWords.matches(lower)
        val isTask = taskVerbs.matches(lower)
        val isEvent = eventNouns.matches(lower)
        val isLocation = locationNouns.matches(lower)

        // "remind me tomorrow at 9 to call the dentist" -> reminder
        if (isReminder) {
            if (!hasSchedule && !isTask) return null
            return build(segment, CandidateKind.REMINDER, time, date, lang, strong = true)
        }

        // An appointment noun plus a time/date is the clearest event signal.
        if (isEvent && hasSchedule) {
            return build(segment, CandidateKind.EVENT, time, date, lang, strong = true)
        }

        // A plain action verb is a task (even without a date).
        if (isTask) {
            return build(segment, CandidateKind.TASK, time, date, lang, strong = hasSchedule)
        }

        // Ticket / check-in style labels ("Check-in opens 06:00, Gate B12").
        if (isLocation && hasSchedule && segment.length > 12) {
            return build(segment, CandidateKind.EVENT, time, date, lang, strong = false)
        }

        return null
    }

    private fun build(
        segment: String,
        kind: CandidateKind,
        time: String?,
        date: String?,
        lang: String,
        strong: Boolean
    ): ScreenCandidate? {
        val title = cleanTitle(segment)
        if (title.length < MIN_TITLE_LENGTH) return null

        var confidence = BASE_CONFIDENCE
        if (time != null) confidence += 0.20f
        if (date != null) confidence += 0.10f
        if (strong) confidence += 0.10f
        if (kind == CandidateKind.EVENT && time != null && date != null) confidence += 0.05f
        confidence = confidence.coerceAtMost(0.95f)

        return ScreenCandidate(
            kind = kind,
            title = title,
            timeLabel = time,
            dateLabel = date,
            location = if (kind == CandidateKind.EVENT) extractLocation(segment, date) else null,
            evidence = segment.take(MAX_EVIDENCE_LENGTH),
            confidence = confidence
        )
    }

    // ------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------

    private val colonTime = Regex("(?<!\\d)([01]?\\d|2[0-3])[:.]([0-5]\\d)(?!\\d)")
    private val hourTime = Regex("(?<!\\d)([01]?\\d|2[0-3])\\s?(?:uhr|h)\\b", RegexOption.IGNORE_CASE)
    private val amPmTime = Regex("(?<!\\d)(1[0-2]|0?[1-9])(?::([0-5]\\d))?\\s?(am|pm)\\b", RegexOption.IGNORE_CASE)

    /** Normalised 24h "HH:mm", or null. */
    internal fun extractTime(text: String): String? {
        colonTime.find(text)?.let { m ->
            val hour = m.groupValues[1].toIntOrNull() ?: return null
            return "%02d:%s".format(Locale.ROOT, hour, m.groupValues[2])
        }
        amPmTime.find(text)?.let { m ->
            val raw = m.groupValues[1].toIntOrNull() ?: return null
            val minutes = m.groupValues[2].ifBlank { "00" }
            val isPm = m.groupValues[3].equals("pm", ignoreCase = true)
            val hour = when {
                isPm && raw < 12 -> raw + 12
                !isPm && raw == 12 -> 0
                else -> raw
            }
            return "%02d:%s".format(Locale.ROOT, hour, minutes)
        }
        hourTime.find(text)?.let { m ->
            val hour = m.groupValues[1].toIntOrNull() ?: return null
            return "%02d:00".format(Locale.ROOT, hour)
        }
        return null
    }

    /** The matched date word, capitalised in the source language. */
    internal fun extractDate(lower: String): String? {
        datePatterns.forEach { (pattern, canonical) ->
            if (pattern.containsMatchIn(lower)) {
                return canonical.replaceFirstChar { it.titlecase(Locale.ROOT) }
            }
        }
        return null
    }

    /** A venue after "at / in / bei / en", if it reads like a proper name. */
    private fun extractLocation(segment: String, date: String?): String? {
        val match = locationPattern.find(segment) ?: return null
        var value = match.groupValues[1].trim()
        stopWords.forEach { stop ->
            val suffix = Regex("\\s+${Regex.escape(stop)}$", RegexOption.IGNORE_CASE)
            value = suffix.replace(value, "")
        }
        value = value.trim(' ', ',', '.', '-', '–')
        if (value.length !in MIN_LOCATION_LENGTH..MAX_LOCATION_LENGTH) return null
        if (date != null && value.equals(date, ignoreCase = true)) return null
        if (dateWords.keys.any { value.equals(it, ignoreCase = true) }) return null
        return value
    }

    private val locationPattern = Regex(
        "\\b(?:at|in|bei|en|@)\\s+([\\p{Lu}][\\p{L}\\d'’.\\-]+(?:\\s+[\\p{L}\\d'’.\\-]+){0,4})"
    )

    // ------------------------------------------------------------------
    // Text hygiene
    // ------------------------------------------------------------------

    private fun splitSegments(line: String): List<String> {
        if (line.isBlank()) return emptyList()
        return line
            .replace('\n', ' ')
            .split(sentenceEnd)
            .map { it.replace(MAIN_SEPARATOR, ' ') }
            .filter { it.isNotBlank() }
    }

    private val sentenceEnd = Regex("(?<=[.!?;•])\\s+")

    private fun cleanSegment(raw: String): String = raw
        .replace(Regex("\\s+"), " ")
        .trim(' ', '\u00a0', '·', '|', '•', '-', '–', ':')
        .trim()

    private fun cleanTitle(segment: String): String {
        var title = segment.trim()
        // Drop a leading timestamp: "17:30 Platform 4" -> "Platform 4".
        title = title.replace(Regex("^\\d{1,2}[:.]\\d{2}\\s*[-–—·|]?\\s*"), "")
        // Drop UI chrome that leaks into the tree from buttons and labels.
        title = chromeTrim.replace(title, "")
        return title.trim().take(MAX_TITLE_LENGTH).trim()
    }

    private val MAIN_SEPARATOR = Regex("\\s*[·|]\\s*")
    private val chromeTrim = Regex("^(?:button|schaltfläche|botón|icon|symbol):?\\s*", RegexOption.IGNORE_CASE)

    // ------------------------------------------------------------------
    // Keyword sets (en / de / es)
    // ------------------------------------------------------------------

    private class Words(list: List<String>) {
        private val regex = Regex(
            "\\b(?:${list.joinToString("|") { Regex.escape(it) }})\\b",
            RegexOption.IGNORE_CASE
        )
        fun matches(text: String) = regex.containsMatchIn(text)
    }

    private val eventNouns = Words(
        listOf(
            "appointment", "meeting", "reservation", "booking", "check-in", "check in",
            "flight", "boarding", "gate", "train", "platform", "interview", "dentist",
            "doctor", "lunch", "dinner", "breakfast", "concert", "workshop", "webinar",
            "seminar", "class", "lecture", "viewing", "pickup",
            "termin", "besprechung", "reservierung", "buchung", "flug", "boarding",
            "zug", "gleis", "zahnarzt", "arzt", "ärztin", "mittagessen", "abendessen",
            "konzert", "seminar", "vorlesung", "besichtigung",
            "cita", "reunión", "reunion", "reserva", "vuelo", "embarque", "tren",
            "andén", "anden", "dentista", "médico", "medico", "almuerzo", "cena",
            "concierto", "clase", "visita"
        )
    )

    private val taskVerbs = Words(
        listOf(
            "bring", "pick up", "call", "pay", "submit", "send", "sign", "confirm",
            "book", "buy", "renew", "prepare", "remember", "check", "download",
            "upload", "fill", "register", "cancel", "return", "collect", "email",
            "reply", "review", "pack", "print", "transfer", "top up",
            "mitbringen", "abholen", "anrufen", "bezahlen", "einreichen", "senden",
            "unterschreiben", "bestätigen", "buchen", "kaufen", "verlängern",
            "vorbereiten", "erinnere", "erinnern", "prüfen", "herunterladen",
            "hochladen", "ausfüllen", "anmelden", "stornieren", "zurückgeben",
            "abgeben", "sammeln", "mailen", "antworten", "überprüfen", "packen",
            "drucken", "überweisen", "aufladen",
            "traer", "recoger", "llamar", "pagar", "enviar", "firmar", "confirmar",
            "reservar", "comprar", "renovar", "preparar", "recuerda", "recordar",
            "revisar", "descargar", "subir", "rellenar", "registrar", "cancelar",
            "devolver", "contestar", "imprimir", "transferir", "recargar"
        )
    )

    private val reminderWords = Words(
        listOf(
            "remind", "reminder", "remember to",
            "erinnere", "erinnerung", "denk daran",
            "recordatorio", "recuérdame", "recuerdame", "acuérdate", "acuerdate"
        )
    )

    private val locationNouns = Words(
        listOf(
            "platform", "gate", "terminal", "station", "airport", "address",
            "gleis", "bahnhof", "flughafen", "adresse", "haltestelle",
            "andén", "anden", "estación", "estacion", "aeropuerto", "dirección"
        )
    )

    private val dateWords: Map<String, String> = linkedMapOf(
        "day after tomorrow" to "day after tomorrow",
        "übermorgen" to "übermorgen",
        "pasado mañana" to "pasado mañana",
        "tomorrow" to "tomorrow",
        "morgen" to "morgen",
        "mañana" to "mañana",
        "manana" to "mañana",
        "tonight" to "tonight",
        "heute abend" to "heute abend",
        "esta noche" to "esta noche",
        "today" to "today",
        "heute" to "heute",
        "hoy" to "hoy",
        "monday" to "Monday", "tuesday" to "Tuesday", "wednesday" to "Wednesday",
        "thursday" to "Thursday", "friday" to "Friday", "saturday" to "Saturday",
        "sunday" to "Sunday",
        "montag" to "Montag", "dienstag" to "Dienstag", "mittwoch" to "Mittwoch",
        "donnerstag" to "Donnerstag", "freitag" to "Freitag", "samstag" to "Samstag",
        "sonntag" to "Sonntag",
        "lunes" to "lunes", "martes" to "martes", "miércoles" to "miércoles",
        "miercoles" to "miercoles", "jueves" to "jueves", "viernes" to "viernes",
        "sábado" to "sábado", "sabado" to "sabado", "domingo" to "domingo"
    )

    private val datePatterns: List<Pair<Regex, String>> = dateWords.map { (word, canonical) ->
        Regex("(?<![\\p{L}])${Regex.escape(word)}(?![\\p{L}])", RegexOption.IGNORE_CASE) to canonical
    }

    private val stopWords = listOf(
        "and", "und", "y", "with", "mit", "con", "please", "bitte", "por favor",
        "at", "um", "a", "today", "heute", "hoy", "tomorrow", "morgen", "mañana"
    )

    companion object {
        const val MIN_SEGMENT_LENGTH = 8
        const val MAX_SEGMENT_LENGTH = 240
        const val MAX_SEGMENTS = 400
        const val MAX_CANDIDATES = 8
        const val MAX_TITLE_LENGTH = 90
        const val MAX_EVIDENCE_LENGTH = 160
        const val MIN_TITLE_LENGTH = 5
        const val MIN_LOCATION_LENGTH = 3
        const val MAX_LOCATION_LENGTH = 42
        const val BASE_CONFIDENCE = 0.55f
    }
}
