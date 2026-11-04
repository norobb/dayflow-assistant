package com.dayflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.SourceType

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val time: String,
    val title: String,
    val location: String?,
    val dateLabel: String,
    val sourceType: String,
    val createdAt: Long
) {
    fun toDomain() = DayflowEvent(
        id = id,
        time = time,
        title = title,
        location = location,
        dateLabel = dateLabel,
        sourceType = SourceType.fromIdentifier(sourceType),
        createdAt = createdAt
    )

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize entity string fields before persisting to Room DB
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

        fun fromDomain(event: DayflowEvent) = EventEntity(
            id = event.id,
            time = sanitizeEntityField(event.time, fallback = "12:00", maxLength = 100),
            title = sanitizeEntityField(event.title, fallback = "Event", maxLength = 300),
            location = event.location?.let { sanitizeEntityField(it, fallback = "", maxLength = 300) }?.ifBlank { null },
            dateLabel = sanitizeEntityField(event.dateLabel, fallback = "Today", maxLength = 100),
            sourceType = event.sourceType.identifier,
            createdAt = event.createdAt
        )
    }
}
