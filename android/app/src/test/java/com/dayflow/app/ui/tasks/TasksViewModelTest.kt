package com.dayflow.app.ui.tasks

import org.junit.Assert.assertEquals
import org.junit.Test

class TasksViewModelTest {

    @Test
    fun testSanitizeTaskFieldStripsControlCharacters() {
        val raw = "Buy\u0000 groceries\u0007"
        val clean = TasksViewModel.sanitizeTaskField(raw)
        assertEquals("Buy groceries", clean)
    }

    @Test
    fun testSanitizeTaskFieldTruncatesLongInput() {
        val longString = "T".repeat(500)
        val clean = TasksViewModel.sanitizeTaskField(longString, maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("T".repeat(300), clean)
    }

    @Test
    fun testSanitizeTaskFieldHandlesNullAndBlank() {
        assertEquals("", TasksViewModel.sanitizeTaskField(null))
        assertEquals("", TasksViewModel.sanitizeTaskField(""))
        assertEquals("", TasksViewModel.sanitizeTaskField("   "))
        assertEquals("", TasksViewModel.sanitizeTaskField("\u0000\u0007"))
    }
}
