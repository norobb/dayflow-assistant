package com.dayflow.app.data.local

import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventEntityTest {

    @Test
    fun testSanitizeTextStripsControlCharacters() {
        val raw = "Team\u0000 Sync\u0007"
        val clean = EventEntity.sanitizeText(raw, "Event")
        assertEquals("Team Sync", clean)
    }

    @Test
    fun testSanitizeTextTruncatesLongInput() {
        val longString = "E".repeat(500)
        val clean = EventEntity.sanitizeText(longString, "Event", maxLength = 300)
        assertEquals(300, clean.length)
        assertEquals("E".repeat(300), clean)
    }

    @Test
    fun testSanitizeTextHandlesNullAndBlankWithFallback() {
        assertEquals("Fallback", EventEntity.sanitizeText(null, "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeText("", "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeText("   ", "Fallback"))
        assertEquals("Fallback", EventEntity.sanitizeText("\u0000\u0007", "Fallback"))
    }

    @Test
    fun testFromDomainSanitizesAllFields() {
        val event = DayflowEvent(
            id = "ev-101",
            time = "14:30\u0000",
            title = "  Doctor\u0007 Appointment  ",
            location = " Praxis\u0000 Stein ",
            dateLabel = "Tomorrow\u0000",
            sourceType = SourceType.CUSTOM
        )

        val entity = EventEntity.fromDomain(event)

        assertEquals("ev-101", entity.id)
        assertEquals("14:30", entity.time)
        assertEquals("Doctor Appointment", entity.title)
        assertEquals("Praxis Stein", entity.location)
        assertEquals("Tomorrow", entity.dateLabel)
    }

    @Test
    fun testFromDomainHandlesNullLocationAndDefaults() {
        val event = DayflowEvent(
            id = "ev-102",
            time = "",
            title = "",
            location = "  \u0000  ",
            dateLabel = "",
            sourceType = SourceType.MESSAGE
        )

        val entity = EventEntity.fromDomain(event)

        assertEquals("12:00", entity.time)
        assertEquals("Event", entity.title)
        assertNull(entity.location)
        assertEquals("Today", entity.dateLabel)
    }
}
