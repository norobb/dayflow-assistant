package com.dayflow.app.integrations.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayflow.app.DayflowApplication
import com.dayflow.app.data.repository.PersonalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles taps on Dayflow notification action buttons. */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as DayflowApplication
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_REMINDER_DONE -> {
                        val id = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return@launch
                        app.repository.dismissReminder(id)
                        app.notificationHelper.cancel(id)
                    }
                    ACTION_REMINDER_SNOOZE -> {
                        val id = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return@launch
                        val row = app.database.reminderDao().getReminder(id) ?: return@launch
                        val snoozed = row.toDomain().copy(scheduledTimeMillis = System.currentTimeMillis() + SNOOZE_MS)
                        app.repository.insertReminder(snoozed)
                        app.notificationHelper.scheduleReminder(snoozed)
                        app.notificationHelper.cancel(id)
                    }
                    ACTION_SUGGESTION_ADD -> {
                        val id = intent.getStringExtra(NotificationHelper.EXTRA_SUGGESTION_ID) ?: return@launch
                        val suggestion = PendingSuggestions.take(id) ?: return@launch
                        app.itemCommitter.commit(suggestion.result)
                        app.personalRepository.log(PersonalRepository.LOG_NOTIFICATION, "Added from ${suggestion.sender}'s message")
                        app.notificationHelper.cancel(id)
                        app.soundAndHaptics.playSuccess()
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_REMINDER_DONE = "com.dayflow.app.REMINDER_DONE"
        const val ACTION_REMINDER_SNOOZE = "com.dayflow.app.REMINDER_SNOOZE"
        const val ACTION_SUGGESTION_ADD = "com.dayflow.app.SUGGESTION_ADD"
        const val SNOOZE_MS = 10 * 60_000L
    }
}

/** Analysis results waiting for the user's tap on a suggestion notification. */
object PendingSuggestions {
    data class Suggestion(val sender: String, val text: String, val result: com.dayflow.app.domain.model.DayflowAnalysisResult)

    private val map = java.util.concurrent.ConcurrentHashMap<String, Suggestion>()

    fun put(id: String, s: Suggestion) {
        if (map.size > 50) map.keys.take(10).forEach { map.remove(it) }
        map[id] = s
    }

    fun take(id: String): Suggestion? = map.remove(id)
}
