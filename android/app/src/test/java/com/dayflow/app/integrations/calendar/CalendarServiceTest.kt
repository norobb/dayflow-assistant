package com.dayflow.app.integrations.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarServiceTest {

    @Test
    fun testSanitizeFieldStripsControlCharacters() {
        val raw = "Meeting\u0000 with\u0007 Team"
        val clean = CalendarService.sanitizeField(raw)
        assertEquals("Meeting with Team", clean)
    }

    @Test
    fun testSanitizeFieldTruncatesLongInput() {
        val longString = "A".repeat(1000)
        val clean = CalendarService.sanitizeField(longString, maxLength = 500)
        assertEquals(500, clean?.length)
        assertEquals("A".repeat(500), clean)
    }

    @Test
    fun testSanitizeFieldHandlesNullAndBlank() {
        assertNull(CalendarService.sanitizeField(null))
        assertNull(CalendarService.sanitizeField(""))
        assertNull(CalendarService.sanitizeField("   "))
        assertNull(CalendarService.sanitizeField("\u0000\u0007"))
    }

    @Test
    fun testSanitizeFieldPreservesValidInput() {
        val valid = "Doctor Appointment at 14:30"
        assertEquals(valid, CalendarService.sanitizeField(valid))
    }
}
