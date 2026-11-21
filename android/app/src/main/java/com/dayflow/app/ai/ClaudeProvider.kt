package com.dayflow.app.ai

import android.util.Base64
import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Any Anthropic Messages API endpoint (Anthropic, Ollama, LiteLLM, other gateways). Images and PDFs are sent natively. */
class ClaudeProvider : AIProvider {
    override val name: String = "Anthropic-compatible"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        mediaBytes: ByteArray?,
        mimeType: String?,
        apiKey: String,
        modelOverride: String,
        context: AnalysisContext
    ): DayflowAnalysisResult = withContext(Dispatchers.IO) {
        val key = GeminiProvider.sanitizeApiKey(apiKey)
        if (context.baseUrl.isBlank()) return@withContext CloudPrompt.error(type, rawInput, name, "Endpoint URL missing")
        val model = validModel(modelOverride)

        val content = JSONArray()
        if (mediaBytes != null && mimeType != null) {
            val data = Base64.encodeToString(mediaBytes, Base64.NO_WRAP)
            val source = JSONObject().put("type", "base64").put("media_type", mimeType).put("data", data)
            when {
                mimeType.startsWith("image/") -> content.put(JSONObject().put("type", "image").put("source", source))
                mimeType == "application/pdf" -> content.put(JSONObject().put("type", "document").put("source", source))
            }
        }
        content.put(JSONObject().put("type", "text").put("text", rawInput?.ifBlank { null } ?: "Organize this."))
        val body = JSONObject()
            .put("model", model)
            .put("max_tokens", 2048)
            .put("system", CloudPrompt.system(context))
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", content)))
        try {
            val (code, text) = CloudPrompt.post(
                "${endpoint(context.baseUrl)}/v1/messages", body,
                headers(key)
            )
            if (code !in 200..299) {
                return@withContext CloudPrompt.error(type, rawInput, name, GeminiProvider.sanitizeError("HTTP $code: ${text.take(300)}", key))
            }
            val blocks = JSONObject(text).getJSONArray("content")
            val answer = (0 until blocks.length()).map { blocks.getJSONObject(it) }
                .filter { it.optString("type") == "text" }.joinToString("") { it.optString("text") }
            CloudPrompt.parse(answer, type, rawInput, context)
        } catch (e: Exception) {
            CloudPrompt.error(type, rawInput, name, GeminiProvider.sanitizeError(e.message, key))
        }
    }

    suspend fun complete(prompt: String, apiKey: String, model: String, baseUrl: String = ""): String? = withContext(Dispatchers.IO) {
        val key = GeminiProvider.sanitizeApiKey(apiKey)
        val body = JSONObject()
            .put("model", validModel(model))
            .put("max_tokens", 512)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        runCatching {
            val (code, text) = CloudPrompt.post("${endpoint(baseUrl)}/v1/messages", body,
                headers(key))
            if (code !in 200..299) null else {
                val blocks = JSONObject(text).getJSONArray("content")
                (0 until blocks.length()).map { blocks.getJSONObject(it) }
                    .filter { it.optString("type") == "text" }.joinToString("") { it.optString("text") }
            }
        }.getOrNull()
    }

    companion object {
        const val DEFAULT_MODEL = "claude-sonnet-5-5"
        const val DEFAULT_ENDPOINT = "https://api.anthropic.com"
        /** Gateway model ids may contain ':' or '/' (e.g. "gemma4-12b:latest", "anthropic/claude-…"). */
        fun validModel(raw: String) = raw.trim().takeIf { it.matches(Regex("^[a-zA-Z0-9._:/-]{1,100}$")) } ?: DEFAULT_MODEL
        fun headers(key: String) = buildMap {
            put("anthropic-version", "2023-06-01")
            if (key.isNotBlank()) put("x-api-key", key)
        }
        fun endpoint(override: String) = override.trim().trimEnd('/').removeSuffix("/v1").ifBlank { DEFAULT_ENDPOINT }
    }
}
