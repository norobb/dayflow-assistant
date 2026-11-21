package com.dayflow.app.ai

import android.util.Base64
import com.dayflow.app.BuildConfig
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiProvider : AIProvider {
    override val name: String = "Gemini Multi-Modal API"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String,
        context: AnalysisContext
    ): DayflowAnalysisResult = withContext(Dispatchers.IO) {
        val rawKey = apiKey.ifBlank {
            try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: "" } catch (e: Exception) { "" }
        }
        val effectiveKey = sanitizeApiKey(rawKey)
        if (effectiveKey.isBlank()) {
            return@withContext CloudPrompt.error(type, rawInput, name, "Gemini API key missing. Please configure it in Settings.")
        }
        val targetModel = sanitizeModelName(modelOverride)
        try {
            val parts = JSONArray()
            if (!rawInput.isNullOrBlank()) parts.put(JSONObject().put("text", rawInput))
            if (mediaBytes != null && mediaBytes.isNotEmpty()) {
                val effectiveMime = mimeType ?: when (type) {
                    SourceType.SCREENSHOT -> "image/png"
                    SourceType.PDF -> "application/pdf"
                    SourceType.VOICE -> "audio/mp4"
                    else -> "application/octet-stream"
                }
                parts.put(JSONObject().put("inlineData", JSONObject()
                    .put("mimeType", effectiveMime)
                    .put("data", Base64.encodeToString(mediaBytes, Base64.NO_WRAP))))
            }
            if (parts.length() == 0) parts.put(JSONObject().put("text", "Organize my day"))
            val body = JSONObject()
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", CloudPrompt.system(context)))))
                .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
                .put("generationConfig", JSONObject().put("temperature", 0.2).put("responseMimeType", "application/json").apply {
                    // Extraction needs little reasoning; low thinking roughly halves latency on Gemini 3.x.
                    if (targetModel.startsWith("gemini-3")) put("thinkingConfig", JSONObject().put("thinkingLevel", "low"))
                })
            val (code, text) = CloudPrompt.post(
                "${endpoint(context.baseUrl)}/v1beta/models/$targetModel:generateContent",
                body, mapOf("x-goog-api-key" to effectiveKey)
            )
            if (code != 200) {
                return@withContext CloudPrompt.error(type, rawInput, name, sanitizeError("Gemini API error ($code): ${text.take(300)}", effectiveKey))
            }
            val answer = JSONObject(text).optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                ?: return@withContext CloudPrompt.error(type, rawInput, name, "Gemini returned an empty result.")
            CloudPrompt.parse(answer, type, rawInput, context)
        } catch (e: Exception) {
            CloudPrompt.error(type, rawInput, name, sanitizeError(e.localizedMessage, effectiveKey))
        }
    }

    /**
     * Raw text completion (no structured DayflowAnalysisResult). Used by the
     * Screen Agent to ask the model for the next on-screen action as JSON.
     */
    suspend fun complete(prompt: String, apiKey: String, modelOverride: String = "", baseUrl: String = ""): String =
        withContext(Dispatchers.IO) {
            val rawKey = apiKey.ifBlank {
                try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: "" } catch (e: Exception) { "" }
            }
            val effectiveKey = sanitizeApiKey(rawKey)
            if (effectiveKey.isBlank()) return@withContext ""

            val targetModel = sanitizeModelName(modelOverride.ifBlank { "gemini-2.0-flash" })
            try {
                val requestBody = JSONObject()
                val contentsArray = JSONArray()
                val content = JSONObject()
                val partsArray = JSONArray()
                val part = JSONObject().put("text", prompt)
                partsArray.put(part)
                content.put("parts", partsArray)
                contentsArray.put(content)
                requestBody.put("contents", contentsArray)

                val genConfig = JSONObject()
                    .put("temperature", 0.1)
                    .put("responseMimeType", "application/json")
                requestBody.put("generationConfig", genConfig)

                val endpointUrl = "${endpoint(baseUrl)}/v1beta/models/$targetModel:generateContent"
                val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", effectiveKey)
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 120000
                }
                OutputStreamWriter(connection.outputStream).use { it.write(requestBody.toString()); it.flush() }
                if (connection.responseCode != 200) return@withContext ""
                val responseText = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val json = JSONObject(responseText)
                val candidates = json.optJSONArray("candidates") ?: return@withContext ""
                if (candidates.length() == 0) return@withContext ""
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: return@withContext ""
                if (parts.length() == 0) return@withContext ""
                parts.getJSONObject(0).optString("text", "")
            } catch (e: Exception) {
                ""
            }
        }

    companion object {
        const val DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com"
        fun endpoint(override: String) = override.trim().trimEnd('/').removeSuffix("/v1beta").ifBlank { DEFAULT_ENDPOINT }

        /**
         * SECURITY ENHANCEMENT: Sanitize API key input by stripping whitespace, control
         * characters, and line break characters (\r, \n) to prevent HTTP request splitting,
         * header/query parameter injection, and malformed URL exceptions.
         */
        fun sanitizeApiKey(rawKey: String?, maxLength: Int = 256): String {
            if (rawKey.isNullOrBlank()) return ""
            return rawKey
                .replace(Regex("[\\x00-\\x20\\x7F-\\xFF]"), "")
                .trim()
                .take(maxLength)
        }

        /**
         * SECURITY ENHANCEMENT: Validate and sanitize model identifier format to prevent
         * URL path traversal or query parameter injection attacks. Defaults safely to gemini-3.5-flash.
         */
        fun sanitizeModelName(rawModel: String?): String {
            if (rawModel.isNullOrBlank()) return "gemini-3.5-flash"
            val trimmed = rawModel.trim()
            return if (trimmed.matches(Regex("^[a-zA-Z0-9._-]+$"))) trimmed else "gemini-3.5-flash"
        }

        /**
         * SECURITY ENHANCEMENT: Sanitize AI response output text fields by stripping non-printable
         * control characters (\x00-\x1F\x7F) and truncating strings to prevent UI layout breakage,
         * control character injection, or IPC buffer overflow.
         */
        fun sanitizeText(value: String?, fallback: String = "", maxLength: Int = 500): String {
            if (value.isNullOrBlank()) return fallback
            val clean = value
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
                .take(maxLength)
            return clean.ifEmpty { fallback }
        }

        /**
         * SECURITY ENHANCEMENT: Redact API keys and URL key query params from error messages
         * to prevent exposing credentials in UI cards or logs.
         */
        fun sanitizeError(message: String?, apiKey: String): String {
            if (message.isNullOrBlank()) return "Unknown error"
            var sanitized = message
            if (apiKey.isNotBlank()) {
                sanitized = sanitized.replace(apiKey, "[REDACTED]")
            }
            return sanitized.replace(Regex("key=[a-zA-Z0-9._-]+"), "key=[REDACTED]")
        }
    }
}
