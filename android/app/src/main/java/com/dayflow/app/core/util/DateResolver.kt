package com.dayflow.app.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Finds and resolves date and time expressions in English, German and Spanish
 * text ("tomorrow", "nächsten Montag", "12 de junio", "2026-06-12", "17:30",
 * "5 pm", "um 9 Uhr"). Pure JVM code, unit-tested.
 */
object DateResolver {

    data class DateMatch(val date: LocalDate, val range: IntRange, val text: String)
    data class TimeMatch(val time: LocalTime, val range: IntRange, val text: String) {
        val label: String get() = "%02d:%02d".format(Locale.ROOT, time.hour, time.minute)
    }

    // ------------------------------------------------------------------
    // Dates
    // ------------------------------------------------------------------

    private val months: Map<String, Int> = buildMap {
        val en = listOf("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")
        val de = listOf("januar", "februar", "märz", "april", "mai", "juni", "juli", "august", "september", "oktober", "november", "dezember")
        val es = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
        listOf(en, de, es).forEach { names -> names.forEachIndexed { i, n -> put(n, i + 1) } }
        // Common abbreviations.
        listOf("jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "sept", "oct", "nov", "dec").forEach { a ->
            put(a, listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
                .indexOf(a.take(3)) + 1)
        }
        put("mär", 3); put("mrz", 3); put("okt", 10); put("dez", 12); put("ene", 1); put("dic", 12); put("ago", 8); put("abr", 4)
        put("marz", 3); put("jänner", 1)
    }
    private val monthAlt = months.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }

    private val weekdays: Map<String, DayOfWeek> = mapOf(
        "monday" to DayOfWeek.MONDAY, "tuesday" to DayOfWeek.TUESDAY, "wednesday" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY, "friday" to DayOfWeek.FRIDAY, "saturday" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY,
        "montag" to DayOfWeek.MONDAY, "dienstag" to DayOfWeek.TUESDAY, "mittwoch" to DayOfWeek.WEDNESDAY,
        "donnerstag" to DayOfWeek.THURSDAY, "freitag" to DayOfWeek.FRIDAY, "samstag" to DayOfWeek.SATURDAY,
        "sonntag" to DayOfWeek.SUNDAY,
        "lunes" to DayOfWeek.MONDAY, "martes" to DayOfWeek.TUESDAY, "miércoles" to DayOfWeek.WEDNESDAY,
        "miercoles" to DayOfWeek.WEDNESDAY, "jueves" to DayOfWeek.THURSDAY, "viernes" to DayOfWeek.FRIDAY,
        "sábado" to DayOfWeek.SATURDAY, "sabado" to DayOfWeek.SATURDAY, "domingo" to DayOfWeek.SUNDAY
    )
    private val weekdayAlt = weekdays.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }

    private const val L = "(?<![\\p{L}\\d])"
    private const val R = "(?![\\p{L}])"
    private val opts = setOf(RegexOption.IGNORE_CASE)

    private val isoDate = Regex("$L(\\d{4})-(\\d{1,2})-(\\d{1,2})(?!\\d)")
    private val dottedDate = Regex("$L(\\d{1,2})\\.\\s?(\\d{1,2})\\.(\\d{4}|\\d{2})?(?!\\d)")
    private val slashDate = Regex("$L(\\d{1,2})/(\\d{1,2})(?:/(\\d{4}|\\d{2}))?(?!\\d)")
    private val dayMonth = Regex("$L(\\d{1,2})(?:\\.|st|nd|rd|th)?\\s+(?:de\\s+)?($monthAlt)\\.?(?:\\s+(?:de\\s+)?(\\d{4}))?$R", opts)
    private val monthDay = Regex("$L($monthAlt)\\.?\\s+(\\d{1,2})(?:st|nd|rd|th)?(?:,?\\s+(\\d{4}))?(?!\\d)", opts)
    private val nextWeekday = Regex(
        "$L(?:next|this coming|nächsten|nächster|kommenden|el próximo|el proximo|próximo|proximo|este)\\s+($weekdayAlt)$R",
        opts
    )
    private val plainWeekday = Regex("$L(?:on\\s+|am\\s+|el\\s+)?($weekdayAlt)$R", opts)
    private val inDays = Regex("$L(?:in|en|dentro de)\\s+(\\d{1,2})\\s+(?:days?|tagen|días|dias)$R", opts)
    private val nextWeek = Regex("$L(?:next week|nächste woche|la próxima semana|la proxima semana|la semana que viene)$R", opts)

    private val relativeWords: List<Pair<Regex, Long>> = listOf(
        "day after tomorrow" to 2L, "übermorgen" to 2L, "pasado mañana" to 2L, "pasado manana" to 2L,
        "tomorrow" to 1L, "morgen" to 1L, "mañana" to 1L, "manana" to 1L,
        "tonight" to 0L, "this evening" to 0L, "heute abend" to 0L, "esta noche" to 0L, "esta tarde" to 0L,
        "today" to 0L, "heute" to 0L, "hoy" to 0L
    ).map { (w, d) ->
        // "Guten Morgen" / "por la mañana" mean morning, not tomorrow.
        val guard = when (w) {
            "morgen" -> "(?<!guten\\s)(?<!am\\s)(?<!heute\\s)"
            "mañana", "manana" -> "(?<!la\\s)(?<!pasado\\s)"
            else -> ""
        }
        Regex("$L$guard${Regex.escape(w)}$R", opts) to d
    }

    /** First date expression in [text], resolved against [today]. */
    fun findDate(text: String, today: LocalDate = LocalDate.now(), dayFirst: Boolean = true): DateMatch? {
        val found = mutableListOf<DateMatch>()

        isoDate.find(text)?.let { m ->
            safeDate(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
                ?.let { found += DateMatch(it, m.range, m.value) }
        }
        dottedDate.find(text)?.let { m ->
            val day = m.groupValues[1].toInt(); val month = m.groupValues[2].toInt()
            explicitOrNext(day, month, m.groupValues[3], today)?.let { found += DateMatch(it, m.range, m.value) }
        }
        slashDate.find(text)?.let { m ->
            val a = m.groupValues[1].toInt(); val b = m.groupValues[2].toInt()
            val (day, month) = if (dayFirst) a to b else b to a
            explicitOrNext(day, month, m.groupValues[3], today)?.let { found += DateMatch(it, m.range, m.value) }
        }
        dayMonth.find(text)?.let { m ->
            val month = months[m.groupValues[2].lowercase(Locale.ROOT)] ?: return@let
            explicitOrNext(m.groupValues[1].toInt(), month, m.groupValues[3], today)
                ?.let { found += DateMatch(it, m.range, m.value) }
        }
        monthDay.find(text)?.let { m ->
            val month = months[m.groupValues[1].lowercase(Locale.ROOT)] ?: return@let
            explicitOrNext(m.groupValues[2].toInt(), month, m.groupValues[3], today)
                ?.let { found += DateMatch(it, m.range, m.value) }
        }
        nextWeekday.find(text)?.let { m ->
            val dow = weekdays[m.groupValues[1].lowercase(Locale.ROOT)] ?: return@let
            found += DateMatch(today.with(TemporalAdjusters.next(dow)), m.range, m.value)
        }
        inDays.find(text)?.let { m ->
            found += DateMatch(today.plusDays(m.groupValues[1].toLong()), m.range, m.value)
        }
        nextWeek.find(text)?.let { m ->
            found += DateMatch(today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)), m.range, m.value)
        }
        relativeWords.forEach { (regex, offset) ->
            regex.find(text)?.let { m -> found += DateMatch(today.plusDays(offset), m.range, m.value) }
        }
        plainWeekday.find(text)?.let { m ->
            val dow = weekdays[m.groupValues[1].lowercase(Locale.ROOT)] ?: return@let
            found += DateMatch(today.with(TemporalAdjusters.nextOrSame(dow)), m.range, m.value)
        }
        // Earliest in the text wins; for overlaps the longer (more specific) match.
        return found.minWithOrNull(compareBy<DateMatch> { it.range.first }.thenByDescending { it.range.last })
    }

    private fun explicitOrNext(day: Int, month: Int, yearText: String, today: LocalDate): LocalDate? {
        if (yearText.isNotBlank()) {
            val y = yearText.toInt().let { if (it < 100) 2000 + it else it }
            return safeDate(y, month, day)
        }
        val thisYear = safeDate(today.year, month, day) ?: return null
        // A date without year that already passed means next year ("12.01." in December).
        return if (thisYear.isBefore(today.minusDays(1))) safeDate(today.year + 1, month, day) else thisYear
    }

    private fun safeDate(y: Int, m: Int, d: Int): LocalDate? =
        runCatching { LocalDate.of(y, m, d) }.getOrNull()

    // ------------------------------------------------------------------
    // Times
    // ------------------------------------------------------------------

    private val colonTime = Regex("(?<![\\d.:])([01]?\\d|2[0-3])[:.]([0-5]\\d)(?![\\d.]\\d)")
    private val amPmTime = Regex("(?<![\\d:])(1[0-2]|0?[1-9])(?::([0-5]\\d))?\\s?(a\\.?m\\.?|p\\.?m\\.?)(?![\\p{L}])", opts)
    private val hourWord = Regex("(?<![\\d:])([01]?\\d|2[0-3])\\s?(?:uhr|h)(?![\\p{L}])", opts)
    private val spanishHour = Regex("(?<![\\p{L}])a las\\s+([01]?\\d|2[0-3])(?![\\d:.])(?:\\s*(?:h|horas)(?![\\p{L}]))?", opts)
    private val germanUm = Regex("(?<![\\p{L}])um\\s+([01]?\\d|2[0-3])(?![\\d:.])(?:\\s*uhr(?![\\p{L}]))?", opts)

    /** First time expression in [text]; ranges in [exclude] (e.g. a matched date) are skipped. */
    fun findTime(text: String, exclude: IntRange? = null): TimeMatch? {
        fun ok(r: IntRange) = exclude == null || r.last < exclude.first || r.first > exclude.last
        val found = mutableListOf<TimeMatch>()
        colonTime.findAll(text).firstOrNull { ok(it.range) }?.let { m ->
            found += TimeMatch(LocalTime.of(m.groupValues[1].toInt(), m.groupValues[2].toInt()), m.range, m.value)
        }
        amPmTime.findAll(text).firstOrNull { ok(it.range) }?.let { m ->
            val raw = m.groupValues[1].toInt()
            val min = m.groupValues[2].ifBlank { "0" }.toInt()
            val pm = m.groupValues[3].lowercase(Locale.ROOT).startsWith("p")
            val hour = when {
                pm && raw < 12 -> raw + 12
                !pm && raw == 12 -> 0
                else -> raw
            }
            found += TimeMatch(LocalTime.of(hour, min), m.range, m.value)
        }
        listOf(hourWord, spanishHour, germanUm).forEach { regex ->
            regex.findAll(text).firstOrNull { ok(it.range) }?.let { m ->
                found += TimeMatch(LocalTime.of(m.groupValues[1].toInt(), 0), m.range, m.value)
            }
        }
        // am/pm wins over a bare colon time at the same spot ("5:30 pm").
        return found.minWithOrNull(compareBy<TimeMatch> { it.range.first }.thenByDescending { it.range.last })
    }

    // ------------------------------------------------------------------
    // Resolution helpers
    // ------------------------------------------------------------------

    /** Parses a stored/AI label ("Tomorrow", "2026-06-12", "Monday 09:00") back to a date. */
    fun resolveLabel(label: String?, today: LocalDate = LocalDate.now()): LocalDate? =
        label?.takeIf { it.isNotBlank() }?.let { findDate(it, today)?.date }

    fun toMillis(date: LocalDate, time: LocalTime?, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atTime(time ?: LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()

    /** Resolves a free label like "Tomorrow 17:30" or "Monday" into epoch millis, or null. */
    fun resolveMillis(label: String?, time: String? = null, now: Long = System.currentTimeMillis()): Long? {
        val zone = ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val combined = listOfNotNull(label, time).joinToString(" ")
        if (combined.isBlank()) return null
        val date = findDate(combined, today)
        val t = findTime(combined, date?.range)?.time
        if (date == null && t == null) return null
        var millis = toMillis(date?.date ?: today, t, zone)
        // A bare time that already passed today means tomorrow.
        if (date == null && millis <= now) millis = toMillis(today.plusDays(1), t, zone)
        return millis
    }

    /** "Tomorrow" / "Mon, 12 Jun" style label for UI, relative to [today]. */
    fun friendlyLabel(date: LocalDate, today: LocalDate, language: String): String {
        val locale = Locale.forLanguageTag(language)
        val diff = java.time.temporal.ChronoUnit.DAYS.between(today, date)
        return when (diff) {
            0L -> when (language) { "de" -> "Heute"; "es" -> "Hoy"; else -> "Today" }
            1L -> when (language) { "de" -> "Morgen"; "es" -> "Mañana"; else -> "Tomorrow" }
            in 2L..6L -> date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, locale)
                .replaceFirstChar { it.titlecase(locale) }
            else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
        }
    }
}
