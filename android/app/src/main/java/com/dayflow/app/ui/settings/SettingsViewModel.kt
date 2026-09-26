package com.dayflow.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val language: String = "en",
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val aiProvider: String = "local"
)

class SettingsViewModel(
    private val repository: DayflowRepository,
    private val preferencesRepository: PreferencesRepository,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.getLanguage(),
        preferencesRepository.isSoundEnabled(),
        preferencesRepository.isHapticEnabled(),
        preferencesRepository.getAiProvider()
    ) { language, sound, haptic, provider ->
        SettingsUiState(
            language = language,
            isSoundEnabled = sound,
            isHapticEnabled = haptic,
            aiProvider = provider
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setLanguage(lang)
            repository.resetToDefaults(lang)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
            if (enabled) soundAndHaptics.playClick()
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticEnabled(enabled)
            if (enabled) soundAndHaptics.performHaptic(14)
        }
    }

    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setAiProvider(provider)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            soundAndHaptics.playSuccess()
            repository.resetToDefaults(uiState.value.language)
        }
    }

    class Factory(
        private val repository: DayflowRepository,
        private val preferencesRepository: PreferencesRepository,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, preferencesRepository, soundAndHaptics) as T
        }
    }
}
