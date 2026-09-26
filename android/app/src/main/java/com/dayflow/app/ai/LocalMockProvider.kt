package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DetectedDocInfo
import com.dayflow.app.domain.model.DetectedEvent
import com.dayflow.app.domain.model.DetectedReminder
import com.dayflow.app.domain.model.DetectedTask
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.delay

class LocalMockProvider : AIProvider {
    override val name: String = "Local Deterministic Provider"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        simulatedDelayMs: Long
    ): DayflowAnalysisResult {
        // Natural processing delay
        val delayTime = simulatedDelayMs.coerceIn(100L, 3000L)
        delay(delayTime)

        return when (type) {
            SourceType.MESSAGE -> {
                DayflowAnalysisResult(
                    id = "analysis-msg-${System.currentTimeMillis()}",
                    sourceType = SourceType.MESSAGE,
                    inputSnippet = "Hey, can you pick me up tomorrow at 17:30 at the train station? And please bring the documents.",
                    summary = "Detected 1 transit event and 1 preparation task from chat message.",
                    events = listOf(
                        DetectedEvent(
                            title = "Pick up friend at train station",
                            time = "17:30",
                            dateLabel = "Tomorrow",
                            location = "Train Station (Central)",
                            confidence = 0.98f
                        )
                    ),
                    tasks = listOf(
                        DetectedTask(
                            title = "Bring the documents to station",
                            dueDate = "Tomorrow 17:30",
                            confidence = 0.96f
                        )
                    ),
                    reminders = listOf(
                        DetectedReminder(
                            title = "Pick up friend & bring documents",
                            timeLabel = "Tomorrow 17:00",
                            confidence = 0.95f
                        )
                    )
                )
            }

            SourceType.SCREENSHOT -> {
                DayflowAnalysisResult(
                    id = "analysis-screen-${System.currentTimeMillis()}",
                    sourceType = SourceType.SCREENSHOT,
                    inputSnippet = "Appointment Receipt: Dr. Julia Stein — Zahnheilkunde — Tuesday 14:30",
                    summary = "Detected medical appointment from confirmation receipt screenshot.",
                    events = listOf(
                        DetectedEvent(
                            title = "Dentist Appointment (Dr. Stein)",
                            time = "14:30",
                            dateLabel = "Tuesday",
                            location = "Praxis Dr. Julia Stein",
                            confidence = 0.99f
                        )
                    ),
                    tasks = listOf(
                        DetectedTask(
                            title = "Bring insurance card to dentist",
                            dueDate = "Tuesday 14:00",
                            confidence = 0.92f
                        )
                    ),
                    reminders = listOf(
                        DetectedReminder(
                            title = "Dentist Appointment in 1 hour",
                            timeLabel = "Tuesday 13:30",
                            confidence = 0.95f
                        )
                    )
                )
            }

            SourceType.PDF -> {
                DayflowAnalysisResult(
                    id = "analysis-pdf-${System.currentTimeMillis()}",
                    sourceType = SourceType.PDF,
                    inputSnippet = "Flight & Hotel Confirmation: Barcelona (BCN) · Oct 12 - 16 · Flight LH1812",
                    summary = "Extracted 2 travel dates, 2 deadlines, and accommodation check-in.",
                    events = listOf(
                        DetectedEvent(
                            title = "Flight to Barcelona (LH1812)",
                            time = "07:15",
                            dateLabel = "Oct 12",
                            location = "Terminal 2, Gate B14",
                            confidence = 0.99f
                        ),
                        DetectedEvent(
                            title = "Hotel Check-in: Hotel Arts",
                            time = "15:00",
                            dateLabel = "Oct 12",
                            location = "Carrer de Marina 19, Barcelona",
                            confidence = 0.97f
                        )
                    ),
                    tasks = listOf(
                        DetectedTask(
                            title = "Complete online check-in 24h prior",
                            dueDate = "Oct 11 07:15",
                            confidence = 0.94f
                        ),
                        DetectedTask(
                            title = "Bring printed boarding pass & passport",
                            dueDate = "Oct 12 05:00",
                            confidence = 0.95f
                        )
                    ),
                    reminders = listOf(
                        DetectedReminder(
                            title = "Leave for airport (Terminal 2)",
                            timeLabel = "Oct 12 04:30",
                            confidence = 0.96f
                        )
                    ),
                    docInfo = DetectedDocInfo(
                        title = "Barcelona_Travel_Itinerary.pdf",
                        pages = 4,
                        extractedDates = 2,
                        extractedTasks = 2
                    )
                )
            }

            SourceType.VOICE -> {
                DayflowAnalysisResult(
                    id = "analysis-voice-${System.currentTimeMillis()}",
                    sourceType = SourceType.VOICE,
                    inputSnippet = "Voice Note: Remember to call the mechanic tomorrow morning at 09:00 about the brake inspection and ask for the invoice.",
                    summary = "Transcribed voice note: Extracted 1 reminder and 1 follow-up task.",
                    events = listOf(
                        DetectedEvent(
                            title = "Call mechanic (Brake inspection)",
                            time = "09:00",
                            dateLabel = "Tomorrow",
                            location = "Auto Service Center",
                            confidence = 0.96f
                        )
                    ),
                    tasks = listOf(
                        DetectedTask(
                            title = "Ask mechanic for itemized invoice",
                            dueDate = "Tomorrow 09:30",
                            confidence = 0.91f
                        )
                    ),
                    reminders = listOf(
                        DetectedReminder(
                            title = "Call mechanic about brake inspection",
                            timeLabel = "Tomorrow 08:45",
                            confidence = 0.95f
                        )
                    )
                )
            }

            SourceType.CUSTOM, SourceType.DEFAULT -> {
                val input = rawInput?.trim() ?: "Custom input"
                // Extract any time pattern (e.g. 17:30, 09:00, 2pm, etc.)
                val timeRegex = Regex("""\b([01]?[0-9]|2[0-3]):[0-5][0-9]\b""")
                val foundTime = timeRegex.find(input)?.value ?: "12:00"

                DayflowAnalysisResult(
                    id = "analysis-custom-${System.currentTimeMillis()}",
                    sourceType = SourceType.CUSTOM,
                    inputSnippet = input.take(200),
                    summary = "Extracted action items from user text.",
                    events = listOf(
                        DetectedEvent(
                            title = input.take(40),
                            time = foundTime,
                            dateLabel = "Today",
                            confidence = 0.92f
                        )
                    ),
                    tasks = listOf(
                        DetectedTask(
                            title = "Follow up: " + input.take(30),
                            dueDate = "Today $foundTime",
                            confidence = 0.88f
                        )
                    ),
                    reminders = listOf(
                        DetectedReminder(
                            title = input.take(35),
                            timeLabel = "Today $foundTime",
                            confidence = 0.90f
                        )
                    )
                )
            }
        }
    }
}
