package com.dayflow.app.domain.model

data class DayflowEvent(
    val id: String,
    val time: String,
    val title: String,
    val location: String? = null,
    val dateLabel: String = "Today",
    val sourceType: SourceType = SourceType.DEFAULT,
    val createdAt: Long = System.currentTimeMillis()
)
