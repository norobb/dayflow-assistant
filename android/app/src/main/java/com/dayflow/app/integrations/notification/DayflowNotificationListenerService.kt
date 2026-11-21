package com.dayflow.app.integrations.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dayflow.app.DayflowApplication
import com.dayflow.app.ai.AnalysisContext
import com.dayflow.app.ai.LocalProvider
import com.dayflow.app.data.repository.PersonalRepository
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.PrefKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reads incoming message notifications (only after the user granted
 * notification access) and, when one contains an appointment, task or
 * reminder, posts a Dayflow suggestion with one-tap "Add" / "Review" actions.
 * Analysis always runs on the device: message content never goes to a cloud model.
 */
class DayflowNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val local = LocalProvider()
    private val recent = ArrayDeque<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.packageName == packageName || sbn.isOngoing) return
        val notification = sbn.notification ?: return
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))
            ?.toString()?.trim().orEmpty()
        if (text.isBlank()) return
        val fingerprint = "${sbn.packageName}|$title|$text"
        synchronized(recent) {
            if (fingerprint in recent) return
            recent.addLast(fingerprint)
            if (recent.size > 30) recent.removeFirst()
        }

        scope.launch {
            runCatching {
                val app = application as? DayflowApplication ?: return@launch
                val prefs = app.preferencesRepository
                val isMessage = notification.category == Notification.CATEGORY_MESSAGE ||
                    extras.containsKey(Notification.EXTRA_MESSAGES) || extras.containsKey("android.messagingStyleUser")
                if (isMessage && prefs.boolPref(PrefKeys.AUTO_LEARN_CONTACTS, true).first()) {
                    app.personalRepository.learnContact(title)
                }
                if (!prefs.boolPref(PrefKeys.AUTO_NOTIFICATION_SUGGESTIONS, true).first()) return@launch

                val language = prefs.getLanguage().first()
                val result = local.analyze(
                    SourceType.MESSAGE, text, null, null, "", "",
                    AnalysisContext(language = language, aliases = app.personalRepository.aliases())
                )
                val total = result.events.size + result.tasks.size + result.reminders.size
                if (total == 0) return@launch
                // Name the sender in event titles: "Pick me up at the train station (Max)".
                val withSender = if (title.isNotBlank()) result.copy(
                    events = result.events.map { e -> if (e.title.contains(title, true)) e else e.copy(title = "${e.title} ($title)") }
                ) else result
                val id = java.util.UUID.randomUUID().toString()
                PendingSuggestions.put(id, PendingSuggestions.Suggestion(title, text, withSender))
                val summary = (withSender.events.map { listOf(it.title, it.dateLabel, it.time).filter(String::isNotBlank).joinToString(" · ") } +
                    withSender.tasks.map { "✓ " + it.title } +
                    withSender.reminders.map { "⏰ " + it.title }).joinToString("\n")
                NotificationInsightBus.publish(
                    NotificationInsightBus.Insight(
                        packageName = sbn.packageName,
                        sender = title.ifBlank { appLabel(sbn.packageName) },
                        content = summary,
                        title = title,
                        text = text,
                        suggestionId = id
                    )
                )
                app.notificationHelper.showSuggestion(id, title.ifBlank { appLabel(sbn.packageName) }, summary, "$title: $text", language)
                app.personalRepository.log(PersonalRepository.LOG_NOTIFICATION, "Suggestion from ${appLabel(sbn.packageName)} ($total)")
            }
        }
    }

    private fun appLabel(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
