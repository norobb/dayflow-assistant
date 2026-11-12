package com.dayflow.app.integrations.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dayflow.app.ai.LocalMockProvider
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DayflowNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        // Filter out Dayflow's own notifications
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        scope.launch {
            try {
                val input = buildString {
                    if (title.isNotBlank()) appendLine(title)
                    if (text.isNotBlank()) appendLine(text)
                }.trim()
                // Use the deterministic local engine so notifications are understood
                // even when offline / without a Gemini key.
                LocalMockProvider().analyze(
                    SourceType.CUSTOM,
                    input,
                    null,
                    null,
                    "",
                    ""
                )
                NotificationInsightBus.publish(
                    NotificationInsightBus.Insight(
                        packageName = sbn.packageName,
                        sender = title.ifBlank { sbn.packageName },
                        content = text.ifBlank { title },
                        title = title,
                        text = text
                    )
                )
            } catch (_: Exception) {
                // Never crash the listener; silently ignore.
            }
        }
    }
}
