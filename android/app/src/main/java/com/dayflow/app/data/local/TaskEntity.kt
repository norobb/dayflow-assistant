package com.dayflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completed: Boolean,
    val dueDate: String?,
    val sourceType: String,
    val createdAt: Long
) {
    fun toDomain() = DayflowTask(
        id = id,
        title = title,
        completed = completed,
        dueDate = dueDate,
        sourceType = SourceType.fromIdentifier(sourceType),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(task: DayflowTask) = TaskEntity(
            id = task.id,
            title = task.title,
            completed = task.completed,
            dueDate = task.dueDate,
            sourceType = task.sourceType.identifier,
            createdAt = task.createdAt
        )
    }
}
