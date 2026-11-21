package com.dayflow.app.ai

import com.dayflow.app.data.repository.PersonalRepository
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.domain.repository.PrefKeys
import com.dayflow.app.domain.repository.PreferencesRepository
import com.dayflow.app.integrations.media.ContentExtractor
import kotlinx.coroutines.flow.first

/**
 * Single entry point for understanding input. Reads the chosen provider from
 * settings, converts media to text on the device where the provider needs it,
 * falls back to on-device analysis when a cloud call fails, and records what
 * happened in the transparency log.
 */
class DayflowAnalyzer(
    private val prefs: PreferencesRepository,
    private val personal: PersonalRepository,
    private val extractor: ContentExtractor,
    private val providers: Map<String, AIProvider> = mapOf(
        LOCAL to LocalProvider(),
        GEMINI to GeminiProvider(),
        OPENAI to OpenAICompatibleProvider(),
        CLAUDE to ClaudeProvider()
    )
) {
    suspend fun analyze(
        type: SourceType,
        rawInput: String? = null,
        mediaBytes: ByteArray? = null,
        mimeType: String? = null
    ): DayflowAnalysisResult {
        val providerId = prefs.getAiProvider().first().lowercase().let { if (it in providers) it else LOCAL }
        val language = prefs.getLanguage().first()
        val apiKey = if (providerId == GEMINI) prefs.getGeminiApiKey().first() else prefs.stringPref(PrefKeys.apiKey(providerId)).first()
        val model = if (providerId == GEMINI) prefs.getGeminiModel().first() else prefs.stringPref(PrefKeys.model(providerId)).first()
        val baseUrl = prefs.stringPref(PrefKeys.baseUrl(providerId)).first()
        val ctx = AnalysisContext(
            language = language,
            aliases = personal.aliases(),
            contacts = personal.contacts(),
            baseUrl = baseUrl
        )
        val cloudReady = when (providerId) {
            LOCAL -> false
            OPENAI -> apiKey.isNotBlank() || baseUrl.isNotBlank()
            CLAUDE -> baseUrl.isNotBlank()
            else -> apiKey.isNotBlank()
        }
        val effective = if (cloudReady) providerId else LOCAL

        // Convert media to text on the device when the provider cannot read it natively.
        val mime = mimeType.orEmpty()
        val needsText = when (effective) {
            LOCAL, OPENAI -> true
            CLAUDE -> mime.startsWith("audio/")
            else -> false
        }
        var extracted: String? = null
        if (mediaBytes != null && needsText) {
            extracted = when {
                mime.startsWith("image/") -> extractor.imageText(mediaBytes)
                mime == "application/pdf" -> extractor.pdfText(mediaBytes)
                mime.startsWith("audio/") -> extractor.audioText(mediaBytes, language)
                else -> null
            }
            if (extracted == null && mime.startsWith("audio/") && effective == LOCAL) {
                return DayflowAnalysisResult(
                    sourceType = type,
                    summaryText = noTranscript(language, extractor.lastAudioError),
                    hasError = true,
                    errorMessage = noTranscript(language, extractor.lastAudioError),
                    confidence = 0f
                )
            }
        }
        val text = listOfNotNull(rawInput?.takeIf { it.isNotBlank() }, extracted).joinToString("\n")
        // Media the provider can read itself is sent as-is; otherwise only the extracted text leaves.
        val sendMedia = if (needsText && effective != OPENAI) null else mediaBytes

        val provider = providers.getValue(effective)
        var result = provider.analyze(type, text, sendMedia, mimeType, apiKey, model, ctx)
        if (effective == LOCAL) {
            personal.log(PersonalRepository.LOG_ANALYSIS_LOCAL, "${type.identifier}: on-device (${result.events.size}/${result.tasks.size}/${result.reminders.size})")
        } else {
            personal.log(PersonalRepository.LOG_ANALYSIS_CLOUD, "${type.identifier} → ${provider.name}" + if (sendMedia != null) " (+${mime})" else "")
        }
        if (result.hasError && effective != LOCAL) {
            // Never leave the user empty-handed: fall back to on-device understanding.
            val local = providers.getValue(LOCAL).analyze(
                type,
                listOfNotNull(text.ifBlank { null }, if (extracted == null && mediaBytes != null) extractFallback(mime, mediaBytes, language) else null).joinToString("\n"),
                null, null, "", "", ctx
            )
            result = local.copy(
                insight = DayflowInsight(provider.name, (result.errorMessage ?: "") + " — " + fellBack(language), true),
                errorMessage = result.errorMessage
            )
        }
        // Show the user what was read (OCR / transcript) so they can verify it.
        return if (extracted != null) result.copy(inputSnippet = extracted.take(600)) else result
    }

    /**
     * Plain text completion with the chosen cloud provider (used by the screen agent).
     * Returns null when the on-device provider is selected or the call fails.
     */
    suspend fun complete(prompt: String): String? {
        val providerId = prefs.getAiProvider().first().lowercase()
        val apiKey = if (providerId == GEMINI) prefs.getGeminiApiKey().first() else prefs.stringPref(PrefKeys.apiKey(providerId)).first()
        val model = if (providerId == GEMINI) prefs.getGeminiModel().first() else prefs.stringPref(PrefKeys.model(providerId)).first()
        val baseUrl = prefs.stringPref(PrefKeys.baseUrl(providerId)).first()
        val text = when (providerId) {
            GEMINI -> if (apiKey.isBlank()) null else (providers[GEMINI] as GeminiProvider).complete(prompt, apiKey, model, baseUrl)
            OPENAI -> if (apiKey.isBlank() && baseUrl.isBlank()) null else (providers[OPENAI] as OpenAICompatibleProvider).complete(prompt, apiKey, model, baseUrl)
            CLAUDE -> if (baseUrl.isBlank()) null else (providers[CLAUDE] as ClaudeProvider).complete(prompt, apiKey, model, baseUrl)
            else -> null
        }
        if (text != null) personal.log(PersonalRepository.LOG_ANALYSIS_CLOUD, "screen agent step → $providerId")
        return text?.ifBlank { null }
    }

    private suspend fun extractFallback(mime: String, bytes: ByteArray, language: String): String? = when {
        mime.startsWith("image/") -> extractor.imageText(bytes)
        mime == "application/pdf" -> extractor.pdfText(bytes)
        mime.startsWith("audio/") -> extractor.audioText(bytes, language)
        else -> null
    }

    private fun noTranscript(l: String, error: Int) = if (error == ContentExtractor.AUDIO_LANGUAGE_MISSING) when (l) {
        "de" -> "Das deutsche Sprachpaket für die Offline-Erkennung wird gerade geladen. Bitte versuche es in ein paar Minuten erneut."
        "es" -> "Se está descargando el paquete de idioma español para el reconocimiento sin conexión. Inténtalo de nuevo en unos minutos."
        else -> "The English language pack for offline recognition is downloading. Please try again in a few minutes."
    } else when (l) {
        "de" -> "Die Sprachaufnahme konnte auf diesem Gerät nicht transkribiert werden. Prüfe die Spracherkennung oder wähle einen KI-Anbieter mit Audio-Unterstützung."
        "es" -> "No se pudo transcribir la nota de voz en este dispositivo. Revisa el reconocimiento de voz o elige un proveedor de IA con audio."
        else -> "The voice note could not be transcribed on this device. Check speech recognition or choose an AI provider with audio support."
    }

    private fun fellBack(l: String) = when (l) {
        "de" -> "stattdessen auf dem Gerät analysiert."
        "es" -> "analizado en el dispositivo en su lugar."
        else -> "analyzed on device instead."
    }

    companion object {
        const val LOCAL = "local"
        const val GEMINI = "gemini"
        const val OPENAI = "openai"
        const val CLAUDE = "claude"
    }
}
