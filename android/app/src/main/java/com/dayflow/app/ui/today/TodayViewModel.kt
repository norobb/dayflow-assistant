package com.dayflow.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.ai.DayflowAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) { events, tasks, reminders, insight, isAnalyzing, verification ->
        TodayUiState(
            events = events,
            tasks = tasks,
            reminders = reminders,
            insight = if (insight.visible) insight else null,
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

    fun startAnalysis(type: SourceType, rawInput: String? = null) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            soundAndHaptics.playClick()
            val provider = preferencesRepository.getAiProvider().first()
            val result = analyzer.analyze(provider, type, rawInput)
            _isAnalyzing.value = false
            _pendingVerification.value = result
            soundAndHaptics.playSuccess()
        }
    }

    fun dismissVerification() {
        _pendingVerification.value = null
    }

    fun addCalendarEvent(result: DayflowAnalysisResult) {
        viewModelScope.launch {
            result.events.firstOrNull()?.let { ev ->
                val event = DayflowEvent(
                    id = "ev-${System.currentTimeMillis()}",
                    time = ev.time,
                    title = ev.title,
                    location = ev.location,
                    dateLabel = ev.dateLabel,
                    sourceType = result.sourceType
                )
                repository.insertEvent(event)
                calendarService.insertEvent(event)
                soundAndHaptics.playSuccess()
            }
        }
    }

    fun addTask(result: DayflowAnalysisResult) {
        viewModelScope.launch {
            result.tasks.firstOrNull()?.let { tk ->
                val task = DayflowTask(
                    id = "tk-${System.currentTimeMillis()}",
                    title = tk.title,
                    completed = false,
                    dueDate = tk.dueDate,
                    sourceType = result.sourceType
                )
                repository.insertTask(task)
                soundAndHaptics.playSuccess()
            }
        }
    }

    fun addReminder(result: DayflowAnalysisResult) {
        viewModelScope.launch {
            result.reminders.firstOrNull()?.let { rm ->
                val reminder = DayflowReminder(
                    id = "rm-${System.currentTimeMillis()}",
                    title = rm.title,
                    timeLabel = rm.timeLabel,
                    sourceType = result.sourceType
                )
                repository.insertReminder(reminder)
                notificationHelper.scheduleReminder(reminder)
                soundAndHaptics.playSuccess()
            }
        }
    }

    fun addBoth(result: DayflowAnalysisResult) {
        addCalendarEvent(result)
        addTask(result)
        addReminder(result)
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
