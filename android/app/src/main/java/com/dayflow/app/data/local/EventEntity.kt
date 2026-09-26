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
        fun fromDomain(event: DayflowEvent) = EventEntity(
            id = event.id,
            time = event.time,
            title = event.title,
            location = event.location,
            dateLabel = event.dateLabel,
            sourceType = event.sourceType.identifier,
            createdAt = event.createdAt
        )
    }
}
