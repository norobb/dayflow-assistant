package com.dayflow.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import java.util.UUID
import com.dayflow.app.domain.repository.DayflowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: DayflowRepository,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    val tasks: StateFlow<List<DayflowTask>> = repository.observeTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            soundAndHaptics.playToggle()
            repository.toggleTask(taskId)
        }
    }

    fun addTask(title: String, dueDate: String?) {
        val cleanTitle = sanitizeTaskField(title, maxLength = 300)
        if (cleanTitle.isBlank()) return
        val cleanDueDate = sanitizeTaskField(dueDate, maxLength = 100).ifBlank { null }
        viewModelScope.launch {
            // SECURITY ENHANCEMENT: Use cryptographically secure UUID for unique task identifier
            val task = DayflowTask(
                id = "tk-${UUID.randomUUID()}",
                title = cleanTitle,
                completed = false,
                dueDate = cleanDueDate,
                sourceType = SourceType.CUSTOM
            )
            repository.insertTask(task)
            soundAndHaptics.playSuccess()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            soundAndHaptics.performHaptic(10)
        }
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize task input fields (title, dueDate) by stripping
         * non-printable control characters (\x00-\x1F\x7F) and enforcing string length limits
         * to prevent IPC buffer overflow and UI rendering crashes.
         */
        fun sanitizeTaskField(value: String?, maxLength: Int = 300): String {
            if (value.isNullOrBlank()) return ""
            return value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
        }
    }

    class Factory(
        private val repository: DayflowRepository,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(repository, soundAndHaptics) as T
        }
    }
}
