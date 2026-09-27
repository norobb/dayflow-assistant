package com.dayflow.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dayflow.app.core.designsystem.AppearanceMode
import com.dayflow.app.core.designsystem.DesignSystemMode
import com.dayflow.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dayflow_prefs")

class UserPreferences(private val context: Context) : PreferencesRepository {

    private object PreferencesKeys {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_APPEARANCE = stringPreferencesKey("appearance_mode")
        val KEY_DESIGN_SYSTEM = stringPreferencesKey("design_system_mode")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val KEY_TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
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

    override fun getAppearanceMode(): Flow<AppearanceMode> {
        return context.dataStore.data.map { preferences ->
            val name = preferences[PreferencesKeys.KEY_APPEARANCE] ?: AppearanceMode.SYSTEM.name
            try { AppearanceMode.valueOf(name) } catch (e: Exception) { AppearanceMode.SYSTEM }
        }
    }

    override suspend fun setAppearanceMode(mode: AppearanceMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_APPEARANCE] = mode.name
        }
    }

    override fun getDesignSystemMode(): Flow<DesignSystemMode> {
        return context.dataStore.data.map { preferences ->
            val name = preferences[PreferencesKeys.KEY_DESIGN_SYSTEM] ?: DesignSystemMode.DAYFLOW_SIGNATURE.name
            try { DesignSystemMode.valueOf(name) } catch (e: Exception) { DesignSystemMode.DAYFLOW_SIGNATURE }
        }
    }

    override suspend fun setDesignSystemMode(mode: DesignSystemMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DESIGN_SYSTEM] = mode.name
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
            preferences[PreferencesKeys.KEY_AI_PROVIDER] ?: "gemini"
        }
    }

    override suspend fun setAiProvider(providerName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_AI_PROVIDER] = providerName
        }
    }

    override fun getGeminiApiKey(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_GEMINI_API_KEY] ?: ""
        }
    }

    override suspend fun setGeminiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_GEMINI_API_KEY] = apiKey
        }
    }

    override fun getGeminiModel(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_GEMINI_MODEL] ?: "gemini-3.6-flash"
        }
    }

    override suspend fun setGeminiModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_GEMINI_MODEL] = model
        }
    }

    override fun isTutorialCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.KEY_TUTORIAL_COMPLETED] ?: false
        }
    }

    override suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_TUTORIAL_COMPLETED] = completed
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
