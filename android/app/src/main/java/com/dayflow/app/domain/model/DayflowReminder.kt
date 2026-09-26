package com.dayflow.app.domain.model

data class DayflowReminder(
    val id: String,
    val title: String,
    val timeLabel: String,
    val scheduledTimeMillis: Long = System.currentTimeMillis() + 3600000L,
    val sourceType: SourceType = SourceType.DEFAULT,
    val dismissed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
