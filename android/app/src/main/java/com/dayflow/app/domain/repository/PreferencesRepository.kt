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

    /** Passive screen observation (ambient understanding). Opt-in. */
    fun isScreenObservationEnabled(): Flow<Boolean>
    suspend fun setScreenObservationEnabled(enabled: Boolean)

    /** Floating "understand this screen" bubble over other apps. */
    fun isScreenBubbleEnabled(): Flow<Boolean>
    suspend fun setScreenBubbleEnabled(enabled: Boolean)

    /** Generic typed preferences for feature settings (keys in [PrefKeys]). */
    fun stringPref(key: String, default: String = ""): Flow<String>
    suspend fun setStringPref(key: String, value: String)
    fun boolPref(key: String, default: Boolean = false): Flow<Boolean>
    suspend fun setBoolPref(key: String, value: Boolean)
}

object PrefKeys {
    fun apiKey(provider: String) = "api_key_$provider"
    fun model(provider: String) = "model_$provider"
    const val OPENAI_BASE_URL = "base_url_openai"
    /** Optional gateway/proxy endpoint for a provider (blank = official API). */
    fun baseUrl(provider: String) = "base_url_$provider"

    const val CALENDAR_SYNC = "calendar_sync"
    /** System calendar id to write to; blank = Dayflow picks/creates one. */
    const val CALENDAR_ID = "calendar_id"

    const val AUTO_BRIEFING = "auto_briefing"
    const val BRIEFING_TIME = "briefing_time"
    const val AUTO_EVENT_REMINDER = "auto_event_reminder"
    /** Minutes before an event, as text ("30"). */
    const val EVENT_REMINDER_LEAD = "event_reminder_lead"
    const val AUTO_NOTIFICATION_SUGGESTIONS = "auto_notification_suggestions"
    const val AUTO_CLEANUP = "auto_cleanup"
    const val AUTO_LEARN_CONTACTS = "auto_learn_contacts"
    /** Accent color chosen in the intro, as RRGGBB hex; blank = Dayflow burgundy. */
    const val ACCENT_COLOR = "accent_color"
    /** No-root mode: Dayflow chips next to notifications in the shade (accessibility). */
    const val SHADE_CHIPS = "shade_chips"
}
