package com.dayflow.app.ui.reminders

import org.junit.Assert.assertEquals
import org.junit.Test

class RemindersViewModelTest {

    @Test
    fun testSanitizeReminderFieldStripsControlCharacters() {
        val raw = "Call\u0000 Mom\u0007"
        val clean = RemindersViewModel.sanitizeReminderField(raw)
        assertEquals("Call Mom", clean)
    }

    @Test
    fun testSanitizeReminderFieldTruncatesLongInput() {
        val longString = "A".repeat(500)
        val clean = RemindersViewModel.sanitizeReminderField(longString, maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("A".repeat(300), clean)
    }

    @Test
    fun testSanitizeReminderFieldHandlesNullAndBlank() {
        assertEquals("", RemindersViewModel.sanitizeReminderField(null))
        assertEquals("", RemindersViewModel.sanitizeReminderField(""))
        assertEquals("", RemindersViewModel.sanitizeReminderField("   "))
        assertEquals("", RemindersViewModel.sanitizeReminderField("\u0000\u0007"))
    }
}
