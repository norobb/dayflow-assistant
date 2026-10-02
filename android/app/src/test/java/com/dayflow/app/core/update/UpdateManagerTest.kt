package com.dayflow.app.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UpdateManagerTest {

    @Test
    fun testDownloadStreamWithLimitSucceedsUnderLimit() {
        val testData = ByteArray(10 * 1024) { 0x41 }
        val input = ByteArrayInputStream(testData)
        val output = ByteArrayOutputStream()

        val success = UpdateManager.downloadStreamWithLimit(
            input = input,
            output = output,
            maxBytes = 20 * 1024
        )

        assertTrue(success)
        assertEquals(10 * 1024, output.toByteArray().size)
    }

    @Test
    fun testDownloadStreamWithLimitFailsWhenExceedingLimit() {
        val testData = ByteArray(50 * 1024) { 0x42 }
        val input = ByteArrayInputStream(testData)
        val output = ByteArrayOutputStream()

        val success = UpdateManager.downloadStreamWithLimit(
            input = input,
            output = output,
            maxBytes = 20 * 1024
        )

        assertFalse(success)
    }

    @Test
    fun testDownloadStreamWithLimitReportsProgress() {
        val testData = ByteArray(100) { 1 }
        val input = ByteArrayInputStream(testData)
        val output = ByteArrayOutputStream()
        var lastProgress = -1

        val success = UpdateManager.downloadStreamWithLimit(
            input = input,
            output = output,
            maxBytes = 1000,
            fileLength = 100,
            onProgress = { progress -> lastProgress = progress }
        )

        assertTrue(success)
        assertEquals(100, lastProgress)
    }
}
