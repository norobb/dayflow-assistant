package com.dayflow.app.integrations.accessibility.context

import com.dayflow.app.data.repository.ItemCommitter
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType

/** Turns a confirmed screen candidate into a saved Dayflow item (in-app screen and floating bubble). */
object ScreenCandidateSaver {
    suspend fun save(candidate: ScreenCandidate, committer: ItemCommitter) {
        val source = SourceType.SCREENSHOT
        when (candidate.kind) {
            CandidateKind.EVENT -> committer.addEvent(
                DayflowEvent(
                    time = candidate.timeLabel.orEmpty(),
                    title = candidate.title,
                    location = candidate.location,
                    dateLabel = candidate.dateLabel.orEmpty(),
                    sourceType = source,
                    startMillis = candidate.startMillis
                )
            )
            CandidateKind.TASK -> committer.addTask(
                DayflowTask(
                    title = candidate.title,
                    dueDate = candidate.scheduleLabel.ifBlank { null },
                    sourceType = source,
                    dueMillis = candidate.startMillis
                )
            )
            CandidateKind.REMINDER -> committer.addReminder(
                DayflowReminder(
                    title = candidate.title,
                    timeLabel = candidate.scheduleLabel,
                    scheduledTimeMillis = candidate.startMillis ?: (System.currentTimeMillis() + 3_600_000L),
                    sourceType = source
                )
            )
        }
    }
}
