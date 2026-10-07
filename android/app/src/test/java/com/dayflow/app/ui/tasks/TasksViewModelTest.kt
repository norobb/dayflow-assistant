package com.dayflow.app.ui.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasksViewModelTest {

    @Test
    fun testSanitizeTaskInputStripsControlCharacters() {
        val raw = "Buy\u0000 groceries\u0007 today"
        val clean = TasksViewModel.sanitizeTaskInput(raw)
        assertEquals("Buy groceries today", clean)
    }

    @Test
    fun testSanitizeTaskInputTruncatesLongInput() {
        val longString = "T".repeat(500)
        val clean = TasksViewModel.sanitizeTaskInput(longString, maxLength = 200)
        assertEquals(200, clean?.length)
        assertEquals("T".repeat(200), clean)
    }

    @Test
    fun testSanitizeTaskInputHandlesNullAndBlank() {
        assertNull(TasksViewModel.sanitizeTaskInput(null))
        assertNull(TasksViewModel.sanitizeTaskInput(""))
        assertNull(TasksViewModel.sanitizeTaskInput("   "))
        assertNull(TasksViewModel.sanitizeTaskInput("\u0000\u0007"))
    }

    @Test
    fun testSanitizeTaskInputPreservesValidInput() {
        val valid = "Submit project presentation"
        assertEquals(valid, TasksViewModel.sanitizeTaskInput(valid))
    }
}
