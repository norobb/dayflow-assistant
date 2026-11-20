package com.dayflow.app.ui.intelligence

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.core.util.DateTimeUtils
import com.dayflow.app.core.util.PermissionUtils
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.accessibility.DayflowAccessibilityService
import com.dayflow.app.integrations.accessibility.context.CandidateKind
import com.dayflow.app.integrations.accessibility.context.CaptureTrigger
import com.dayflow.app.integrations.accessibility.context.ScreenCandidate
import com.dayflow.app.integrations.accessibility.context.ScreenCapture
import com.dayflow.app.integrations.accessibility.context.ScreenCaptureStore
import com.dayflow.app.integrations.accessibility.context.ScreenIntelligenceRunner
import com.dayflow.app.integrations.accessibility.context.ScreenObservationSettings
import com.dayflow.app.integrations.accessibility.control.ScreenActionBus
import com.dayflow.app.integrations.accessibility.control.ScreenAgent
import com.dayflow.app.integrations.accessibility.control.ScreenAgentBus
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper
import com.dayflow.app.integrations.overlay.ScreenIntelligenceBubbleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A translatable status line; the screen maps [code] to a string resource. */
data class UiStatus(val code: String, val args: List<String> = emptyList())

data class IntelligenceUiState(
    val language: String = "en",
    val accessibilityActive: Boolean = false,
    val notificationAccess: Boolean = false,
    val overlayPermission: Boolean = false,
    val ambientObservation: Boolean = false,
    val floatingBubble: Boolean = false,
    val capturing: Boolean = false,
    val analyzing: Boolean = false,
    val capture: ScreenCapture? = null,
    val proposals: List<ScreenCandidate> = emptyList(),
    val addedIds: Set<String> = emptySet(),
    val history: List<ScreenCapture> = emptyList(),
    val elements: List<ScreenActionBus.ScreenElement> = emptyList(),
    val actionResult: ScreenActionBus.ActionResult = ScreenActionBus.ActionResult.Idle,
    val status: UiStatus? = null,
    val agentSteps: List<ScreenAgentBus.Step> = emptyList(),
    val agentRunning: Boolean = false,
    val showText: Boolean = false
) {
    val hasAccess: Boolean get() = accessibilityActive
    val hasProposals: Boolean get() = proposals.isNotEmpty()
}

class IntelligenceViewModel(
    private val context: Context,
    private val repository: DayflowRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarService: CalendarService,
    private val notificationHelper: NotificationHelper,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    private val _state = MutableStateFlow(IntelligenceUiState())
    val state: StateFlow<IntelligenceUiState> = _state.asStateFlow()

    private val agent = ScreenAgent()

    init {
        viewModelScope.launch {
            preferencesRepository.getLanguage().collect { lang ->
                _state.update { it.copy(language = lang) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isScreenObservationEnabled().collect { enabled ->
                ScreenObservationSettings.setAmbientEnabled(enabled)
                _state.update { it.copy(ambientObservation = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isScreenBubbleEnabled().collect { enabled ->
                _state.update { it.copy(floatingBubble = enabled) }
            }
        }
        viewModelScope.launch {
            ScreenCaptureStore.latest.collect { capture ->
                if (capture != null) {
                    _state.update { it.copy(capture = capture, capturing = false) }
                }
            }
        }
        viewModelScope.launch {
            ScreenCaptureStore.history.collect { history ->
                _state.update { it.copy(history = history) }
            }
        }
        viewModelScope.launch {
            ScreenIntelligenceRunner.proposals.collect { proposals ->
                _state.update { current ->
                    current.copy(
                        proposals = proposals,
                        addedIds = current.addedIds.intersect(proposals.map { it.id }.toSet())
                    )
                }
            }
        }
        viewModelScope.launch {
            ScreenIntelligenceRunner.analyzing.collect { analyzing ->
                _state.update { it.copy(analyzing = analyzing) }
            }
        }
        viewModelScope.launch {
            ScreenActionBus.elements.collect { elements ->
                _state.update { it.copy(elements = elements) }
            }
        }
        viewModelScope.launch {
            ScreenActionBus.result.collect { result ->
                _state.update { current ->
                    val stillCapturing = when (result) {
                        is ScreenActionBus.ActionResult.Done ->
                            result.code != ScreenActionBus.Codes.CAPTURE_DONE
                        is ScreenActionBus.ActionResult.Failed ->
                            result.code != ScreenActionBus.Codes.CAPTURE_EMPTY
                        is ScreenActionBus.ActionResult.Running -> true
                        ScreenActionBus.ActionResult.Idle -> false
                    }
                    current.copy(actionResult = result, capturing = if (stillCapturing) current.capturing else false)
                }
            }
        }
        viewModelScope.launch {
            ScreenAgentBus.steps.collect { steps -> _state.update { it.copy(agentSteps = steps) } }
        }
        viewModelScope.launch {
            ScreenAgentBus.running.collect { running -> _state.update { it.copy(agentRunning = running) } }
        }
        refreshAccessState()
    }

    // ------------------------------------------------------------------
    // Access & settings
    // ------------------------------------------------------------------

    fun refreshAccessState() {
        _state.update {
            it.copy(
                accessibilityActive = isAccessibilityServiceEnabled(),
                notificationAccess = NotificationManagerCompat
                    .getEnabledListenerPackages(context)
                    .contains(context.packageName),
                overlayPermission = Settings.canDrawOverlays(context)
            )
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        if (DayflowAccessibilityService.instance != null) return true
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                info.resolveInfo.serviceInfo.packageName == context.packageName &&
                    info.resolveInfo.serviceInfo.name.endsWith(
                        DayflowAccessibilityService::class.java.simpleName
                    )
            }
    }

    fun openAccessibilitySettings() {
        launchSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openNotificationAccessSettings() {
        launchSettings(Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS"))
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
        launchSettings(intent)
    }

    private fun launchSettings(intent: Intent) {
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun setAmbientObservation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setScreenObservationEnabled(enabled)
            ScreenObservationSettings.setAmbientEnabled(enabled)
            if (!enabled) {
                // Ambient captures are not interesting without observation.
                ScreenCaptureStore.clear()
            }
            soundAndHaptics.playToggle()
        }
    }

    fun setFloatingBubble(enabled: Boolean) {
        if (enabled && !Settings.canDrawOverlays(context)) {
            _state.update { it.copy(status = UiStatus(STATUS_BUBBLE_PERMISSION)) }
            requestOverlayPermission()
            return
        }
        viewModelScope.launch {
            preferencesRepository.setScreenBubbleEnabled(enabled)
            soundAndHaptics.playToggle()
            val intent = Intent(context, ScreenIntelligenceBubbleService::class.java)
            runCatching {
                if (enabled) context.startService(intent) else context.stopService(intent)
            }
            _state.update {
                it.copy(status = UiStatus(if (enabled) STATUS_BUBBLE_ON else STATUS_BUBBLE_OFF))
            }
        }
    }

    // ------------------------------------------------------------------
    // Capture
    // ------------------------------------------------------------------

    fun captureNow() {
        if (!_state.value.accessibilityActive) {
            _state.update { it.copy(status = UiStatus(STATUS_NEED_ACCESS)) }
            return
        }
        soundAndHaptics.playClick()
        _state.update {
            it.copy(
                capturing = true,
                status = null,
                addedIds = emptySet()
            )
        }
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.CAPTURE))
    }

    fun review(capture: ScreenCapture) {
        _state.update { it.copy(addedIds = emptySet()) }
        ScreenIntelligenceRunner.review(capture)
    }

    fun clearHistory() {
        ScreenCaptureStore.clear()
        _state.update { it.copy(capture = null, proposals = emptyList(), addedIds = emptySet()) }
    }

    fun toggleText() {
        _state.update { it.copy(showText = !it.showText) }
    }

    // ------------------------------------------------------------------
    // Verified actions
    // ------------------------------------------------------------------

    fun accept(candidate: ScreenCandidate) {
        if (candidate.id in _state.value.addedIds) return
        viewModelScope.launch {
            save(candidate)
            soundAndHaptics.playSuccess()
            _state.update {
                it.copy(
                    addedIds = it.addedIds + candidate.id,
                    status = UiStatus(STATUS_ADDED)
                )
            }
        }
    }

    fun acceptAll() {
        val pending = _state.value.proposals.filterNot { it.id in _state.value.addedIds }
        if (pending.isEmpty()) return
        viewModelScope.launch {
            pending.forEach { save(it) }
            soundAndHaptics.playSuccess()
            _state.update {
                it.copy(
                    addedIds = it.addedIds + pending.map { c -> c.id },
                    status = UiStatus(STATUS_ADDED_ALL, listOf(pending.size.toString()))
                )
            }
        }
    }

    private suspend fun save(candidate: ScreenCandidate) {
        val source = SourceType.CUSTOM
        when (candidate.kind) {
            CandidateKind.EVENT -> {
                val event = DayflowEvent(
                    time = candidate.timeLabel ?: (candidate.dateLabel ?: "—"),
                    title = candidate.title,
                    location = candidate.location,
                    dateLabel = candidate.dateLabel ?: "Today",
                    sourceType = source
                )
                repository.insertEvent(event)
                if (PermissionUtils.hasCalendarPermission(context)) {
                    calendarService.insertEvent(event)
                }
            }

            CandidateKind.TASK -> {
                repository.insertTask(
                    DayflowTask(
                        title = candidate.title,
                        dueDate = candidate.scheduleLabel.ifBlank { null },
                        sourceType = source
                    )
                )
            }

            CandidateKind.REMINDER -> {
                val reminder = DayflowReminder(
                    title = candidate.title,
                    timeLabel = candidate.scheduleLabel.ifBlank { "Today" },
                    scheduledTimeMillis = DateTimeUtils.parseTimeToMillis(candidate.timeLabel ?: ""),
                    sourceType = source
                )
                repository.insertReminder(reminder)
                notificationHelper.scheduleReminder(reminder)
            }
        }
    }

    fun dismissStatus() {
        _state.update { it.copy(status = null) }
    }

    // ------------------------------------------------------------------
    // Screen control
    // ------------------------------------------------------------------

    fun scan() {
        soundAndHaptics.playClick()
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SCAN))
    }

    fun tapElement(id: Int) {
        soundAndHaptics.playClick()
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.TAP_ELEMENT, id))
    }

    fun swipeUp() {
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SWIPE_UP))
    }

    fun swipeDown() {
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SWIPE_DOWN))
    }

    fun back() {
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.BACK))
    }

    // ------------------------------------------------------------------
    // Screen agent
    // ------------------------------------------------------------------

    fun runAgent(goal: String) {
        if (goal.isBlank()) return
        agent.run(goal.trim())
    }

    fun stopAgent() {
        ScreenAgentBus.cancel()
    }

    class Factory(
        private val context: Context,
        private val repository: DayflowRepository,
        private val preferencesRepository: PreferencesRepository,
        private val calendarService: CalendarService,
        private val notificationHelper: NotificationHelper,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntelligenceViewModel(
                context.applicationContext,
                repository,
                preferencesRepository,
                calendarService,
                notificationHelper,
                soundAndHaptics
            ) as T
        }
    }

    companion object {
        const val STATUS_ADDED = "added"
        const val STATUS_ADDED_ALL = "added_all"
        const val STATUS_NEED_ACCESS = "need_access"
        const val STATUS_BUBBLE_ON = "bubble_on"
        const val STATUS_BUBBLE_OFF = "bubble_off"
        const val STATUS_BUBBLE_PERMISSION = "bubble_permission"
    }
}
