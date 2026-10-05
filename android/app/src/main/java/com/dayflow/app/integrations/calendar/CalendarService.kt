package com.dayflow.app.integrations.calendar

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.dayflow.app.core.util.DateTimeUtils
import com.dayflow.app.core.util.PermissionUtils
import com.dayflow.app.domain.model.DayflowEvent
import java.util.TimeZone

class CalendarService(private val context: Context) {

    /**
     * Inserts event directly if WRITE_CALENDAR permission is granted,
     * otherwise launches the standard Android Calendar Intent so the user
     * can verify and confirm in their native calendar app.
     */
    fun insertEvent(event: DayflowEvent): Boolean {
        return if (PermissionUtils.hasCalendarPermission(context)) {
            insertDirectly(event) || launchCalendarIntent(event)
        } else {
            launchCalendarIntent(event)
        }
    }

    private fun insertDirectly(event: DayflowEvent): Boolean {
        return try {
            val startTime = DateTimeUtils.parseTimeToMillis(event.time)
            val endTime = startTime + 3600000L
            val cleanTitle = sanitizeField(event.title) ?: "Event"
            val cleanLocation = sanitizeField(event.location)

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, cleanTitle)
                put(CalendarContract.Events.DESCRIPTION, "Organized by Dayflow")
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.CALENDAR_ID, 1) // Primary calendar
                if (!cleanLocation.isNullOrBlank()) {
                    put(CalendarContract.Events.EVENT_LOCATION, cleanLocation)
                }
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (_: Exception) {
            false
        }
    }

    fun launchCalendarIntent(event: DayflowEvent): Boolean {
        return try {
            val startTime = DateTimeUtils.parseTimeToMillis(event.time)
            val cleanTitle = sanitizeField(event.title) ?: "Event"
            val cleanLocation = sanitizeField(event.location)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, cleanTitle)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 3600000L)
                putExtra(CalendarContract.Events.DESCRIPTION, "Organized by Dayflow")
                if (!cleanLocation.isNullOrBlank()) {
                    putExtra(CalendarContract.Events.EVENT_LOCATION, cleanLocation)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize calendar text fields (title, location) by stripping
         * non-printable control characters and truncating long strings to prevent IPC transaction
         * buffer overflow (TransactionTooLargeException) or DoS attacks.
         */
        fun sanitizeField(value: String?, maxLength: Int = 500): String? {
            if (value.isNullOrBlank()) return null
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { null }
        }
    }
}
