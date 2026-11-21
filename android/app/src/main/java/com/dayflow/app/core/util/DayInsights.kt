package com.dayflow.app.core.util

import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Proactive context from the user's own data ("Your presentation is due
 * tomorrow, 2 tasks still open"). Used by the Today card and the morning briefing.
 */
object DayInsights {

    fun contextInsight(
        events: List<DayflowEvent>,
        tasks: List<DayflowTask>,
        language: String,
        now: Long = System.currentTimeMillis()
    ): DayflowInsight? {
        val open = tasks.filterNot { it.completed }
        val next = events.filter { (it.startMillis ?: 0) > now }.minByOrNull { it.startMillis!! }
        val dueSoon = open.filter { it.dueMillis != null && it.dueMillis <= endOfTomorrow(now) }
        val overdue = open.filter { it.dueMillis != null && it.dueMillis < now }
        if (next == null && open.isEmpty()) return null

        val parts = mutableListOf<String>()
        if (next != null && next.startMillis!! - now <= 36 * 3_600_000L) {
            val time = SimpleDateFormat("HH:mm", Locale.ROOT).format(next.startMillis)
            val when_ = if (isToday(next.startMillis, now)) T.today(language) else T.tomorrow(language)
            parts += T.next(language, next.title, when_, time, next.location)
        }
        if (overdue.isNotEmpty()) parts += T.overdue(language, overdue.size)
        else if (dueSoon.isNotEmpty()) parts += T.dueSoon(language, dueSoon.size, dueSoon.first().title)
        else if (open.isNotEmpty()) parts += T.open(language, open.size)
        if (parts.isEmpty()) return null
        return DayflowInsight(T.title(language), parts.joinToString(" "), true)
    }

    /** Title + multi-line body for the morning briefing notification. */
    fun briefing(
        events: List<DayflowEvent>,
        tasks: List<DayflowTask>,
        reminders: List<DayflowReminder>,
        language: String,
        now: Long = System.currentTimeMillis()
    ): Pair<String, String> {
        val today = events.filter { it.startMillis != null && isToday(it.startMillis, now) }.sortedBy { it.startMillis }
        val open = tasks.filterNot { it.completed }
        val rem = reminders.filter { isToday(it.scheduledTimeMillis, now) && !it.dismissed }
        val fmt = SimpleDateFormat("HH:mm", Locale.ROOT)
        val lines = mutableListOf<String>()
        today.forEach { lines += "${fmt.format(it.startMillis)}  ${it.title}" + (it.location?.let { l -> " · $l" } ?: "") }
        if (open.isNotEmpty()) lines += T.open(language, open.size) + " " + open.take(3).joinToString(", ") { it.title }
        rem.forEach { lines += "⏰ ${fmt.format(it.scheduledTimeMillis)} ${it.title}" }
        if (lines.isEmpty()) lines += T.free(language)
        return T.briefingTitle(language, today.size) to lines.joinToString("\n")
    }

    /** Routines ("Training, Tue/Thu 18:00") as today's timeline events, if today is one of their days. */
    fun routinesToday(entries: List<com.dayflow.app.data.local.ContextEntryEntity>, now: Long = System.currentTimeMillis()): List<DayflowEvent> {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val iso = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 // Calendar.SUNDAY=1 -> ISO 7
        return entries.filter { e ->
            e.kind == com.dayflow.app.data.local.ContextEntryEntity.KIND_ROUTINE &&
                e.weekdays.split(',').mapNotNull { it.trim().toIntOrNull() }.contains(iso)
        }.map { e ->
            val (h, m) = e.time.split(':').takeIf { it.size == 2 }?.let { it[0].toInt() to it[1].toInt() } ?: (0 to 0)
            val start = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            DayflowEvent(
                id = "routine-${e.id}", time = e.time, title = e.term, dateLabel = "",
                sourceType = com.dayflow.app.domain.model.SourceType.CUSTOM, startMillis = start, external = true
            )
        }
    }

    private fun isToday(millis: Long, now: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = millis }
        val b = Calendar.getInstance().apply { timeInMillis = now }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun endOfTomorrow(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now; add(Calendar.DAY_OF_YEAR, 2)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private object T {
        fun title(l: String) = when (l) { "de" -> "Kontext-Hinweis"; "es" -> "Recordatorio de contexto"; else -> "Context reminder" }
        fun today(l: String) = when (l) { "de" -> "heute"; "es" -> "hoy"; else -> "today" }
        fun tomorrow(l: String) = when (l) { "de" -> "morgen"; "es" -> "mañana"; else -> "tomorrow" }
        fun next(l: String, t: String, w: String, time: String, loc: String?) = when (l) {
            "de" -> "Als Nächstes: $t, $w um $time" + (loc?.let { " ($it)" } ?: "") + "."
            "es" -> "Lo próximo: $t, $w a las $time" + (loc?.let { " ($it)" } ?: "") + "."
            else -> "Up next: $t, $w at $time" + (loc?.let { " ($it)" } ?: "") + "."
        }
        fun overdue(l: String, n: Int) = when (l) {
            "de" -> "$n überfällige Aufgabe${if (n == 1) "" else "n"}."
            "es" -> "$n tarea${if (n == 1) "" else "s"} vencida${if (n == 1) "" else "s"}."
            else -> "$n overdue task${if (n == 1) "" else "s"}."
        }
        fun dueSoon(l: String, n: Int, first: String) = when (l) {
            "de" -> "„$first“ ist bald fällig – noch $n offene Aufgabe${if (n == 1) "" else "n"} bis morgen."
            "es" -> "«$first» vence pronto: $n tarea${if (n == 1) "" else "s"} pendiente${if (n == 1) "" else "s"} hasta mañana."
            else -> "“$first” is due soon — $n unfinished task${if (n == 1) "" else "s"} until tomorrow."
        }
        fun open(l: String, n: Int) = when (l) {
            "de" -> "$n offene Aufgabe${if (n == 1) "" else "n"}."
            "es" -> "$n tarea${if (n == 1) "" else "s"} pendiente${if (n == 1) "" else "s"}."
            else -> "$n open task${if (n == 1) "" else "s"}."
        }
        fun free(l: String) = when (l) { "de" -> "Heute steht nichts an."; "es" -> "Hoy no tienes nada pendiente."; else -> "Nothing planned today." }
        fun briefingTitle(l: String, n: Int) = when (l) {
            "de" -> "Guten Morgen – $n Termin${if (n == 1) "" else "e"} heute"
            "es" -> "Buenos días: $n cita${if (n == 1) "" else "s"} hoy"
            else -> "Good morning — $n event${if (n == 1) "" else "s"} today"
        }
    }
}
