package com.dayflow.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.SourceType
import java.util.UUID
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
        val cleanTitle = sanitizeReminderField(title, maxLength = 300)
        if (cleanTitle.isBlank()) return
        val cleanTime = sanitizeReminderField(timeLabel, maxLength = 100).ifBlank { "Later today" }
        viewModelScope.launch {
            // SECURITY ENHANCEMENT: Use cryptographically secure UUID for unique reminder identifier
            val reminder = DayflowReminder(
                id = "rm-${UUID.randomUUID()}",
                title = cleanTitle,
                timeLabel = cleanTime,
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

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize reminder fields (title, timeLabel) by stripping
         * non-printable control characters (\x00-\x1F\x7F) and enforcing string length limits
         * to prevent IPC transaction failure and notification rendering crashes.
         */
        fun sanitizeReminderField(value: String?, maxLength: Int = 300): String {
            if (value.isNullOrBlank()) return ""
            return value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
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
