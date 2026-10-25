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

        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        val title = sanitizeNotificationText(rawTitle)
        val text = sanitizeNotificationText(rawText)

        if (title.isBlank() && text.isBlank()) return

        // Optionally send a broadcast or allow explicit user click to analyze
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize notification strings received from external apps.
         * Strips non-printable control characters (\x00-\x1F\x7F) and enforces a hard length
         * bound (default 500 chars) to prevent UI crashes, control character injection, or DoS
         * from untrusted system notification payloads.
         */
        fun sanitizeNotificationText(text: String?, maxLength: Int = 500): String {
            if (text.isNullOrBlank()) return ""
            return text
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
        }
    }
}
