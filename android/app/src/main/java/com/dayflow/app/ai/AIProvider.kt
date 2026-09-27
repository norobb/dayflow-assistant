package com.dayflow.app.ai

import com.dayflow.app.domain.model.DayflowAnalysisResult
import com.dayflow.app.domain.model.SourceType

interface AIProvider {
    val name: String
    suspend fun analyze(
        type: SourceType,
        rawInput: String? = null,
        mediaBytes: ByteArray? = null,
        mimeType: String? = null,
        apiKey: String = "",
        modelOverride: String = ""
    ): DayflowAnalysisResult
}
