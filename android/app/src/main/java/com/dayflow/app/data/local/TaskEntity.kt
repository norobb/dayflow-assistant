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
        /**
         * SECURITY ENHANCEMENT: Sanitize task entity string fields before persisting to Room DB
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

        fun fromDomain(task: DayflowTask) = TaskEntity(
            id = task.id,
            title = sanitizeEntityField(task.title, fallback = "Task", maxLength = 300),
            completed = task.completed,
            dueDate = task.dueDate?.let { sanitizeEntityField(it, fallback = "", maxLength = 100) }?.ifBlank { null },
            sourceType = task.sourceType.identifier,
            createdAt = task.createdAt
        )
    }
}
