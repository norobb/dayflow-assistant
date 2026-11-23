package com.dayflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.SourceType

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timeLabel: String,
    val scheduledTimeMillis: Long,
    val sourceType: String,
    val dismissed: Boolean,
    val createdAt: Long
) {
    fun toDomain() = DayflowReminder(
        id = id,
        title = title,
        timeLabel = timeLabel,
        scheduledTimeMillis = scheduledTimeMillis,
        sourceType = SourceType.fromIdentifier(sourceType),
        dismissed = dismissed,
        createdAt = createdAt
    )

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize reminder entity string fields before persisting to Room DB
         * by stripping non-printable control characters (\x00-\x1F\x7F) and enforcing maximum
         * field string lengths to prevent DB corruption, UI rendering crashes, or IPC buffer overflow.
         */
        fun sanitizeEntityField(value: String?, fallback: String = "", maxLength: Int = 300): String {
            if (value.isNullOrBlank()) return fallback
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { fallback }
        }

        fun fromDomain(reminder: DayflowReminder) = ReminderEntity(
            id = reminder.id,
            title = sanitizeEntityField(reminder.title, fallback = "Reminder", maxLength = 300),
            timeLabel = sanitizeEntityField(reminder.timeLabel, fallback = "Later today", maxLength = 100),
            scheduledTimeMillis = reminder.scheduledTimeMillis,
            sourceType = reminder.sourceType.identifier,
            dismissed = reminder.dismissed,
            createdAt = reminder.createdAt
        )
    }
}
