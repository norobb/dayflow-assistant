package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType

class LocalMockProvider : AIProvider {
    override val name: String = "Local Parsing Engine"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String
    ): DayflowAnalysisResult {
        val text = rawInput?.trim() ?: ""
        if (text.isBlank() && (mediaBytes == null || mediaBytes.isEmpty())) {
            return DayflowAnalysisResult(
                sourceType = type,
                inputSnippet = text,
                summaryText = "No content provided to organize.",
                events = emptyList(),
                tasks = emptyList(),
                reminders = emptyList(),
                insight = DayflowInsight(title = "", text = "", visible = false),
                confidence = 0f
            )
        }

        val eventsList = mutableListOf<DayflowEvent>()
        val tasksList = mutableListOf<DayflowTask>()
        val remindersList = mutableListOf<DayflowReminder>()

        val lower = text.lowercase()
        if (lower.contains("remind") || lower.contains("erinnere") || lower.contains("recuérdame")) {
            remindersList.add(
                DayflowReminder(
                    id = java.util.UUID.randomUUID().toString(),
                    title = text.ifBlank { "Reminder" },
                    timeLabel = "Today 18:00",
                    sourceType = type
                )
            )
        } else if (lower.contains("task") || lower.contains("todo") || lower.contains("aufgabe") || lower.contains("tarea")) {
            tasksList.add(
                DayflowTask(
                    id = java.util.UUID.randomUUID().toString(),
                    title = text.ifBlank { "New Task" },
                    completed = false,
                    dueDate = "Today",
                    sourceType = type
                )
            )
        } else {
            eventsList.add(
                DayflowEvent(
                    id = java.util.UUID.randomUUID().toString(),
                    time = "12:00 - 13:00",
                    title = text.ifBlank { "Organized Item" },
                    location = null,
                    dateLabel = "Today",
                    sourceType = type
                )
            )
        }

        return DayflowAnalysisResult(
            sourceType = type,
            inputSnippet = text,
            summaryText = "Extracted 1 action item from user input.",
            events = eventsList,
            tasks = tasksList,
            reminders = remindersList,
            insight = DayflowInsight(
                title = "Local Analysis",
                text = "Parsed using offline rules. Configure Gemini in Settings for advanced AI understanding.",
                visible = true
            ),
            confidence = 0.8f,
            hasError = false
        )
    }
}
