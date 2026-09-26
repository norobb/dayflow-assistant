package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType

class DayflowAnalyzer(
    private val localProvider: AIProvider = LocalMockProvider(),
    private val geminiProvider: AIProvider = GeminiProvider()
) {
    suspend fun analyze(
        providerName: String,
        type: SourceType,
        rawInput: String? = null
    ): DayflowAnalysisResult {
        val provider = if (providerName.equals("gemini", ignoreCase = true)) {
            geminiProvider
        } else {
            localProvider
        }
        return provider.analyze(type, rawInput)
    }
}
