package com.dayflow.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.core.update.UpdateManager
import com.dayflow.app.core.update.UpdateStatus
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
    val aiProvider: String = "local",
    val updateChannel: String = "releases",
    val updateStatus: UpdateStatus = UpdateStatus.Idle
)

class SettingsViewModel(
    private val context: Context,
    private val repository: DayflowRepository,
    private val preferencesRepository: PreferencesRepository,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    private val updateManager = UpdateManager(context)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferencesRepository.getLanguage(),
            preferencesRepository.isSoundEnabled(),
            preferencesRepository.isHapticEnabled(),
            preferencesRepository.getAiProvider()
        ) { lang, sound, haptic, ai ->
            Tuple4(lang, sound, haptic, ai)
        },
        combine(
            preferencesRepository.getUpdateChannel(),
            updateManager.updateStatus
        ) { channel, status ->
            Pair(channel, status)
        }
    ) { p1, p2 ->
        SettingsUiState(
            language = p1.a,
            isSoundEnabled = p1.b,
            isHapticEnabled = p1.c,
            aiProvider = p1.d,
            updateChannel = p2.first,
            updateStatus = p2.second
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

    fun setUpdateChannel(channel: String) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setUpdateChannel(channel)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            updateManager.checkForUpdates(uiState.value.updateChannel)
        }
    }

    fun downloadAndInstallUpdate(updateInfo: UpdateStatus.UpdateAvailable) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            updateManager.downloadAndInstallUpdate(updateInfo)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            soundAndHaptics.playSuccess()
            repository.resetToDefaults(uiState.value.language)
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    class Factory(
        private val context: Context,
        private val repository: DayflowRepository,
        private val preferencesRepository: PreferencesRepository,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(context, repository, preferencesRepository, soundAndHaptics) as T
        }
    }
}
