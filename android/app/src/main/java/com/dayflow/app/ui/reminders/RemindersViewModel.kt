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
        val cleanTitle = sanitizeReminderInput(title, maxLength = 200) ?: return
        val cleanTime = sanitizeReminderInput(timeLabel, maxLength = 100) ?: "Later today"
        viewModelScope.launch {
            val reminder = DayflowReminder(
                id = "rm-${System.currentTimeMillis()}",
                title = cleanTitle,
                timeLabel = cleanTime,
                sourceType = SourceType.CUSTOM
            )
            repository.insertReminder(reminder)
            notificationHelper.scheduleReminder(reminder)
            soundAndHaptics.playSuccess()
        }
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize reminder text fields (title, timeLabel) by stripping
         * non-printable control characters and truncating long strings to prevent storage
         * or notification payload / rendering DoS attacks.
         */
        fun sanitizeReminderInput(value: String?, maxLength: Int = 200): String? {
            if (value.isNullOrBlank()) return null
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { null }
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
