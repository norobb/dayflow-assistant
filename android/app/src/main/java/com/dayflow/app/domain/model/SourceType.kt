package com.dayflow.app.domain.model

enum class SourceType(val identifier: String) {
    MESSAGE("message"),
    SCREENSHOT("screenshot"),
    PDF("pdf"),
    VOICE("voice"),
    CUSTOM("custom"),
    DEFAULT("default");

    companion object {
        fun fromIdentifier(value: String): SourceType {
            return entries.find { it.identifier.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}
