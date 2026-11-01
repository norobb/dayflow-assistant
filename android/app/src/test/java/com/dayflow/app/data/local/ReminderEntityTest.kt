package com.dayflow.app.data.local

import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderEntityTest {

    @Test
    fun testSanitizeEntityFieldStripsControlCharacters() {
        val raw = "Pick up\u0000 groceries\u0007 now"
        val clean = ReminderEntity.sanitizeEntityField(raw)
        assertEquals("Pick up groceries now", clean)
    }

    @Test
    fun testSanitizeEntityFieldTruncatesLongInput() {
        val longText = "Z".repeat(500)
        val clean = ReminderEntity.sanitizeEntityField(longText, maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("Z".repeat(300), clean)
    }

    @Test
    fun testSanitizeEntityFieldFallbackOnBlankOrNull() {
        assertEquals("Reminder", ReminderEntity.sanitizeEntityField(null, fallback = "Reminder"))
        assertEquals("Reminder", ReminderEntity.sanitizeEntityField("", fallback = "Reminder"))
        assertEquals("Reminder", ReminderEntity.sanitizeEntityField("   ", fallback = "Reminder"))
        assertEquals("Reminder", ReminderEntity.sanitizeEntityField("\u0000\u0007", fallback = "Reminder"))
    }

    @Test
    fun testFromDomainSanitizesAllStringFields() {
        val dirtyReminder = DayflowReminder(
            id = "rm-123",
            title = "Doctor\u0007 visit" + "C".repeat(500),
            timeLabel = "Tomorrow\u0000 09:00",
            scheduledTimeMillis = 1000L,
            sourceType = SourceType.VOICE,
            dismissed = false
        )

        val entity = ReminderEntity.fromDomain(dirtyReminder)

        assertEquals("rm-123", entity.id)
        assertEquals("Doctor visit" + "C".repeat(288), entity.title)
        assertEquals(300, entity.title.length)
        assertEquals("Tomorrow 09:00", entity.timeLabel)
        assertEquals("voice", entity.sourceType)
    }

    @Test
    fun testFromDomainHandlesBlankFields() {
        val emptyReminder = DayflowReminder(
            id = "rm-456",
            title = "",
            timeLabel = "",
            scheduledTimeMillis = 2000L,
            sourceType = SourceType.CUSTOM,
            dismissed = true
        )

        val entity = ReminderEntity.fromDomain(emptyReminder)

        assertEquals("Reminder", entity.title)
        assertEquals("Later today", entity.timeLabel)
    }
}
