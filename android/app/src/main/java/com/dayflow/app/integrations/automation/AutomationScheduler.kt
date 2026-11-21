package com.dayflow.app.integrations.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayflow.app.DayflowApplication
import com.dayflow.app.core.util.DayInsights
import com.dayflow.app.data.repository.PersonalRepository
import com.dayflow.app.domain.repository.PrefKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Daily automation workflows: the morning briefing notification and the
 * clean-up of completed tasks. "Reminder before each event" lives in
 * [com.dayflow.app.data.repository.ItemCommitter], "suggestions from
 * notifications" in the notification listener.
 */
object AutomationScheduler {
    const val ACTION_DAILY = "com.dayflow.app.AUTOMATION_DAILY"

    suspend fun reschedule(app: DayflowApplication) {
        val prefs = app.preferencesRepository
        val enabled = prefs.boolPref(PrefKeys.AUTO_BRIEFING, false).first() || prefs.boolPref(PrefKeys.AUTO_CLEANUP, false).first()
        val alarm = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(app)
        alarm.cancel(pi)
        if (!enabled) return
        val at = nextTrigger(prefs.stringPref(PrefKeys.BRIEFING_TIME, "07:30").first())
        runCatching { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) }
            .onFailure { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) }
    }

    fun nextTrigger(time: String, now: Long = System.currentTimeMillis()): Long {
        val (h, m) = time.split(":").let { (it.getOrNull(0)?.toIntOrNull() ?: 7) to (it.getOrNull(1)?.toIntOrNull() ?: 30) }
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23)); set(Calendar.MINUTE, m.coerceIn(0, 59))
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    private fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
        context, 9001,
        Intent(context, AutomationReceiver::class.java).setAction(ACTION_DAILY),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** Runs the daily workflows now (also used by "Run now" in settings). */
    suspend fun runDaily(app: DayflowApplication) {
        val prefs = app.preferencesRepository
        if (prefs.boolPref(PrefKeys.AUTO_CLEANUP, false).first()) {
            val removed = app.database.taskDao().deleteCompletedBefore(System.currentTimeMillis() - 24 * 3_600_000L)
            if (removed > 0) app.personalRepository.log(PersonalRepository.LOG_AUTOMATION, "Cleanup: $removed completed task(s) removed")
        }
        if (prefs.boolPref(PrefKeys.AUTO_BRIEFING, false).first()) {
            val language = prefs.getLanguage().first()
            val now = System.currentTimeMillis()
            val stored = app.repository.observeEvents().first()
            val calendar = app.calendarService.readEvents(
                now - 12 * 3_600_000L, now + 24 * 3_600_000L,
                stored.mapNotNull { it.calendarEventId }.toSet()
            )
            val routines = DayInsights.routinesToday(app.personalRepository.routines(), now)
            val (title, text) = DayInsights.briefing(
                stored + calendar + routines, app.repository.observeTasks().first(), app.repository.observeReminders().first(), language, now
            )
            app.notificationHelper.showBriefing(title, text)
            app.personalRepository.log(PersonalRepository.LOG_AUTOMATION, "Morning briefing: $title")
        }
    }
}

class AutomationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AutomationScheduler.ACTION_DAILY) return
        val app = context.applicationContext as DayflowApplication
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AutomationScheduler.runDaily(app)
            } finally {
                AutomationScheduler.reschedule(app)
                pending.finish()
            }
        }
    }
}
