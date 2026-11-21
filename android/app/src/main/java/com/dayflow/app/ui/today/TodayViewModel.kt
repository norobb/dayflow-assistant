package com.dayflow.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.DayflowApplication
import com.dayflow.app.core.util.DayInsights
import com.dayflow.app.data.local.ContextEntryEntity
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.PrefKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/** One line of "Today, understood.": something Dayflow organized today. */
data class UnderstoodItem(val id: String, val kind: Kind, val title: String, val sourceType: SourceType, val at: Long) {
    enum class Kind { EVENT, TASK, REMINDER }
}

data class TodayUiState(
    val events: List<DayflowEvent> = emptyList(),
    val tasks: List<DayflowTask> = emptyList(),
    val reminders: List<DayflowReminder> = emptyList(),
    val understood: List<UnderstoodItem> = emptyList(),
    val insight: DayflowInsight? = null,
    val language: String = "en",
    val isAnalyzing: Boolean = false,
    val pendingVerification: DayflowAnalysisResult? = null,
    val failedCalendarEvents: List<DayflowEvent> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(private val app: DayflowApplication) : ViewModel() {

    private val repository = app.repository
    private val prefs = app.preferencesRepository
    private val sound = app.soundAndHaptics

    private val isAnalyzing = MutableStateFlow(false)
    private val pendingVerification = MutableStateFlow<DayflowAnalysisResult?>(null)
    private val failedCalendarEvents = MutableStateFlow<List<DayflowEvent>>(emptyList())
    private val insightSnoozedUntil = MutableStateFlow(0L)
    private val routines = MutableStateFlow<List<ContextEntryEntity>>(emptyList())

    /** Live device-calendar events (Google Calendar etc.) for today and the next 7 days. */
    private val calendarEvents = prefs.boolPref(PrefKeys.CALENDAR_SYNC, true).flatMapLatest { on ->
        if (!on) flowOf(emptyList())
        else combine(app.calendarService.changes(), repository.observeEvents()) { _, stored ->
            val start = startOfToday()
            app.calendarService.readEvents(start, start + 8 * DAY, stored.mapNotNull { it.calendarEventId }.toSet())
        }.flowOn(Dispatchers.IO)
    }

    private val timeline = combine(repository.observeEvents(), calendarEvents, routines) { stored, external, routineEntries ->
        val start = startOfToday()
        // Past days drop off the timeline; undated items stay until removed.
        (stored.filter { (it.startMillis ?: Long.MAX_VALUE) >= start } + external + DayInsights.routinesToday(routineEntries))
            .sortedWith(compareBy<DayflowEvent> { it.startMillis ?: Long.MAX_VALUE }.thenBy { it.time })
    }

    val uiState: StateFlow<TodayUiState> = combine(
        timeline,
        repository.observeTasks(),
        repository.observeReminders(),
        combine(repository.observeInsight(), insightSnoozedUntil, prefs.getLanguage()) { a, b, c -> Triple(a, b, c) },
        combine(isAnalyzing, pendingVerification, failedCalendarEvents) { a, b, c -> Triple(a, b, c) }
    ) { events, tasks, reminders, (analysisInsight, snoozedUntil, language), (analyzing, verification, failed) ->
        val now = System.currentTimeMillis()
        val insight = when {
            snoozedUntil > now -> null
            analysisInsight.visible && analysisInsight.title.isNotBlank() -> analysisInsight
            else -> DayInsights.contextInsight(events, tasks, language, now)
        }
        TodayUiState(
            events = events,
            tasks = tasks,
            reminders = reminders,
            understood = understoodToday(events, tasks, reminders),
            insight = insight,
            language = language,
            isAnalyzing = analyzing,
            pendingVerification = verification,
            failedCalendarEvents = failed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    init {
        viewModelScope.launch {
            app.personalRepository.observeContext()
                .map { list -> list.filter { it.kind == ContextEntryEntity.KIND_ROUTINE } }
                .collect { routines.value = it }
        }
    }

    private fun understoodToday(events: List<DayflowEvent>, tasks: List<DayflowTask>, reminders: List<DayflowReminder>): List<UnderstoodItem> {
        val start = startOfToday()
        return (events.filter { !it.external && it.createdAt >= start && it.sourceType != SourceType.DEFAULT }
            .map { UnderstoodItem(it.id, UnderstoodItem.Kind.EVENT, it.title, it.sourceType, it.createdAt) } +
            tasks.filter { it.createdAt >= start }.map { UnderstoodItem(it.id, UnderstoodItem.Kind.TASK, it.title, it.sourceType, it.createdAt) } +
            reminders.filter { it.createdAt >= start }.map { UnderstoodItem(it.id, UnderstoodItem.Kind.REMINDER, it.title, it.sourceType, it.createdAt) })
            .sortedByDescending { it.at }
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            sound.playToggle()
            repository.toggleTask(taskId)
        }
    }

    fun snoozeInsight() {
        sound.playClick()
        insightSnoozedUntil.value = System.currentTimeMillis() + 3 * 3_600_000L
        viewModelScope.launch { repository.snoozeInsight() }
    }

    fun startAnalysis(type: SourceType, rawInput: String? = null, mediaBytes: ByteArray? = null, mimeType: String? = null) {
        viewModelScope.launch {
            isAnalyzing.value = true
            sound.playAnalyze()
            val result = runCatching { app.analyzer.analyze(type, rawInput, mediaBytes, mimeType) }
                .getOrElse { e ->
                    DayflowAnalysisResult(sourceType = type, hasError = true, errorMessage = e.message, summaryText = e.message.orEmpty())
                }
            isAnalyzing.value = false
            pendingVerification.value = result
            if (result.hasError || result.events.isEmpty() && result.tasks.isEmpty() && result.reminders.isEmpty()) sound.playError()
            else sound.playSuccess()
        }
    }

    fun dismissVerification() {
        pendingVerification.value = null
    }

    fun acceptVerification(result: DayflowAnalysisResult) {
        viewModelScope.launch {
            val report = app.itemCommitter.commit(result)
            failedCalendarEvents.value = report.calendarFailed
            sound.playSuccess()
            pendingVerification.value = null
        }
    }

    /** Calendar permission was missing: try again, else hand over to the calendar app. */
    fun retryCalendarInsertion() {
        viewModelScope.launch {
            val still = app.itemCommitter.retryCalendar(failedCalendarEvents.value)
            still.forEach { app.calendarService.launchCalendarIntent(it) }
            failedCalendarEvents.value = emptyList()
        }
    }

    fun dismissCalendarWarning() {
        failedCalendarEvents.value = emptyList()
    }

    fun deleteEvent(event: DayflowEvent) {
        if (event.external) return
        viewModelScope.launch { app.itemCommitter.deleteEvent(event) }
    }

    fun dismissReminder(id: String) {
        viewModelScope.launch {
            sound.playToggle()
            repository.dismissReminder(id)
            app.notificationHelper.cancelReminder(id)
        }
    }

    fun resetDay() {
        viewModelScope.launch {
            repository.resetToDefaults(prefs.getLanguage().first())
            insightSnoozedUntil.value = 0
        }
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    class Factory(private val app: DayflowApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TodayViewModel(app) as T
    }

    companion object {
        private const val DAY = 24 * 3_600_000L
    }
}
