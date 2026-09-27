package com.dayflow.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.ai.DayflowAnalyzer
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.calendar.CalendarService
import com.dayflow.app.integrations.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayUiState(
    val events: List<DayflowEvent> = emptyList(),
    val tasks: List<DayflowTask> = emptyList(),
    val reminders: List<DayflowReminder> = emptyList(),
    val insight: DayflowInsight? = null,
    val isAnalyzing: Boolean = false,
    val pendingVerification: DayflowAnalysisResult? = null
)

class TodayViewModel(
    private val repository: DayflowRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarService: CalendarService,
    private val notificationHelper: NotificationHelper,
    private val soundAndHaptics: SoundAndHaptics,
    private val analyzer: DayflowAnalyzer
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    private val _pendingVerification = MutableStateFlow<DayflowAnalysisResult?>(null)

    val uiState: StateFlow<TodayUiState> = combine(
        repository.observeEvents(),
        repository.observeTasks(),
        repository.observeReminders(),
        repository.observeInsight(),
        _isAnalyzing,
        _pendingVerification
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val events = flows[0] as List<DayflowEvent>
        @Suppress("UNCHECKED_CAST")
        val tasks = flows[1] as List<DayflowTask>
        @Suppress("UNCHECKED_CAST")
        val reminders = flows[2] as List<DayflowReminder>
        val insight = flows[3] as DayflowInsight
        val isAnalyzing = flows[4] as Boolean
        val verification = flows[5] as DayflowAnalysisResult?

        TodayUiState(
            events = events,
            tasks = tasks,
            reminders = reminders,
            insight = if (insight.visible && insight.title.isNotBlank()) insight else null,
            isAnalyzing = isAnalyzing,
            pendingVerification = verification
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState()
    )

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            soundAndHaptics.playToggle()
            repository.toggleTask(taskId)
        }
    }

    fun snoozeInsight() {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            repository.snoozeInsight()
        }
    }

    fun startAnalysis(
        type: SourceType,
        rawInput: String? = null,
        mediaBytes: ByteArray? = null,
        mimeType: String? = null
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            soundAndHaptics.playClick()
            val provider = preferencesRepository.getAiProvider().first()
            val apiKey = preferencesRepository.getGeminiApiKey().first()
            val modelOverride = preferencesRepository.getGeminiModel().first()

            val result = analyzer.analyze(
                providerName = provider,
                type = type,
                rawInput = rawInput,
                mediaBytes = mediaBytes,
                mimeType = mimeType,
                apiKey = apiKey,
                modelOverride = modelOverride
            )
            _isAnalyzing.value = false
            _pendingVerification.value = result
            if (result.hasError) {
                soundAndHaptics.playError()
            } else {
                soundAndHaptics.playSuccess()
            }
        }
    }

    fun dismissVerification() {
        _pendingVerification.value = null
    }

    fun acceptVerification(result: DayflowAnalysisResult) {
        viewModelScope.launch {
            result.events.forEach { ev ->
                repository.insertEvent(ev)
                calendarService.insertEvent(ev)
            }
            result.tasks.forEach { tk ->
                repository.insertTask(tk)
            }
            result.reminders.forEach { rm ->
                repository.insertReminder(rm)
                notificationHelper.scheduleReminder(rm)
            }
            if (result.insight.visible && result.insight.title.isNotBlank()) {
                repository.setInsight(result.insight)
            }
            soundAndHaptics.playSuccess()
            _pendingVerification.value = null
        }
    }

    class Factory(
        private val repository: DayflowRepository,
        private val preferencesRepository: PreferencesRepository,
        private val calendarService: CalendarService,
        private val notificationHelper: NotificationHelper,
        private val soundAndHaptics: SoundAndHaptics,
        private val analyzer: DayflowAnalyzer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(
                repository,
                preferencesRepository,
                calendarService,
                notificationHelper,
                soundAndHaptics,
                analyzer
            ) as T
        }
    }
}
