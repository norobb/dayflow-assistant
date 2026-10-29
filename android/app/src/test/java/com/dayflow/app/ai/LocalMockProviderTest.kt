package com.dayflow.app.ai

import com.dayflow.app.domain.model.SourceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalMockProviderTest {

    @Test
    fun testAnalyzeSanitizesExtractedItemTitleAndBoundsLength() {
        runBlocking {
            val provider = LocalMockProvider()
            val longTitleWithControlChars = "Remind me to buy\u0000 milk\u0007 " + "x".repeat(400)

            val result = provider.analyze(
                type = SourceType.CUSTOM,
                rawInput = longTitleWithControlChars
            )

            assertEquals(1, result.reminders.size)
            val reminder = result.reminders[0]
            assertEquals(300, reminder.title.length)
            assertEquals("Remind me to buy milk " + "x".repeat(278), reminder.title)
        }
    }

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
