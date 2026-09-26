package com.dayflow.app.domain.model

data class DetectedEvent(
    val title: String,
    val time: String,
    val dateLabel: String,
    val location: String? = null,
    val confidence: Float = 0.95f
)

data class DetectedTask(
    val title: String,
    val dueDate: String? = null,
    val confidence: Float = 0.95f
)

data class DetectedReminder(
    val title: String,
    val timeLabel: String,
    val confidence: Float = 0.95f
)

data class DetectedDocInfo(
    val title: String,
    val pages: Int,
    val extractedDates: Int,
    val extractedTasks: Int
)

data class DayflowAnalysisResult(
    val id: String,
    val sourceType: SourceType,
    val inputSnippet: String,
    val summary: String,
    val events: List<DetectedEvent> = emptyList(),
    val tasks: List<DetectedTask> = emptyList(),
    val reminders: List<DetectedReminder> = emptyList(),
    val docInfo: DetectedDocInfo? = null
)
