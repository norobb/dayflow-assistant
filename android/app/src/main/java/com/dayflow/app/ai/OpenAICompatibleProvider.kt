package com.dayflow.app.ai

import android.util.Base64
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Any OpenAI-compatible Chat Completions endpoint: OpenAI, OpenRouter, Groq,
 * or a local Ollama / LM Studio server (e.g. http://10.0.2.2:11434/v1).
 * Images are sent inline; PDFs and audio arrive here already converted to text.
 */
class OpenAICompatibleProvider : AIProvider {
    override val name: String = "OpenAI-compatible"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String,
        context: AnalysisContext
    ): DayflowAnalysisResult = withContext(Dispatchers.IO) {
        val base = context.baseUrl.trim().trimEnd('/').ifBlank { DEFAULT_BASE_URL }
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            return@withContext CloudPrompt.error(type, rawInput, name, "Invalid base URL")
        }
        val key = GeminiProvider.sanitizeApiKey(apiKey)
        val model = GeminiProvider.sanitizeModelName(modelOverride.ifBlank { DEFAULT_MODEL }).let {
            // sanitizeModelName falls back to a Gemini id; Ollama ids contain ':'.
            if (modelOverride.matches(Regex("^[a-zA-Z0-9._:/-]{1,100}$"))) modelOverride else it
        }
        val user = JSONArray()
        user.put(JSONObject().put("type", "text").put("text", rawInput?.ifBlank { null } ?: "Organize this."))
        if (mediaBytes != null && mimeType?.startsWith("image/") == true) {
            val dataUrl = "data:$mimeType;base64," + Base64.encodeToString(mediaBytes, Base64.NO_WRAP)
            user.put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", dataUrl)))
        }
        val body = JSONObject()
            .put("model", model)
            .put("temperature", 0.1)
            .put("messages", JSONArray()
                .put(JSONObject().put("role", "system").put("content", CloudPrompt.system(context)))
                .put(JSONObject().put("role", "user").put("content", user)))
            .put("response_format", JSONObject().put("type", "json_object"))
        try {
            val headers = if (key.isNotBlank()) mapOf("Authorization" to "Bearer $key") else emptyMap()
            val (code, text) = CloudPrompt.post("$base/chat/completions", body, headers, timeoutMs = 180_000)
            if (code !in 200..299) {
                return@withContext CloudPrompt.error(type, rawInput, name, GeminiProvider.sanitizeError("HTTP $code: ${text.take(300)}", key))
            }
            val content = JSONObject(text).getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content")
            CloudPrompt.parse(content, type, rawInput, context)
        } catch (e: Exception) {
            CloudPrompt.error(type, rawInput, name, GeminiProvider.sanitizeError(e.message, key))
        }
    }

    suspend fun complete(prompt: String, apiKey: String, model: String, baseUrl: String): String? = withContext(Dispatchers.IO) {
        val base = baseUrl.trim().trimEnd('/').ifBlank { DEFAULT_BASE_URL }
        val key = GeminiProvider.sanitizeApiKey(apiKey)
        val body = JSONObject()
            .put("model", model.ifBlank { DEFAULT_MODEL })
            .put("temperature", 0.1)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        runCatching {
            val (code, text) = CloudPrompt.post("$base/chat/completions", body,
                if (key.isNotBlank()) mapOf("Authorization" to "Bearer $key") else emptyMap(), timeoutMs = 180_000)
            if (code !in 200..299) null
            else JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content")
        }.getOrNull()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
        const val DEFAULT_MODEL = "gpt-4o-mini"
    }
}
