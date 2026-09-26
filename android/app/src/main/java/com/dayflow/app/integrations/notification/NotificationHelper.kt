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

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(remindersChannel)
            manager?.createNotificationChannel(eventsChannel)
        }
    }

    fun showReminderNotification(reminderId: String, title: String, timeLabel: String) {
        if (!PermissionUtils.hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.dayflow_symbol_official)
            .setContentTitle("Dayflow")
            .setContentText("$title · $timeLabel")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF641C24.toInt())
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
        } catch (_: SecurityException) {}
    }

    fun scheduleReminder(reminder: DayflowReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_TIME_LABEL, reminder.timeLabel)
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
