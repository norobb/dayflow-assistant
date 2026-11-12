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
        val text = sanitizeInput(rawInput)

        // Check if matching standard preset scenarios from README
        when (type) {
            SourceType.MESSAGE -> {
                if (text.contains("station", ignoreCase = true) || text.contains("bahnhof", ignoreCase = true) || text.contains("estación", ignoreCase = true) || text.isBlank()) {
                    return DayflowAnalysisResult(
                        sourceType = type,
                        inputSnippet = text.ifBlank { "Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents." },
                        summaryText = "Detected 1 transit event and 1 preparation task from incoming chat message.",
                        events = listOf(
                            DayflowEvent(
                                title = "Pick up friend at train station",
                                time = "17:30",
                                dateLabel = "Tomorrow",
                                location = "Train Station (Central)",
                                sourceType = type
                            )
                        ),
                        tasks = listOf(
                            DayflowTask(
                                title = "Bring the documents",
                                dueDate = "Tomorrow 17:30",
                                completed = false,
                                sourceType = type
                            )
                        ),
                        reminders = listOf(
                            DayflowReminder(
                                title = "Bring the documents",
                                timeLabel = "Tomorrow 17:00",
                                sourceType = type
                            )
                        ),
                        insight = DayflowInsight(
                            title = "Message Intelligence",
                            text = "Train station pickup detected. Traffic near Central Station is heavy around 17:00.",
                            visible = true
                        ),
                        confidence = 0.98f
                    )
                }
            }
            SourceType.SCREENSHOT -> {
                return DayflowAnalysisResult(
                    sourceType = type,
                    inputSnippet = text.ifBlank { "Doctor Appointment: Dr. Julia Stein — Zahnheilkunde — Tuesday 14:30" },
                    summaryText = "Detected medical appointment from confirmation receipt screenshot.",
                    events = listOf(
                        DayflowEvent(
                            title = "Dentist Appointment (Dr. Stein)",
                            time = "14:30",
                            dateLabel = "Tuesday",
                            location = "Praxis Dr. Julia Stein",
                            sourceType = type
                        )
                    ),
                    tasks = listOf(
                        DayflowTask(
                            title = "Bring insurance card to dentist",
                            dueDate = "Tuesday 14:00",
                            completed = false,
                            sourceType = type
                        )
                    ),
                    reminders = listOf(
                        DayflowReminder(
                            title = "Dentist appointment at 14:30",
                            timeLabel = "Tuesday 14:00",
                            sourceType = type
                        )
                    ),
                    insight = DayflowInsight(
                        title = "Appointment Insight",
                        text = "Regular 6-month checkup. Remember to bring your insurance card.",
                        visible = true
                    ),
                    confidence = 0.99f
                )
            }
            SourceType.PDF -> {
                return DayflowAnalysisResult(
                    sourceType = type,
                    inputSnippet = text.ifBlank { "School Trip — Barcelona (June 12–15, 2026)" },
                    summaryText = "Extracted itinerary dates, packing requirements, and parent authorization slip.",
                    events = listOf(
                        DayflowEvent(
                            title = "Departure & Flight to Barcelona",
                            time = "08:15",
                            dateLabel = "June 12",
                            location = "Airport Terminal 2",
                            sourceType = type
                        ),
                        DayflowEvent(
                            title = "Sagrada Familia & Gothic Quarter Tour",
                            time = "10:30",
                            dateLabel = "June 13",
                            location = "Barcelona Center",
                            sourceType = type
                        )
                    ),
                    tasks = listOf(
                        DayflowTask(
                            title = "Bring passport (valid > 6 months)",
                            dueDate = "Before June 10",
                            completed = false,
                            sourceType = type
                        ),
                        DayflowTask(
                            title = "Submit permission form to school office",
                            dueDate = "Friday before trip",
                            completed = false,
                            sourceType = type
                        )
                    ),
                    reminders = listOf(
                        DayflowReminder(
                            title = "Submit Barcelona trip permission form",
                            timeLabel = "Friday 12:00",
                            sourceType = type
                        )
                    ),
                    insight = DayflowInsight(
                        title = "Itinerary Verified",
                        text = "Multi-day school itinerary processed. 2 events and 2 prerequisites added.",
                        visible = true
                    ),
                    confidence = 0.97f
                )
            }
            SourceType.VOICE -> {
                return DayflowAnalysisResult(
                    sourceType = type,
                    inputSnippet = text.ifBlank { "Remind me next Monday to submit my presentation." },
                    summaryText = "Transcribed voice note and identified action item with reminder anchor.",
                    events = emptyList(),
                    tasks = listOf(
                        DayflowTask(
                            title = "Submit presentation",
                            dueDate = "Next Monday",
                            completed = false,
                            sourceType = type
                        )
                    ),
                    reminders = listOf(
                        DayflowReminder(
                            title = "Submit presentation",
                            timeLabel = "Monday 09:00",
                            sourceType = type
                        )
                    ),
                    insight = DayflowInsight(
                        title = "Voice Transcription",
                        text = "Voice note transcribed with high confidence (98%).",
                        visible = true
                    ),
                    confidence = 0.98f
                )
            }
            else -> {
                // fall through to custom parsing
            }
        }

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
                text = "Parsed using offline deterministic engine. Configure Gemini in Settings for advanced AI understanding.",
                visible = true
            ),
            confidence = 0.85f,
            hasError = false
        )
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize raw input text for offline local parsing by stripping
         * non-printable control characters and capping length to prevent DoS or injection.
         */
        fun sanitizeInput(rawInput: String?, maxLength: Int = 1000): String {
            if (rawInput.isNullOrBlank()) return ""
            return rawInput
                .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
        }
    }
}
