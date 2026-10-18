package com.dayflow.app.integrations.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationHelperTest {

    @Test
    fun testSanitizeNotificationTextStripsControlCharacters() {
        val raw = "Meeting\u0000 with\u0007 Team"
        val clean = NotificationHelper.sanitizeNotificationText(raw)
        assertEquals("Meeting with Team", clean)
    }

    @Test
    fun testSanitizeNotificationTextTruncatesLongInput() {
        val longString = "B".repeat(500)
        val clean = NotificationHelper.sanitizeNotificationText(longString, maxLength = 200)
        assertEquals(200, clean?.length)
        assertEquals("B".repeat(200), clean)
    }

    @Test
    fun testSanitizeNotificationTextHandlesNullAndBlank() {
        assertNull(NotificationHelper.sanitizeNotificationText(null))
        assertNull(NotificationHelper.sanitizeNotificationText(""))
        assertNull(NotificationHelper.sanitizeNotificationText("   "))
        assertNull(NotificationHelper.sanitizeNotificationText("\u0000\u0007"))
    }

    @Test
    fun testSanitizeNotificationTextPreservesValidInput() {
        val valid = "Doctor appointment at 14:30"
        assertEquals(valid, NotificationHelper.sanitizeNotificationText(valid))
    }

    @Test
    fun testSanitizeNotificationListenerTitleAndBody() {
        val rawTitle = "Calendar Alert\u0000\u001F"
        val rawBody = "You have a meeting with \u0000Dr. Smith at 15:00\u007F"

        val cleanTitle = NotificationHelper.sanitizeNotificationText(rawTitle, maxLength = 200)
        val cleanBody = NotificationHelper.sanitizeNotificationText(rawBody, maxLength = 1000)

        assertEquals("Calendar Alert", cleanTitle)
        assertEquals("You have a meeting with Dr. Smith at 15:00", cleanBody)
    }

    @Test
    fun testSanitizeReminderIdInBroadcastIntent() {
        val rawReminderId = "rm-123456789\u0000-id"
        val cleanReminderId = NotificationHelper.sanitizeNotificationText(rawReminderId, maxLength = 100)

        assertEquals("rm-123456789-id", cleanReminderId)
    }
}
