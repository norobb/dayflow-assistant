package com.dayflow.app

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dayflow.app.integrations.media.ContentExtractor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * End-to-end on-device speech check: the system TTS speaks a voice note into
 * a WAV file, then [ContentExtractor] transcribes it with the system recognizer.
 */
@RunWith(AndroidJUnit4::class)
class VoiceTranscriptionTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext


    private fun synthesize(text: String, locale: Locale, out: File): Boolean {
        val ready = CountDownLatch(1)
        val done = CountDownLatch(1)
        var tts: TextToSpeech? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            tts = TextToSpeech(context) { ready.countDown() }
        }
        ready.await(20, TimeUnit.SECONDS)
        val engine = tts ?: return false
        engine.language = locale
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { done.countDown() }
            @Deprecated("Deprecated in Java") override fun onError(id: String?) { done.countDown() }
        })
        engine.synthesizeToFile(text, null, out, "note")
        done.await(60, TimeUnit.SECONDS)
        engine.shutdown()
        if (out.length() > 10_000) exportToDownloads(out)
        return out.length() > 10_000
    }

    /** Keeps a copy in Downloads so the share-sheet flow can be tried by hand with real speech. */
    private fun exportToDownloads(file: File) {
        val resolver = context.contentResolver
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, "audio/wav")
        }
        runCatching {
            resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                resolver.openOutputStream(uri)?.use { os -> file.inputStream().use { it.copyTo(os) } }
            }
        }
    }

    @Test
    fun transcribesGermanVoiceNote() {
        val out = File(context.getExternalFilesDir(null), "sprachnotiz.wav")
        assumeTrue("TTS produced audio", synthesize("Erinnere mich nächsten Montag daran, meine Präsentation abzugeben.", Locale.GERMANY, out))
        val text = runBlocking { ContentExtractor(context).audioText(out.readBytes(), "de") }
        android.util.Log.i("VoiceTest", "de transcript: $text")
        assertTrue("transcript: $text", text != null && text.contains("Präsentation", ignoreCase = true))
    }

    @Test
    fun transcribesEnglishVoiceNote() {
        val out = File(context.getExternalFilesDir(null), "voice_note.wav")
        assumeTrue("TTS produced audio", synthesize("Remind me next Monday to submit my presentation.", Locale.US, out))
        val text = runBlocking { ContentExtractor(context).audioText(out.readBytes(), "en") }
        android.util.Log.i("VoiceTest", "en transcript: $text")
        assertTrue("transcript: $text", text != null && text.contains("presentation", ignoreCase = true))
    }
}
