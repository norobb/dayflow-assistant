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
        fun fromDomain(reminder: DayflowReminder) = ReminderEntity(
            id = reminder.id,
            title = reminder.title,
            timeLabel = reminder.timeLabel,
            scheduledTimeMillis = reminder.scheduledTimeMillis,
            sourceType = reminder.sourceType.identifier,
            dismissed = reminder.dismissed,
            createdAt = reminder.createdAt
        )
    }
}
