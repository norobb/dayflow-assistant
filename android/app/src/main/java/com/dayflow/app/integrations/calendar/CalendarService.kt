package com.dayflow.app.integrations.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import com.dayflow.app.core.util.DateResolver
import com.dayflow.app.core.util.PermissionUtils
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.TimeZone

/**
 * Two-way bridge to the Android calendar provider. Accounts synced to the
 * device (Google Calendar, Exchange, …) appear here, so writing to and reading
 * from the provider is how Dayflow syncs with Google Calendar.
 */
class CalendarService(private val context: Context) {

    data class CalendarInfo(val id: Long, val name: String, val account: String, val color: Int, val primary: Boolean)

    fun hasPermission() = PermissionUtils.hasCalendarPermission(context)

    fun writableCalendars(): List<CalendarInfo> {
        if (!hasPermission()) return emptyList()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ? AND ${CalendarContract.Calendars.VISIBLE} = 1"
        return runCatching {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, projection, selection,
                arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()), null
            )?.use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(CalendarInfo(c.getLong(0), c.getString(1).orEmpty(), c.getString(2).orEmpty(), c.getInt(3), c.getInt(4) == 1))
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    /** The chosen calendar, else the account's primary one, else a local "Dayflow" calendar. */
    fun resolveTargetCalendar(preferredId: String): Long? {
        val calendars = writableCalendars()
        preferredId.toLongOrNull()?.let { id -> if (calendars.any { it.id == id }) return id }
        calendars.firstOrNull { it.primary && it.account.contains("@") }?.let { return it.id }
        calendars.firstOrNull { it.name == LOCAL_CALENDAR_NAME }?.let { return it.id }
        calendars.firstOrNull()?.let { return it.id }
        return createLocalCalendar()
    }

    private fun createLocalCalendar(): Long? = runCatching {
        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, LOCAL_CALENDAR_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, LOCAL_CALENDAR_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, LOCAL_CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, LOCAL_CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF641C24.toInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, LOCAL_CALENDAR_NAME)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
        }
        context.contentResolver.insert(uri, values)?.let { ContentUris.parseId(it) }
    }.getOrNull()

    /** Start time for an event: its resolved millis, else parsed from its labels. */
    fun startOf(event: DayflowEvent): Long =
        event.startMillis
            ?: DateResolver.resolveMillis(event.dateLabel, event.time)
            ?: (System.currentTimeMillis() + 3_600_000L)

    /** Writes the event directly. Returns the new calendar row id, or null. */
    fun insertEvent(event: DayflowEvent, preferredCalendarId: String = ""): Long? {
        if (!hasPermission()) return null
        val calendarId = resolveTargetCalendar(preferredCalendarId) ?: return null
        return runCatching {
            val start = startOf(event)
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.DTSTART, start)
                put(CalendarContract.Events.DTEND, start + DEFAULT_DURATION_MS)
                put(CalendarContract.Events.TITLE, sanitizeField(event.title) ?: "Event")
                put(CalendarContract.Events.DESCRIPTION, DESCRIPTION_MARKER)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                sanitizeField(event.location)?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)?.let { ContentUris.parseId(it) }
        }.getOrNull()
    }

    fun deleteEvent(calendarEventId: Long): Boolean = hasPermission() && runCatching {
        context.contentResolver.delete(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarEventId), null, null
        ) > 0
    }.getOrDefault(false)

    /** Opens the system calendar's editor prefilled — the user confirms there. */
    fun launchCalendarIntent(event: DayflowEvent): Boolean = runCatching {
        val start = startOf(event)
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, sanitizeField(event.title) ?: "Event")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + DEFAULT_DURATION_MS)
            putExtra(CalendarContract.Events.DESCRIPTION, DESCRIPTION_MARKER)
            sanitizeField(event.location)?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    /** Opens an existing calendar entry in the user's calendar app. */
    fun openInCalendar(calendarEventId: Long) = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarEventId))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.isSuccess

    /** Calendar instances in [from, to), except those Dayflow wrote itself (already in its own list). */
    fun readEvents(from: Long, to: Long, excludeIds: Set<Long>): List<DayflowEvent> {
        if (!hasPermission()) return emptyList()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, from); ContentUris.appendId(it, to)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.ALL_DAY
        )
        return runCatching {
            context.contentResolver.query(uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
                buildList {
                    while (c.moveToNext()) {
                        val eventId = c.getLong(0)
                        if (eventId in excludeIds) continue
                        val begin = c.getLong(1)
                        val allDay = c.getInt(4) == 1
                        add(
                            DayflowEvent(
                                id = "cal-$eventId-$begin",
                                time = if (allDay) "" else java.text.SimpleDateFormat("HH:mm", java.util.Locale.ROOT).format(begin),
                                title = c.getString(2).orEmpty().ifBlank { "—" },
                                location = c.getString(3)?.ifBlank { null },
                                dateLabel = "",
                                sourceType = SourceType.DEFAULT,
                                startMillis = begin,
                                calendarEventId = eventId,
                                external = true
                            )
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    /** Emits whenever the calendar provider changes (new Google sync, edits in other apps). */
    fun changes(): Flow<Unit> = callbackFlow {
        trySend(Unit)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { trySend(Unit) }
        }
        val registered = runCatching {
            context.contentResolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
        }.isSuccess
        awaitClose { if (registered) context.contentResolver.unregisterContentObserver(observer) }
    }

    companion object {
        const val LOCAL_CALENDAR_NAME = "Dayflow"
        const val DESCRIPTION_MARKER = "Organized by Dayflow"
        const val DEFAULT_DURATION_MS = 3_600_000L

        /** Strips control characters and caps length for calendar text fields. */
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
