package com.dayflow.app.ui.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemindersViewModelTest {

    @Test
    fun testSanitizeReminderInputStripsControlCharacters() {
        val raw = "Water\u0000 plants\u0007"
        val clean = RemindersViewModel.sanitizeReminderInput(raw)
        assertEquals("Water plants", clean)
    }

    @Test
    fun testSanitizeReminderInputTruncatesLongInput() {
        val longString = "R".repeat(300)
        val clean = RemindersViewModel.sanitizeReminderInput(longString, maxLength = 200)
        assertEquals(200, clean?.length)
        assertEquals("R".repeat(200), clean)
    }

    @Test
    fun testSanitizeReminderInputHandlesNullAndBlank() {
        assertNull(RemindersViewModel.sanitizeReminderInput(null))
        assertNull(RemindersViewModel.sanitizeReminderInput(""))
        assertNull(RemindersViewModel.sanitizeReminderInput("   "))
        assertNull(RemindersViewModel.sanitizeReminderInput("\u0000\u0007"))
    }

    @Test
    fun testSanitizeReminderInputPreservesValidInput() {
        val valid = "Call doctor at 16:00"
        assertEquals(valid, RemindersViewModel.sanitizeReminderInput(valid))
    }
}
