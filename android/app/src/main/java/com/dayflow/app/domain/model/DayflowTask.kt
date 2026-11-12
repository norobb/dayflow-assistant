package com.dayflow.app.domain.model

data class DayflowTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
    val dueDate: String? = null,
    val sourceType: SourceType = SourceType.DEFAULT,
    val createdAt: Long = System.currentTimeMillis()
)
