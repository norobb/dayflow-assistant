package com.dayflow.app.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiProviderTest {

    @Test
    fun testSanitizeModelNameValidIdentifiers() {
        assertEquals("gemini-1.5-pro", GeminiProvider.sanitizeModelName("gemini-1.5-pro"))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("gemini-3.6-flash"))
        assertEquals("custom_model.v1", GeminiProvider.sanitizeModelName("custom_model.v1"))
    }

    @Test
    fun testSanitizeModelNameRejectsPathTraversalAndQueryParams() {
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("../gemini"))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("gemini-1.5?key=123"))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("model/v1"))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("gemini; rm -rf /"))
    }

    @Test
    fun testSanitizeModelNameHandlesNullAndBlank() {
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName(null))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName(""))
        assertEquals("gemini-3.6-flash", GeminiProvider.sanitizeModelName("   "))
    }

    @Test
    fun testSanitizeErrorRedactsApiKey() {
        val apiKey = "AIzaSyABC123456789SecretKey"
        val rawMessage = "Failed to call endpoint with key AIzaSyABC123456789SecretKey: 403 Forbidden"
        val sanitized = GeminiProvider.sanitizeError(rawMessage, apiKey)
        assertEquals("Failed to call endpoint with key [REDACTED]: 403 Forbidden", sanitized)
    }

    @Test
    fun testSanitizeErrorRedactsUrlKeyQueryParam() {
        val apiKey = "secret_key_999"
        val urlError = "java.io.FileNotFoundException: https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=AIzaSyXYZ_987654321"
        val sanitized = GeminiProvider.sanitizeError(urlError, apiKey)
        assertEquals("java.io.FileNotFoundException: https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=[REDACTED]", sanitized)
    }

    @Test
    fun testSanitizeErrorHandlesNullAndBlank() {
        assertEquals("Unknown error", GeminiProvider.sanitizeError(null, "key"))
        assertEquals("Unknown error", GeminiProvider.sanitizeError("", "key"))
    }

    @Test
    fun testSanitizeApiKeyStripsWhitespaceNewlinesAndControlChars() {
        val rawWithNewlinesAndSpaces = "  AIzaSyABC123 \r\n\t SecretKey_456  "
        assertEquals("AIzaSyABC123SecretKey_456", GeminiProvider.sanitizeApiKey(rawWithNewlinesAndSpaces))
    }

    @Test
    fun testSanitizeApiKeyHandlesNullAndBlank() {
        assertEquals("", GeminiProvider.sanitizeApiKey(null))
        assertEquals("", GeminiProvider.sanitizeApiKey(""))
        assertEquals("", GeminiProvider.sanitizeApiKey("   \r\n  "))
    }

    @Test
    fun testSanitizeApiKeyEnforcesLengthLimit() {
        val longKey = "A".repeat(300)
        assertEquals("A".repeat(256), GeminiProvider.sanitizeApiKey(longKey))
    }

    @Test
    fun testSanitizeTextStripsControlCharacters() {
        val raw = "Doctor\u0000 Appointment\u0007 at 14:30"
        val clean = GeminiProvider.sanitizeText(raw, "Fallback", 300)
        assertEquals("Doctor Appointment at 14:30", clean)
    }

    @Test
    fun testSanitizeTextTruncatesLongInput() {
        val longTitle = "T".repeat(500)
        val clean = GeminiProvider.sanitizeText(longTitle, "Fallback", 100)
        assertEquals(100, clean.length)
        assertEquals("T".repeat(100), clean)
    }

    @Test
    fun testSanitizeTextHandlesNullBlankAndControlOnly() {
        assertEquals("Default Event", GeminiProvider.sanitizeText(null, "Default Event", 300))
        assertEquals("Default Event", GeminiProvider.sanitizeText("", "Default Event", 300))
        assertEquals("Default Event", GeminiProvider.sanitizeText("   ", "Default Event", 300))
        assertEquals("Default Event", GeminiProvider.sanitizeText("\u0000\u0007", "Default Event", 300))
    }
}
