package com.dayflow.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
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
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = DayflowTask(
                id = "tk-${System.currentTimeMillis()}",
                title = title.trim(),
                completed = false,
                dueDate = dueDate?.trim()?.ifBlank { null },
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
