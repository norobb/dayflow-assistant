package com.dayflow.app.integrations.accessibility.context

import com.dayflow.app.integrations.accessibility.control.ScreenActionBus

/** What a captured screen turned out to contain. */
enum class CandidateKind { EVENT, TASK, REMINDER }

/** Why a capture happened. Ambient captures never produce proposals. */
enum class CaptureTrigger { MANUAL, BUBBLE, AMBIENT }

/**
 * One structured item the user may add to Dayflow, extracted from a screen.
 * Purely data — [com.dayflow.app.integrations.accessibility.context.ScreenContextAnalyzer]
 * produces it, the UI decides whether to accept it.
 */
data class ScreenCandidate(
    val id: String = java.util.UUID.randomUUID().toString(),
    val kind: CandidateKind,
    val title: String,
    val timeLabel: String? = null,
    val dateLabel: String? = null,
    val location: String? = null,
    val evidence: String = "",
    val confidence: Float = 0.7f,
    /** Resolved start (events/reminders) or due time (tasks); null if unknown. */
    val startMillis: Long? = null
) {
    /** "Tomorrow · 17:30" style secondary line, built by the UI. */
    val scheduleLabel: String
        get() = listOfNotNull(dateLabel, timeLabel).joinToString(" · ")
}

/**
 * One read of the foreground screen: the raw node text, the numbered
 * interactive elements, and where it came from. This is the unit the
 * Intelligence screen shows in its history.
 */
data class ScreenCapture(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageName: String = "",
    val appLabel: String = "",
    val capturedAt: Long = System.currentTimeMillis(),
    val texts: List<String> = emptyList(),
    val elements: List<ScreenActionBus.ScreenElement> = emptyList(),
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val trigger: CaptureTrigger = CaptureTrigger.MANUAL
) {
    /** Display text for the collapsible "screen text" block. */
    val text: String
        get() = texts.joinToString(" · ")

    val isEmpty: Boolean
        get() = texts.isEmpty() && elements.isEmpty()
}
