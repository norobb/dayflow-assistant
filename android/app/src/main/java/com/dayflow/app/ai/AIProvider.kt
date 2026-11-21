package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType

/**
 * Everything a provider may use besides the input itself: the user's language,
 * "now" (so relative dates resolve), and their personal vocabulary.
 */
data class AnalysisContext(
    val language: String = "en",
    val now: Long = System.currentTimeMillis(),
    /** Personal vocabulary: term -> meaning (e.g. "Büro" -> "Hauptstraße 5, Berlin"). */
    val aliases: Map<String, String> = emptyMap(),
    /** Frequent contact names, to keep them verbatim. */
    val contacts: List<String> = emptyList(),
    /** Endpoint for OpenAI-compatible providers. */
    val baseUrl: String = ""
)

interface AIProvider {
    val name: String
    suspend fun analyze(
        type: SourceType,
        rawInput: String? = null,
        mediaBytes: ByteArray? = null,
        mimeType: String? = null,
        apiKey: String = "",
        modelOverride: String = "",
        context: AnalysisContext = AnalysisContext()
    ): DayflowAnalysisResult
}
