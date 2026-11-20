package com.dayflow.app.integrations.accessibility.context

import com.dayflow.app.DayflowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Turns the newest explicit [ScreenCapture] into [ScreenCandidate]s.
 *
 * Lives in the application scope so the floating bubble and the in-app
 * Intelligence screen share one analysis result instead of parsing the same
 * screen twice. Ambient captures are ignored on purpose: silently reading
 * every app screen would be surveillance, not assistance.
 */
object ScreenIntelligenceRunner {

    private val analyzer = ScreenContextAnalyzer()

    private val _proposals = MutableStateFlow<List<ScreenCandidate>>(emptyList())
    val proposals: StateFlow<List<ScreenCandidate>> = _proposals.asStateFlow()

    private val _analyzing = MutableStateFlow(false)
    val analyzing: StateFlow<Boolean> = _analyzing.asStateFlow()

    private var scope: CoroutineScope? = null

    fun init() {
        if (scope != null) return
        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope = job
        job.launch {
            ScreenCaptureStore.latest.collectLatest { capture ->
                if (capture == null) {
                    _proposals.value = emptyList()
                    return@collectLatest
                }
                if (capture.trigger == CaptureTrigger.AMBIENT) return@collectLatest
                analyze(capture)
            }
        }
    }

    private suspend fun analyze(capture: ScreenCapture) {
        _analyzing.value = true
        try {
            val app = DayflowApplication.instance
            val language = runCatching {
                app?.preferencesRepository?.getLanguage()?.first()
            }.getOrNull().orEmpty().ifBlank { "en" }
            _proposals.value = analyzer.analyze(capture.texts, language)
        } finally {
            _analyzing.value = false
        }
    }

    /** Re-runs detection for a stored capture (history item tapped). */
    fun review(capture: ScreenCapture) {
        ScreenCaptureStore.publish(capture)
    }
}
