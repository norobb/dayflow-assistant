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
         * SECURITY ENHANCEMENT: Sanitize event text fields by stripping non-printable
         * control characters (\x00-\x1F\x7F) and truncating strings to prevent database payload
         * corruption, UI rendering crashes, and IPC transaction size overflows.
         */
        fun sanitizeText(value: String?, fallback: String = "", maxLength: Int = 300): String {
            if (value.isNullOrBlank()) return fallback
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { fallback }
        }

        fun fromDomain(event: DayflowEvent) = EventEntity(
            id = event.id,
            time = sanitizeText(event.time, "12:00", 100),
            title = sanitizeText(event.title, "Event", 300),
            location = event.location?.let { sanitizeText(it, "", 300) }?.ifBlank { null },
            dateLabel = sanitizeText(event.dateLabel, "Today", 100),
            sourceType = event.sourceType.identifier,
            createdAt = event.createdAt
        )
    }
}
