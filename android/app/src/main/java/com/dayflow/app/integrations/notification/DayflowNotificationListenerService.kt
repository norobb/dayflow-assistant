package com.dayflow.app.integrations.notification

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dayflow.app.ShareActivity

class DayflowNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Filter out Dayflow's own notifications
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // SECURITY ENHANCEMENT: Sanitize title and text extracted from third-party app notifications
        // to strip non-printable control characters and enforce strict string length bounds.
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        val title = NotificationHelper.sanitizeNotificationText(rawTitle, maxLength = 200) ?: ""
        val text = NotificationHelper.sanitizeNotificationText(rawText, maxLength = 1000) ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Optionally send a broadcast or allow explicit user click to analyze
    }
}
