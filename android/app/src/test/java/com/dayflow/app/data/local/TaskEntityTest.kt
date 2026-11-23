package com.dayflow.app.data.local

import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskEntityTest {

    @Test
    fun testSanitizeEntityFieldStripsControlCharacters() {
        val raw = "Finish\u0000 Report\u0007 Immediately"
        val clean = TaskEntity.sanitizeEntityField(raw)
        assertEquals("Finish Report Immediately", clean)
    }

    @Test
    fun testSanitizeEntityFieldTruncatesLongInput() {
        val longText = "Y".repeat(500)
        val clean = TaskEntity.sanitizeEntityField(longText, maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("Y".repeat(300), clean)
    }

    @Test
    fun testSanitizeEntityFieldFallbackOnBlankOrNull() {
        assertEquals("Task", TaskEntity.sanitizeEntityField(null, fallback = "Task"))
        assertEquals("Task", TaskEntity.sanitizeEntityField("", fallback = "Task"))
        assertEquals("Task", TaskEntity.sanitizeEntityField("   ", fallback = "Task"))
        assertEquals("Task", TaskEntity.sanitizeEntityField("\u0000\u0007", fallback = "Task"))
    }

    @Test
    fun testFromDomainSanitizesAllStringFields() {
        val dirtyTask = DayflowTask(
            id = "tk-123",
            title = "Code\u0007 Review" + "B".repeat(500),
            completed = false,
            dueDate = "Tomorrow\u0000 17:00",
            sourceType = SourceType.MESSAGE
        )

        val entity = TaskEntity.fromDomain(dirtyTask)

        assertEquals("tk-123", entity.id)
        assertEquals("Code Review" + "B".repeat(289), entity.title)
        assertEquals(300, entity.title.length)
        assertEquals("Tomorrow 17:00", entity.dueDate)
        assertEquals("message", entity.sourceType)
    }

    @Test
    fun testFromDomainHandlesNullDueDateAndBlankFields() {
        val emptyTask = DayflowTask(
            id = "tk-456",
            title = "",
            completed = true,
            dueDate = null,
            sourceType = SourceType.CUSTOM
        )

        val entity = TaskEntity.fromDomain(emptyTask)

        assertEquals("Task", entity.title)
        assertNull(entity.dueDate)
    }
}
