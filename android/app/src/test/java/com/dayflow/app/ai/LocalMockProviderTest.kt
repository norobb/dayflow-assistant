package com.dayflow.app.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalMockProviderTest {

    @Test
    fun testSanitizeInputStripsControlCharactersAndPreservesNewlines() {
        val rawInput = "Meeting with\u0000 Bob\u0007\nLine 2\tTabbed"
        val sanitized = LocalMockProvider.sanitizeInput(rawInput)
        assertEquals("Meeting with Bob\nLine 2\tTabbed", sanitized)
    }

    @Test
    fun testSanitizeInputCapsMaxLength() {
        val longInput = "a".repeat(1500)
        val sanitized = LocalMockProvider.sanitizeInput(longInput, 1000)
        assertEquals(1000, sanitized.length)
    }

    @Test
    fun testSanitizeInputHandlesNullAndBlank() {
        assertEquals("", LocalMockProvider.sanitizeInput(null))
        assertEquals("", LocalMockProvider.sanitizeInput("   "))
        assertEquals("", LocalMockProvider.sanitizeInput("\u0000\u0007"))
    }
}
