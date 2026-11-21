package com.dayflow.app.integrations.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dayflow.app.MainActivity
import com.dayflow.app.R
import com.dayflow.app.core.util.PermissionUtils
import com.dayflow.app.domain.model.DayflowReminder

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "dayflow_reminders"
        const val CHANNEL_EVENTS = "dayflow_events"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME_LABEL = "extra_time_label"
        const val CHANNEL_SUGGESTIONS = "dayflow_suggestions"
        const val CHANNEL_BRIEFING = "dayflow_briefing"
        const val EXTRA_SUGGESTION_ID = "extra_suggestion_id"
        const val EXTRA_REVIEW_TEXT = "extra_review_text"
        const val EXTRA_OPEN_ROUTE = "extra_open_route"
        const val BRIEFING_ID = 4711

        /** Resources in the app's chosen language (may differ from the system locale). */
        fun localized(context: Context, language: String): Context {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(java.util.Locale.forLanguageTag(language))
            return context.createConfigurationContext(config)
        }

        /**
         * SECURITY ENHANCEMENT: Sanitize notification text fields (title, timeLabel) by stripping
         * non-printable control characters and truncating long strings to prevent IPC transaction
         * buffer overflow or rendering issues in Android notification views.
         */
        fun sanitizeNotificationText(value: String?, maxLength: Int = 200): String? {
            if (value.isNullOrBlank()) return null
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { null }
        }
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_reminders_desc)
                enableVibration(true)
            }

            val eventsChannel = NotificationChannel(
                CHANNEL_EVENTS,
                context.getString(R.string.notif_channel_events),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_events_desc)
            }

            val suggestionsChannel = NotificationChannel(
                CHANNEL_SUGGESTIONS,
                context.getString(R.string.notif_channel_suggestions),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_channel_suggestions_desc) }

            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFING,
                context.getString(R.string.notif_channel_briefing),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(remindersChannel)
            manager?.createNotificationChannel(eventsChannel)
            manager?.createNotificationChannel(suggestionsChannel)
            manager?.createNotificationChannel(briefingChannel)
        }
    }

    fun showReminderNotification(reminderId: String, title: String, timeLabel: String, language: String = "en") {
        if (!PermissionUtils.hasNotificationPermission(context)) return
        val res = localized(context, language)

        val cleanTitle = sanitizeNotificationText(title, 200) ?: "Reminder"
        val cleanTime = sanitizeNotificationText(timeLabel, 100) ?: ""

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (cleanTime.isNotBlank()) "$cleanTitle · $cleanTime" else cleanTitle

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.dayflow_symbol_mono)
            .setContentTitle("Dayflow")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF641C24.toInt())
            .addAction(0, res.getString(R.string.notif_action_done), actionIntent(NotificationActionReceiver.ACTION_REMINDER_DONE, reminderId))
            .addAction(0, res.getString(R.string.notif_action_snooze), actionIntent(NotificationActionReceiver.ACTION_REMINDER_SNOOZE, reminderId))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
        } catch (_: SecurityException) {}
    }

    private fun actionIntent(action: String, id: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        (action + id).hashCode(),
        Intent(context, NotificationActionReceiver::class.java).setAction(action).putExtra(EXTRA_REMINDER_ID, id).putExtra(EXTRA_SUGGESTION_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun openAppIntent(requestCode: Int, extras: Intent.() -> Unit): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            extras()
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /**
     * "Dayflow found an appointment in Max's message" with one-tap actions.
     * Nothing is saved until the user taps Add (or reviews in the app).
     */
    fun showSuggestion(id: String, sender: String, summary: String, sourceText: String, language: String) {
        if (!PermissionUtils.hasNotificationPermission(context)) return
        val res = localized(context, language)
        val title = res.getString(R.string.notif_suggestion_title, sanitizeNotificationText(sender, 60) ?: "")
        val notification = NotificationCompat.Builder(context, CHANNEL_SUGGESTIONS)
            .setSmallIcon(R.drawable.dayflow_symbol_mono)
            .setContentTitle(title)
            .setContentText(sanitizeNotificationText(summary, 200))
            .setStyle(NotificationCompat.BigTextStyle().bigText(sanitizeNotificationText(summary, 400)))
            .setColor(0xFF641C24.toInt())
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(id.hashCode()) { putExtra(EXTRA_REVIEW_TEXT, sourceText.take(4000)) })
            .addAction(0, res.getString(R.string.notif_action_add), actionIntent(NotificationActionReceiver.ACTION_SUGGESTION_ADD, id))
            .addAction(0, res.getString(R.string.notif_action_review), openAppIntent(id.hashCode() + 1) { putExtra(EXTRA_REVIEW_TEXT, sourceText.take(4000)) })
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
        } catch (_: SecurityException) {}
    }

    fun cancel(id: String) = NotificationManagerCompat.from(context).cancel(id.hashCode())

    fun showBriefing(title: String, text: String) {
        if (!PermissionUtils.hasNotificationPermission(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_BRIEFING)
            .setSmallIcon(R.drawable.dayflow_symbol_mono)
            .setContentTitle(title)
            .setContentText(text.lineSequence().firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(0xFF641C24.toInt())
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(BRIEFING_ID) {})
            .build()
        try {
            NotificationManagerCompat.from(context).notify(BRIEFING_ID, notification)
        } catch (_: SecurityException) {}
    }

    fun cancelReminder(reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(
            context, reminderId.hashCode(), Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pi)
    }

    fun scheduleReminder(reminder: DayflowReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val cleanTitle = sanitizeNotificationText(reminder.title, 200) ?: "Reminder"
        val cleanTime = sanitizeNotificationText(reminder.timeLabel, 100) ?: ""

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, cleanTitle)
            putExtra(EXTRA_TIME_LABEL, cleanTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = reminder.scheduledTimeMillis.coerceAtLeast(System.currentTimeMillis() + 5000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback to non-exact if SCHEDULE_EXACT_ALARM is restricted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
