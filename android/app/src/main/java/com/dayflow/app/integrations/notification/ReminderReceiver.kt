package com.dayflow.app.integrations.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dayflow.app.DayflowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val timeLabel = intent.getStringExtra(NotificationHelper.EXTRA_TIME_LABEL) ?: ""
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as DayflowApplication
                // Skip reminders the user already completed or deleted.
                val row = app.database.reminderDao().getReminder(reminderId)
                if (row != null && !row.dismissed) {
                    val lang = app.preferencesRepository.getLanguage().first()
                    app.notificationHelper.showReminderNotification(reminderId, title, timeLabel, lang)
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Re-arms pending reminder alarms and automations after a reboot or app update. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as DayflowApplication
                app.database.reminderDao().getPending(System.currentTimeMillis())
                    .forEach { app.notificationHelper.scheduleReminder(it.toDomain()) }
                com.dayflow.app.integrations.automation.AutomationScheduler.reschedule(app)
            } finally {
                pending.finish()
            }
        }
    }
}
