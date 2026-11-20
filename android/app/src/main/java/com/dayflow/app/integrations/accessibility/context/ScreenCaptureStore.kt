package com.dayflow.app.integrations.accessibility.context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for screen captures, shared by the Intelligence
 * screen (in-app) and the floating bubble (out of app).
 *
 * A capture of the same app within [DEDUPE_WINDOW_MS] replaces the previous
 * one instead of stacking duplicates — accessibility fires content changes
 * constantly, and scrolling a feed would otherwise flood the history.
 */
object ScreenCaptureStore {

    const val MAX_HISTORY = 12
    const val DEDUPE_WINDOW_MS = 20_000L

    private val _latest = MutableStateFlow<ScreenCapture?>(null)
    val latest: StateFlow<ScreenCapture?> = _latest.asStateFlow()

    private val _history = MutableStateFlow<List<ScreenCapture>>(emptyList())
    val history: StateFlow<List<ScreenCapture>> = _history.asStateFlow()

    fun publish(capture: ScreenCapture) {
        _latest.value = capture
        if (capture.trigger == CaptureTrigger.AMBIENT) return

        val previous = _history.value.firstOrNull()
        val isRepeat = previous != null &&
            previous.packageName == capture.packageName &&
            previous.text == capture.text &&
            capture.capturedAt - previous.capturedAt < DEDUPE_WINDOW_MS

        _history.value = if (isRepeat) {
            listOf(capture) + _history.value.drop(1)
        } else {
            (listOf(capture) + _history.value).take(MAX_HISTORY)
        }
    }

    fun clear() {
        _latest.value = null
        _history.value = emptyList()
    }
}
