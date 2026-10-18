package com.dayflow.app.integrations.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val rawReminderId = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return
        // SECURITY ENHANCEMENT: Sanitize reminder ID from incoming broadcast intent extras
        val reminderId = NotificationHelper.sanitizeNotificationText(rawReminderId, maxLength = 100) ?: return
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val timeLabel = intent.getStringExtra(NotificationHelper.EXTRA_TIME_LABEL) ?: ""

        val helper = NotificationHelper(context)
        helper.showReminderNotification(reminderId, title, timeLabel)
    }
}
