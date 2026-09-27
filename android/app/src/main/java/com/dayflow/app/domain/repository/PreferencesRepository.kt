package com.dayflow.app.domain.repository

import com.dayflow.app.core.designsystem.AppearanceMode
import com.dayflow.app.core.designsystem.DesignSystemMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(languageCode: String)

    fun getAppearanceMode(): Flow<AppearanceMode>
    suspend fun setAppearanceMode(mode: AppearanceMode)

    fun getDesignSystemMode(): Flow<DesignSystemMode>
    suspend fun setDesignSystemMode(mode: DesignSystemMode)

    fun isSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)

    fun isHapticEnabled(): Flow<Boolean>
    suspend fun setHapticEnabled(enabled: Boolean)

    fun getAiProvider(): Flow<String>
    suspend fun setAiProvider(providerName: String)

    fun getGeminiApiKey(): Flow<String>
    suspend fun setGeminiApiKey(apiKey: String)

    fun getGeminiModel(): Flow<String>
    suspend fun setGeminiModel(model: String)

    fun isTutorialCompleted(): Flow<Boolean>
    suspend fun setTutorialCompleted(completed: Boolean)
}
