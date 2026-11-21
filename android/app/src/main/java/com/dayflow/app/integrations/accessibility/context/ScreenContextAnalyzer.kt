package com.dayflow.app.integrations.accessibility.context

import com.dayflow.app.core.util.DateResolver
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
        val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val segments = lines.asSequence()
            .flatMap { splitSegments(it) }
            .map(::cleanSegment)
            .filter { it.length in MIN_SEGMENT_LENGTH..MAX_SEGMENT_LENGTH }
            .distinct()
            .take(MAX_SEGMENTS)
            .toList()

        val candidates = mutableListOf<ScreenCandidate>()
        var lastSchedule: Schedule? = null
        segments.forEach { segment ->
            var candidate = classify(segment, lang, today) ?: return@forEach
            // "…tomorrow at 17:30. And bring the documents." -> the task is due then too.
            if (candidate.kind == CandidateKind.TASK && candidate.startMillis == null && lastSchedule != null) {
                val s = lastSchedule!!
                candidate = candidate.copy(dateLabel = s.dateLabel, timeLabel = s.timeLabel, startMillis = s.millis)
            }
            if (candidate.startMillis != null) {
                lastSchedule = Schedule(candidate.dateLabel, candidate.timeLabel, candidate.startMillis)
            }
            // Screen readers repeat a message with a prefix ("Max said …"): same schedule + contained title = same item.
            val duplicate = candidates.indexOfFirst {
                it.title.equals(candidate.title, ignoreCase = true) ||
                    (it.startMillis == candidate.startMillis && it.kind == candidate.kind &&
                        (it.title.contains(candidate.title, true) || candidate.title.contains(it.title, true)))
            }
            if (duplicate < 0) candidates += candidate
            else if (candidate.title.length < candidates[duplicate].title.length) candidates[duplicate] = candidate
        }
        if (candidates.none { it.kind == CandidateKind.EVENT }) {
            documentEvent(lines, lang, today)?.let { doc ->
                if (candidates.none { it.title.equals(doc.title, ignoreCase = true) }) candidates.add(0, doc)
            }
        }
        // Keep the reading order of the source; drop the weakest beyond the cap.
        val kept = candidates.sortedByDescending { it.confidence }.take(MAX_CANDIDATES).toSet()
        return candidates.filter { it in kept }
    }

    private data class Schedule(val dateLabel: String?, val timeLabel: String?, val millis: Long?)

    /**
     * Screens, screenshots and PDFs spread one appointment over several lines
     * ("Dentist Appointment" / "Tuesday • 14:30" / "Dental Care Center").
     * When no single line is an event, look at the block as a whole.
     */
    private fun documentEvent(lines: List<String>, lang: String, today: LocalDate): ScreenCandidate? {
        val clean = lines.flatMap { it.split('\n') }.map(::cleanSegment).filter { it.length >= 3 }
        if (clean.isEmpty()) return null
        val titleLine = clean.firstOrNull { eventNouns.matches(it.lowercase(Locale.ROOT)) && it.length <= MAX_TITLE_LENGTH }
            ?: return null
        val title = cleanTitle(titleLine, DateResolver.findDate(titleLine, today), DateResolver.findTime(titleLine))
        if (title.length < MIN_TITLE_LENGTH) return null
        // Read the schedule and venue from the title onwards: a status-bar clock or
        // header above the appointment ("9:41") is not its time.
        val titleIndex = clean.indexOf(titleLine)
        val ordered = clean.drop(titleIndex) + clean.take(titleIndex)
        val whole = ordered.joinToString(" \n ")
        val date = DateResolver.findDate(whole, today, dayFirst = lang != "en")
        val time = DateResolver.findTime(whole, date?.range)
        if (date == null && time == null) return null
        val location = ordered.firstOrNull { line ->
            venueWords.matches(line.lowercase(Locale.ROOT)) && line.length <= MAX_LOCATION_LENGTH + 20 &&
                DateResolver.findTime(line) == null
        }?.let { line ->
            line.replace(Regex("\\s*[•·|]\\s*"), ", ").trim(' ', ',')
        } ?: extractLocation(whole.replace(" \n ", ". "), null)
        val millis = DateResolver.toMillis(date?.date ?: today, time?.time)
        return ScreenCandidate(
            kind = CandidateKind.EVENT,
            title = title,
            timeLabel = time?.label,
            dateLabel = date?.let { DateResolver.friendlyLabel(it.date, today, lang) },
            location = location,
            evidence = clean.take(4).joinToString(" · ").take(MAX_EVIDENCE_LENGTH),
            confidence = 0.75f + (if (date != null && time != null) 0.1f else 0f),
            startMillis = millis
        )
    }

    // ------------------------------------------------------------------
    // Classification
    // ------------------------------------------------------------------

    private fun classify(segment: String, lang: String, today: LocalDate): ScreenCandidate? {
        val lower = segment.lowercase(Locale.ROOT)
        if (uiChrome.matches(lower.trim())) return null
        val date = DateResolver.findDate(segment, today, dayFirst = lang != "en")
        val time = DateResolver.findTime(segment, date?.range)
        val hasSchedule = time != null || date != null

        val isReminder = reminderWords.matches(lower)
        val isTask = taskVerbs.matches(lower)
        val isEvent = eventNouns.matches(lower)
        val isLocation = locationNouns.matches(lower)

        // "remind me tomorrow at 9 to call the dentist" -> reminder
        if (isReminder) {
            if (!hasSchedule && !isTask) return null
            return build(segment, CandidateKind.REMINDER, time, date, lang, today, strong = true)
        }

        // An appointment noun plus a time/date is the clearest event signal.
        if ((isEvent || isLocation) && hasSchedule && time != null) {
            return build(segment, CandidateKind.EVENT, time, date, lang, today, strong = isEvent)
        }
        if (isEvent && hasSchedule) {
            return build(segment, CandidateKind.EVENT, time, date, lang, today, strong = true)
        }

        // A plain action verb is a task (even without a date).
        if (isTask) {
            return build(segment, CandidateKind.TASK, time, date, lang, today, strong = hasSchedule)
        }

        // "Meet me at 8 pm tomorrow" — a concrete day *and* time is an appointment.
        if (date != null && time != null && segment.length > 12) {
            return build(segment, CandidateKind.EVENT, time, date, lang, today, strong = false)
        }

        return null
    }

    private fun build(
        segment: String,
        kind: CandidateKind,
        time: DateResolver.TimeMatch?,
        date: DateResolver.DateMatch?,
        lang: String,
        today: LocalDate,
        strong: Boolean
    ): ScreenCandidate? {
        val title = cleanTitle(segment, date, time)
        if (title.length < MIN_TITLE_LENGTH) return null

        var confidence = BASE_CONFIDENCE
        if (time != null) confidence += 0.20f
        if (date != null) confidence += 0.10f
        if (strong) confidence += 0.10f
        if (kind == CandidateKind.EVENT && time != null && date != null) confidence += 0.05f
        confidence = confidence.coerceAtMost(0.95f)

        val millis = if (date != null || time != null) {
            var m = DateResolver.toMillis(date?.date ?: today, time?.time)
            // A bare time that already passed refers to tomorrow.
            if (date == null && m <= System.currentTimeMillis()) m = DateResolver.toMillis(today.plusDays(1), time?.time)
            m
        } else null
        val dateLabel = when {
            date != null -> DateResolver.friendlyLabel(date.date, today, lang)
            millis != null -> DateResolver.friendlyLabel(
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate(), today, lang
            )
            else -> null
        }

        return ScreenCandidate(
            kind = kind,
            title = title,
            timeLabel = time?.label,
            dateLabel = dateLabel,
            location = if (kind == CandidateKind.EVENT) extractLocation(segment, date?.text) else null,
            evidence = segment.take(MAX_EVIDENCE_LENGTH),
            confidence = confidence,
            startMillis = millis
        )
    }

    // ------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------

    /** A venue: a proper name after "at / in / bei / en", or "<words> station/Bahnhof". */
    private fun extractLocation(segment: String, dateText: String?): String? {
        // Try every "at/in/bei …" phrase ("am Dienstag" is a date, not a place), then venue nouns.
        val candidates = locationPattern.findAll(segment).map { it.groupValues[1] } +
            venueNounPattern.findAll(segment).map { it.value }
        return candidates.mapNotNull { cleanLocation(it, dateText) }.firstOrNull()
    }

    private fun cleanLocation(raw: String, dateText: String?): String? {
        var value = raw.trim()
        stopWords.forEach { stop ->
            val suffix = Regex("\\s+${Regex.escape(stop)}$", RegexOption.IGNORE_CASE)
            value = suffix.replace(value, "")
        }
        value = value.replace(Regex("^(?:the|der|die|das|dem|den|la|el|los|las)\\s+", RegexOption.IGNORE_CASE), "")
        value = value.trim(' ', ',', '.', '-', '–', '?', '!')
        if (value.length !in MIN_LOCATION_LENGTH..MAX_LOCATION_LENGTH) return null
        if (dateText != null && dateText.contains(value, ignoreCase = true)) return null
        if (DateResolver.findDate(value) != null && value.split(' ').size <= 2) return null
        return value.replaceFirstChar { it.titlecase(Locale.ROOT) }
    }

    private val locationPattern = Regex(
        "(?<![\\p{L}])(?:at|in|bei|en|@|im|in der|am)\\s+(?:the\\s+)?([\\p{Lu}][\\p{L}\\d'’.\\-]+(?:\\s+(?:[\\p{Lu}\\d][\\p{L}\\d'’.\\-]*|de|del|la|von|an der)){0,4})"
    )

    /** "train station", "Hauptbahnhof", "Terminal 2", "estación central". */
    private val venueNounPattern = Regex(
        "(?:[\\p{L}]+\\s+)?(?:station|bahnhof|hauptbahnhof|airport|flughafen|terminal\\s*\\d*|gleis\\s*\\d+|platform\\s*\\d+|" +
            "estación|estacion|aeropuerto|office|büro|praxis|clinic|klinik|school|schule|escuela)(?:\\s+(?:central|centro|\\d+[a-z]?))?",
        RegexOption.IGNORE_CASE
    )

    private val venueWords = Words(
        listOf(
            "street", "st.", "road", "avenue", "center", "centre", "room", "clinic", "hospital", "hotel", "station",
            "airport", "terminal", "praxis", "straße", "strasse", "str.", "platz", "weg", "raum", "zentrum", "klinik",
            "bahnhof", "flughafen", "calle", "avenida", "plaza", "sala", "centro", "clínica", "estación", "aeropuerto"
        )
    )

    // ------------------------------------------------------------------
    // Text hygiene
    // ------------------------------------------------------------------

    private fun splitSegments(line: String): List<String> {
        if (line.isBlank()) return emptyList()
        return line
            .replace('\n', ' ')
            .split(sentenceEnd)
            .map { it.replace(MAIN_SEPARATOR, " ") }
            .filter { it.isNotBlank() }
    }

    // Sentence end, but not after titles/abbreviations ("Dr. Stein", "Hauptstr. 5", "z.B.").
    private val sentenceEnd = Regex(
        "(?<=[.!?;•])(?<!(?<![\\p{L}])(?:Dr|Prof|St|Nr|Str|Mr|Mrs|Ms|Sr|Sra|Hr|Fr|Tel|ca|bzw|vs|Dra)\\.)(?<!(?<![\\p{L}])z\\.B\\.)" +
            // "12. Juni" / "3. März" is a date, not a sentence end.
            "(?<!\\d\\.)\\s+|(?<=\\d\\.)\\s+(?!(?:jan|feb|mär|mar|apr|mai|jun|jul|aug|sep|okt|nov|dez)\\p{L}*)",
        RegexOption.IGNORE_CASE
    )

    private fun cleanSegment(raw: String): String = raw
        .replace(Regex("\\s+"), " ")
        .trim(' ', '\u00a0', '·', '|', '•', '-', '–', ':')
        .trim()

    private fun cleanTitle(
        segment: String,
        date: DateResolver.DateMatch? = null,
        time: DateResolver.TimeMatch? = null
    ): String {
        var title = segment.trim()
        // Cut the schedule out of the title (it is shown separately), together
        // with the preposition that introduced it ("at 17:30", "um 9 Uhr", "el lunes").
        val ranges = listOfNotNull(date?.range, time?.range).sortedByDescending { it.first }
        ranges.forEach { r ->
            if (r.last >= title.length) return@forEach
            val before = title.substring(0, r.first)
            val glue = danglingPreposition.find(before)
            val start = glue?.range?.first ?: r.first
            title = title.removeRange(start, r.last + 1)
        }
        // Drop a leading timestamp: "17:30 Platform 4" -> "Platform 4".
        title = title.replace(Regex("^\\d{1,2}[:.]\\d{2}\\s*[-–—·|]?\\s*"), "")
        // Drop UI chrome that leaks into the tree from buttons and labels.
        title = chromeTrim.replace(title, "")
        title = title.replace(Regex("\\s+"), " ").trim().trimStart('¿', '¡')
        repeat(3) { title = fillerPrefix.replace(title.trim(), "").trimStart('¿', '¡') }
        title = politeWords.replace(title, " ")
        title = title.replace(Regex("\\s+"), " ").trim(' ', ',', '.', ';', ':', '?', '!', '-', '–')
        title = title.replaceFirstChar { it.titlecase(Locale.ROOT) }
        return title.take(MAX_TITLE_LENGTH).trim()
    }

    /** Button and chip labels of other apps ("Suggested reply", "Book now") are not tasks. */
    private val uiChrome = Regex(
        "(?:suggested|vorgeschlagene?|sugerencias?)\\s+\\p{L}+|" +
            "(?:reply|reply all|send|call|call back|share|copy|confirm|cancel|check in|book now|buy now|pay now|" +
            "download|upload|register|sign in|sign up|print|mark as read|antworten|allen antworten|senden|anrufen|" +
            "zurückrufen|teilen|kopieren|bestätigen|abbrechen|jetzt buchen|jetzt kaufen|bezahlen|herunterladen|" +
            "drucken|als gelesen markieren|responder|enviar|llamar|compartir|copiar|confirmar|cancelar|" +
            "reservar ahora|comprar ahora|pagar|descargar|imprimir)"
    )

    private val danglingPreposition = Regex(
        "(?<![\\p{L}])(?:at|on|by|until|um|am|bis|ab|a las|el|hasta|para el)\\s*$",
        RegexOption.IGNORE_CASE
    )

    private val fillerPrefix = Regex(
        "^(?:hey|hi|hello|hallo|hola|servus|moin|ok|okay|so)[,!]?\\s+|" +
            "^(?:and|und|y|also|then|dann)\\s+|" +
            "^(?:can you|could you|would you|will you|can we|could we|shall we|should we|kannst du|könntest du|kannst du mir|würdest du|" +
            "können wir|wollen wir|sollen wir|puedes|podrías|podrias|podemos|quieres)\\s+|" +
            "^(?:please|bitte|por favor)\\s+|" +
            "^(?:remind me(?: to)?|don't forget to|dont forget to|remember to|erinnere mich(?: daran)?(?:,)?(?: dass)?(?: ich)?|" +
            "denk daran(?:,)?|recuérdame(?: que)?|recuerdame(?: que)?|no olvides)\\s+",
        RegexOption.IGNORE_CASE
    )
    private val politeWords = Regex("(?<![\\p{L}])(?:please|bitte|por favor)(?![\\p{L}])", RegexOption.IGNORE_CASE)

    private val MAIN_SEPARATOR = Regex("\\s*[·|]\\s*")
    private val chromeTrim = Regex("^(?:button|schaltfläche|botón|icon|symbol):?\\s*", RegexOption.IGNORE_CASE)

    // ------------------------------------------------------------------
    // Keyword sets (en / de / es)
    // ------------------------------------------------------------------

    /** Whole-word keyword match (Unicode-aware). [compound] also matches word endings: "Zahnarzt|termin". */
    private class Words(list: List<String>, compound: List<String> = emptyList()) {
        private val regex = Regex(
            "(?<![\\p{L}])(?:${list.joinToString("|") { Regex.escape(it) }})(?![\\p{L}])" +
                (if (compound.isEmpty()) "" else "|(?:${compound.joinToString("|") { Regex.escape(it) }})(?![\\p{L}])"),
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
        ),
        compound = listOf(
            "termin", "besprechung", "reservierung", "buchung", "flug", "arzt", "ärztin", "konzert",
            "seminar", "vorlesung", "besichtigung", "treffen", "sitzung", "meeting", "kurs", "training",
            "probe", "party", "feier", "geburtstag", "essen", "abholung", "untersuchung", "sprechstunde"
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
            "drucken", "überweisen", "aufladen", "bringen", "bring", "mitnehmen", "nimm", "zahlen", "schicken",
            "schick", "besorgen", "erledigen", "kündigen", "einkaufen", "abholen", "zurückrufen", "ruf",
            "melden", "vereinbaren", "beantragen", "mitbringen", "unterschreiben", "denk dran", "nicht vergessen",
            "traer", "recoger", "llamar", "pagar", "enviar", "firmar", "confirmar",
            "reservar", "comprar", "renovar", "preparar", "recuerda", "recordar",
            "revisar", "descargar", "subir", "rellenar", "registrar", "cancelar",
            "devolver", "contestar", "imprimir", "transferir", "recargar", "trae", "lleva", "llevar",
            "compra", "paga", "envía", "envia", "llama", "no olvides", "entregar", "entrega"
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
