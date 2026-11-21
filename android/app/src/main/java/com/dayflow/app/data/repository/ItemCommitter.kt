package com.dayflow.app.data.repository

import com.dayflow.app.core.util.DateResolver
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PrefKeys
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The one place where confirmed items are saved: Dayflow's database, the
 * system calendar (Google Calendar sync), scheduled reminder alarms, the
 * "reminder before event" automation and the transparency log. Every entry
 * point (Today, share sheet, screen intelligence, notification actions,
 * floating bubble) goes through here after the user confirmed.
 */
class ItemCommitter(
    private val repository: DayflowRepository,
    private val prefs: PreferencesRepository,
    private val personal: PersonalRepository,
    private val calendar: CalendarService,
    private val notifications: NotificationHelper
) {
    data class Report(val saved: Int, val calendarFailed: List<DayflowEvent>)

    suspend fun commit(result: DayflowAnalysisResult): Report {
        val failed = mutableListOf<DayflowEvent>()
        result.events.forEach { if (!addEvent(it)) failed += it }
        result.tasks.forEach { addTask(it) }
        result.reminders.forEach { addReminder(it) }
        return Report(result.events.size + result.tasks.size + result.reminders.size, failed)
    }

    /** Saves the event; returns false when calendar sync is on but the calendar write failed. */
    suspend fun addEvent(event: DayflowEvent): Boolean = withContext(Dispatchers.IO) {
        val start = event.startMillis ?: DateResolver.resolveMillis(event.dateLabel, event.time)
        val syncOn = prefs.boolPref(PrefKeys.CALENDAR_SYNC, true).first()
        val calendarId = if (syncOn) {
            calendar.insertEvent(event.copy(startMillis = start), prefs.stringPref(PrefKeys.CALENDAR_ID).first())
        } else null
        val saved = event.copy(startMillis = start, calendarEventId = calendarId, external = false)
        repository.insertEvent(saved)
        personal.log(PersonalRepository.LOG_SAVED, "Event: ${saved.title}")
        if (calendarId != null) personal.log(PersonalRepository.LOG_CALENDAR, "Calendar ← ${saved.title}")

        // Automation: a reminder shortly before each new appointment.
        if (start != null && prefs.boolPref(PrefKeys.AUTO_EVENT_REMINDER, true).first()) {
            val lead = prefs.stringPref(PrefKeys.EVENT_REMINDER_LEAD, "30").first().toLongOrNull()?.coerceIn(5, 1440) ?: 30
            val at = start - lead * 60_000L
            if (at > System.currentTimeMillis()) {
                addReminder(
                    DayflowReminder(
                        title = saved.title,
                        timeLabel = listOf(saved.dateLabel, saved.time).filter { it.isNotBlank() }.joinToString(" · ") +
                            " (−$lead min)",
                        scheduledTimeMillis = at,
                        sourceType = saved.sourceType
                    ),
                    automated = true
                )
            }
        }
        !syncOn || calendarId != null
    }

    suspend fun addTask(task: DayflowTask) {
        val due = task.dueMillis ?: task.dueDate?.let { DateResolver.resolveMillis(it) }
        repository.insertTask(task.copy(dueMillis = due))
        personal.log(PersonalRepository.LOG_SAVED, "Task: ${task.title}")
    }

    suspend fun addReminder(reminder: DayflowReminder, automated: Boolean = false) {
        repository.insertReminder(reminder)
        notifications.scheduleReminder(reminder)
        val time = SimpleDateFormat("EEE dd.MM. HH:mm", Locale.getDefault()).format(reminder.scheduledTimeMillis)
        personal.log(
            if (automated) PersonalRepository.LOG_AUTOMATION else PersonalRepository.LOG_REMINDER,
            "Reminder $time: ${reminder.title}"
        )
    }

    /** Deletes from Dayflow and, if Dayflow wrote it there, from the system calendar. */
    suspend fun deleteEvent(event: DayflowEvent) = withContext(Dispatchers.IO) {
        repository.deleteEvent(event.id)
        // The automatic "before the event" reminder goes with it.
        event.startMillis?.let { start ->
            repository.observeReminders().first()
                .filter { it.title == event.title && it.scheduledTimeMillis in (start - 24 * 3_600_000L)..start }
                .forEach { notifications.cancelReminder(it.id); repository.dismissReminder(it.id) }
        }
        event.calendarEventId?.let { if (calendar.deleteEvent(it)) personal.log(PersonalRepository.LOG_CALENDAR, "Calendar ✕ ${event.title}") }
    }

    /** Retries the calendar write for events whose sync failed (e.g. permission granted later). */
    suspend fun retryCalendar(events: List<DayflowEvent>): List<DayflowEvent> = withContext(Dispatchers.IO) {
        events.filter { ev ->
            val id = calendar.insertEvent(ev, prefs.stringPref(PrefKeys.CALENDAR_ID).first())
            if (id != null) {
                repository.insertEvent(ev.copy(calendarEventId = id, startMillis = ev.startMillis ?: calendar.startOf(ev)))
                personal.log(PersonalRepository.LOG_CALENDAR, "Calendar ← ${ev.title}")
            }
            id == null
        }
    }
}
