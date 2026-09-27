package com.dayflow.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dayflow.app.core.designsystem.AppearanceMode
import com.dayflow.app.core.designsystem.DesignSystemMode
import com.dayflow.app.core.sound.SoundAndHaptics
import com.dayflow.app.core.update.UpdateManager
import com.dayflow.app.core.update.UpdateStatus
import com.dayflow.app.domain.repository.DayflowRepository
import com.dayflow.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val language: String = "en",
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val designSystemMode: DesignSystemMode = DesignSystemMode.DAYFLOW_SIGNATURE,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val aiProvider: String = "gemini",
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.6-flash",
    val isTutorialCompleted: Boolean = true
)

class SettingsViewModel(
    context: Context,
    private val repository: DayflowRepository,
    private val preferencesRepository: PreferencesRepository,
    private val soundAndHaptics: SoundAndHaptics
) : ViewModel() {

    private val updateManager = UpdateManager(context)

    val updateStatus: StateFlow<UpdateStatus> = updateManager.updateStatus

    val appearanceMode: StateFlow<AppearanceMode> = preferencesRepository.getAppearanceMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceMode.SYSTEM)

    val designSystemMode: StateFlow<DesignSystemMode> = preferencesRepository.getDesignSystemMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DesignSystemMode.DAYFLOW_SIGNATURE)

    val isTutorialCompleted: StateFlow<Boolean> = preferencesRepository.isTutorialCompleted()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.getLanguage(),
        preferencesRepository.getAppearanceMode(),
        preferencesRepository.getDesignSystemMode(),
        preferencesRepository.isSoundEnabled(),
        preferencesRepository.isHapticEnabled()
    ) { lang, appearance, design, sound, haptic ->
        Tuple5(lang, appearance, design, sound, haptic)
    }.combine(
        combine(
            preferencesRepository.getAiProvider(),
            preferencesRepository.getGeminiApiKey(),
            preferencesRepository.getGeminiModel(),
            preferencesRepository.isTutorialCompleted()
        ) { provider, apiKey, model, tutorial ->
            Tuple4(provider, apiKey, model, tutorial)
        }
    ) { group1, group2 ->
        SettingsUiState(
            language = group1.t1,
            appearanceMode = group1.t2,
            designSystemMode = group1.t3,
            soundEnabled = group1.t4,
            hapticEnabled = group1.t5,
            aiProvider = group2.t1,
            geminiApiKey = group2.t2,
            geminiModel = group2.t3,
            isTutorialCompleted = group2.t4
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setLanguage(languageCode)
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setAppearanceMode(mode)
        }
    }

    fun setDesignSystemMode(mode: DesignSystemMode) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setDesignSystemMode(mode)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
            soundAndHaptics.playToggle()
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticEnabled(enabled)
            soundAndHaptics.playToggle()
        }
    }

    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            preferencesRepository.setAiProvider(provider)
        }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKey(key)
        }
    }

    fun setGeminiModel(model: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiModel(model)
        }
    }

    fun setTutorialCompleted(completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setTutorialCompleted(completed)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            val currentLang = preferencesRepository.getLanguage().first()
            repository.resetToDefaults(currentLang)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            updateManager.checkForUpdates("releases")
        }
    }

    fun downloadAndInstallUpdate(status: UpdateStatus.UpdateAvailable) {
        viewModelScope.launch {
            soundAndHaptics.playClick()
            updateManager.downloadAndInstallUpdate(status)
        }
    }

    class Factory(
        private val context: Context,
        private val repository: DayflowRepository,
        private val preferencesRepository: PreferencesRepository,
        private val soundAndHaptics: SoundAndHaptics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                context,
                repository,
                preferencesRepository,
                soundAndHaptics
            ) as T
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val t1: A, val t2: B, val t3: C, val t4: D, val t5: E)
private data class Tuple4<A, B, C, D>(val t1: A, val t2: B, val t3: C, val t4: D)
