package com.dayflow.app.integrations.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceRecorderTest {

    @Test
    fun testVoiceRecorderMaxDurationAndFileSizeBounds() {
        assertEquals(300_000, VoiceRecorder.MAX_RECORDING_DURATION_MS)
        assertEquals(25 * 1024 * 1024L, VoiceRecorder.MAX_RECORDING_FILE_SIZE_BYTES)
    }
}
