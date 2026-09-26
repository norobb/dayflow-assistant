package com.dayflow.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.integrations.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repository: DayflowRepository,
    private val notificationHelper: NotificationHelper,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    val reminders: StateFlow<List<DayflowReminder>> = repository.observeReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addReminder(title: String, timeLabel: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val reminder = DayflowReminder(
                id = "rm-${System.currentTimeMillis()}",
                title = title.trim(),
                timeLabel = timeLabel.trim().ifBlank { "Later today" },
                sourceType = SourceType.CUSTOM
            )
            repository.insertReminder(reminder)
            notificationHelper.scheduleReminder(reminder)
            soundAndHaptics.playSuccess()
        }
    }

    fun dismissReminder(reminderId: String) {
        viewModelScope.launch {
            repository.dismissReminder(reminderId)
            soundAndHaptics.performHaptic(10)
        }
    }

    class Factory(
        private val repository: DayflowRepository,
        private val notificationHelper: NotificationHelper,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(repository, notificationHelper, soundAndHaptics) as T
        }
    }
}
