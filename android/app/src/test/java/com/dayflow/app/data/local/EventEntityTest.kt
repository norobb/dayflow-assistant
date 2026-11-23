package com.dayflow.app.data.local

import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventEntityTest {

    @Test
    fun testSanitizeEntityFieldStripsControlCharacters() {
        val raw = "Team\u0000 Sync\u0007 Meeting"
        val clean = EventEntity.sanitizeEntityField(raw)
        assertEquals("Team Sync Meeting", clean)
    }

    @Test
    fun testSanitizeEntityFieldTruncatesLongInput() {
        val longText = "X".repeat(500)
        val clean = EventEntity.sanitizeEntityField(longText, maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("X".repeat(300), clean)
    }

    @Test
    fun testSanitizeEntityFieldFallbackOnBlankOrNull() {
        assertEquals("Fallback", EventEntity.sanitizeEntityField(null, fallback = "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeEntityField("", fallback = "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeEntityField("   ", fallback = "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeEntityField("\u0000\u0007", fallback = "Fallback"))
    }

    @Test
    fun testFromDomainSanitizesAllStringFields() {
        val dirtyEvent = DayflowEvent(
            id = "ev-123",
            time = "14:00\u0000 - 15:00",
            title = "Design\u0007 Review" + "A".repeat(500),
            location = "Room\u001F 101",
            dateLabel = "Tomorrow\u0000",
            sourceType = SourceType.MESSAGE
        )

        val entity = EventEntity.fromDomain(dirtyEvent)

        assertEquals("ev-123", entity.id)
        assertEquals("14:00 - 15:00", entity.time)
        assertEquals("Design Review" + "A".repeat(287), entity.title)
        assertEquals(300, entity.title.length)
        assertEquals("Room 101", entity.location)
        assertEquals("Tomorrow", entity.dateLabel)
        assertEquals("message", entity.sourceType)
    }

    @Test
    fun testFromDomainHandlesNullLocationAndBlankFields() {
        val emptyEvent = DayflowEvent(
            id = "ev-456",
            time = "",
            title = "",
            location = null,
            dateLabel = "",
            sourceType = SourceType.CUSTOM
        )

        val entity = EventEntity.fromDomain(emptyEvent)

        assertEquals("12:00", entity.time)
        assertEquals("Event", entity.title)
        assertNull(entity.location)
        assertEquals("Today", entity.dateLabel)
    }
}
