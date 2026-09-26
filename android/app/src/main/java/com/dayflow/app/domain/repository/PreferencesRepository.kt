package com.dayflow.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(languageCode: String)

    fun isSoundEnabled(): Flow<Boolean>
    suspend fun setSoundEnabled(enabled: Boolean)

    fun isHapticEnabled(): Flow<Boolean>
    suspend fun setHapticEnabled(enabled: Boolean)

    fun getAiProvider(): Flow<String>
    suspend fun setAiProvider(providerName: String)
}
