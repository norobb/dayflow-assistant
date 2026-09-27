package com.dayflow.app.domain.model

data class DayflowAnalysisResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sourceType: SourceType = SourceType.CUSTOM,
    val inputSnippet: String = "",
    val summaryText: String = "",
    val events: List<DayflowEvent> = emptyList(),
    val tasks: List<DayflowTask> = emptyList(),
    val reminders: List<DayflowReminder> = emptyList(),
    val insight: DayflowInsight = DayflowInsight(title = "", text = "", visible = false),
    val confidence: Float = 0.95f,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)
