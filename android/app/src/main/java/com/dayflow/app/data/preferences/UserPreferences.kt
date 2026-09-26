package com.dayflow.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dayflow.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dayflow_prefs")

class UserPreferences(private val context: Context) : PreferencesRepository {

    private object PreferencesKeys {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
    }

    override fun getLanguage(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_LANGUAGE] ?: detectSystemLanguage()
        }
    }

    override suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_LANGUAGE] = languageCode
        }
    }

    override fun isSoundEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_SOUND_ENABLED] ?: true
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_SOUND_ENABLED] = enabled
        }
    }

    override fun isHapticEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_HAPTIC_ENABLED] ?: true
        }
    }

    override suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_HAPTIC_ENABLED] = enabled
        }
    }

    override fun getAiProvider(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_AI_PROVIDER] ?: "local"
        }
    }

    override suspend fun setAiProvider(providerName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_AI_PROVIDER] = providerName
        }
    }

    private fun detectSystemLanguage(): String {
        val lang = Locale.getDefault().language
        return when (lang) {
            "de" -> "de"
            "es" -> "es"
            else -> "en"
        }
    }
}
