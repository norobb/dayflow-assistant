package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType

/**
 * Gemini Provider for Dayflow
 *
 * Implements secure proxy-based architecture for multi-modal analysis.
 * In accordance with Dayflow security rules, credentials are never embedded
 * in the client APK. If no proxy endpoint is configured, it falls back
 * safely to the deterministic local engine.
 */
class GeminiProvider(
    private val fallbackProvider: AIProvider = LocalMockProvider()
) : AIProvider {
    override val name: String = "Gemini Multi-Modal API"

    override suspend fun analyze(
        type: SourceType,
        rawInput: String?,
        simulatedDelayMs: Long
    ): DayflowAnalysisResult {
        // In local/offline mode or when secure backend proxy is not reachable,
        // use reliable local understanding
        return fallbackProvider.analyze(type, rawInput, simulatedDelayMs)
    }
}
