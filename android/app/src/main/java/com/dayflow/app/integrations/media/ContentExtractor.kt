package com.dayflow.app.integrations.media

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteOrder
import kotlin.coroutines.resume

/**
 * Turns media into text entirely on the device:
 * images via ML Kit OCR, PDFs via [PdfRenderer] + OCR, audio via the system
 * speech recognizer. Every method returns null when nothing could be read.
 */
class ContentExtractor(private val context: Context) {

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    // ------------------------------------------------------------------
    // Images
    // ------------------------------------------------------------------

    suspend fun imageText(bytes: ByteArray): String? = withContext(Dispatchers.Default) {
        val bitmap = decodeScaled(bytes) ?: return@withContext null
        ocr(bitmap).also { bitmap.recycle() }
    }

    private fun decodeScaled(bytes: ByteArray, maxSide: Int = 2400): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private suspend fun ocr(bitmap: Bitmap): String? = suspendCancellableCoroutine { cont ->
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                // One line per OCR line keeps label/value pairs apart for the parser.
                val text = result.textBlocks.flatMap { b -> b.lines.map { it.text } }.joinToString("\n")
                cont.resume(text.ifBlank { null })
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "OCR failed", e)
                cont.resume(null)
            }
    }

    // ------------------------------------------------------------------
    // PDFs
    // ------------------------------------------------------------------

    suspend fun pdfText(bytes: ByteArray, maxPages: Int = 8): String? = withContext(Dispatchers.IO) {
        val file = File.createTempFile("dayflow_pdf", ".pdf", context.cacheDir)
        try {
            FileOutputStream(file).use { it.write(bytes) }
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val pages = (0 until minOf(renderer.pageCount, maxPages)).mapNotNull { index ->
                        renderer.openPage(index).use { page ->
                            // ~200 dpi equivalent, capped to keep memory bounded.
                            val scale = minOf(2.8f, 2000f / page.width)
                            val bitmap = Bitmap.createBitmap(
                                (page.width * scale).toInt(), (page.height * scale).toInt(), Bitmap.Config.ARGB_8888
                            )
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            ocr(bitmap).also { bitmap.recycle() }
                        }
                    }
                    pages.joinToString("\n").ifBlank { null }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "PDF read failed", e)
            null
        } finally {
            file.delete()
        }
    }

    // ------------------------------------------------------------------
    // Audio
    // ------------------------------------------------------------------

    /** True when this device can transcribe recorded audio files offline. */
    fun canTranscribeFiles(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SpeechRecognizer.isRecognitionAvailable(context)

    /** Why the last transcription failed: [AUDIO_LANGUAGE_MISSING] when the language pack is still downloading. */
    @Volatile var lastAudioError: Int = 0
        private set

    suspend fun audioText(bytes: ByteArray, language: String): String? {
        lastAudioError = 0
        if (!canTranscribeFiles()) return null
        val pcm = withContext(Dispatchers.IO) { decodeToPcm(bytes) } ?: return null
        val onDevice = withTimeoutOrNull(120_000) { recognize(pcm, language, onDevice = true) }
        if (onDevice != null || lastAudioError !in LANGUAGE_ERRORS) return onDevice
        // Language pack missing on the device: start its download and try the default recognizer meanwhile.
        triggerLanguageDownload(language)
        val fallback = withTimeoutOrNull(120_000) { recognize(pcm, language, onDevice = false) }
        if (fallback == null) lastAudioError = AUDIO_LANGUAGE_MISSING
        return fallback
    }

    private suspend fun triggerLanguageDownload(language: String) = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) return@withContext
        runCatching {
            val sr = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            sr.triggerModelDownload(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag(language)))
            Handler(Looper.getMainLooper()).postDelayed({ runCatching { sr.destroy() } }, 5_000)
        }
    }

    private fun localeTag(language: String) = when (language.take(2)) { "de" -> "de-DE"; "es" -> "es-ES"; else -> "en-US" }

    private class Pcm(val data: ByteArray, val sampleRate: Int)

    /** Decodes any Android-supported audio container to 16-bit mono PCM. */
    private fun decodeToPcm(bytes: ByteArray): Pcm? {
        val file = File.createTempFile("dayflow_audio", ".bin", context.cacheDir)
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            FileOutputStream(file).use { it.write(bytes) }
            extractor.setDataSource(file.absolutePath)
            val track = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return null
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            var sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val out = ByteArrayOutputStream()
            if (mime == MediaFormat.MIMETYPE_AUDIO_RAW) {
                // WAV/PCM: copy samples directly.
                val buf = java.nio.ByteBuffer.allocate(1 shl 16)
                while (true) {
                    val n = extractor.readSampleData(buf, 0)
                    if (n < 0) break
                    val arr = ByteArray(n); buf.get(arr, 0, n); buf.clear()
                    out.write(arr); extractor.advance()
                }
            } else {
                codec = MediaCodec.createDecoderByType(mime).apply { configure(format, null, null, 0); start() }
                val info = MediaCodec.BufferInfo()
                var inputDone = false
                while (true) {
                    if (!inputDone) {
                        val inIndex = codec.dequeueInputBuffer(10_000)
                        if (inIndex >= 0) {
                            val n = extractor.readSampleData(codec.getInputBuffer(inIndex)!!, 0)
                            if (n < 0) {
                                codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                codec.queueInputBuffer(inIndex, 0, n, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                    val outIndex = codec.dequeueOutputBuffer(info, 10_000)
                    if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        codec.outputFormat.let {
                            sampleRate = it.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            channels = it.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                    } else if (outIndex >= 0) {
                        val buf = codec.getOutputBuffer(outIndex)!!
                        val arr = ByteArray(info.size); buf.position(info.offset); buf.get(arr); out.write(arr)
                        codec.releaseOutputBuffer(outIndex, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                    }
                    if (out.size() > MAX_PCM_BYTES) break
                }
            }
            val raw = out.toByteArray()
            if (raw.isEmpty()) return null
            return Pcm(downmix(raw, channels), sampleRate)
        } catch (e: Exception) {
            Log.w(TAG, "Audio decode failed", e)
            return null
        } finally {
            runCatching { codec?.stop() }; runCatching { codec?.release() }
            extractor.release()
            file.delete()
        }
    }

    private fun downmix(pcm: ByteArray, channels: Int): ByteArray {
        if (channels <= 1) return pcm
        val src = java.nio.ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val frames = src.remaining() / channels
        val dst = java.nio.ByteBuffer.allocate(frames * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(frames) { f ->
            var sum = 0
            repeat(channels) { c -> sum += src.get(f * channels + c) }
            dst.putShort((sum / channels).toShort())
        }
        return dst.array()
    }

    /** Feeds PCM through a pipe into the system recognizer (Android 13+). Runs on the main looper. */
    private suspend fun recognize(pcm: Pcm, language: String, onDevice: Boolean): String? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val (readSide, writeSide) = ParcelFileDescriptor.createPipe()
            val sr = if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
            val segments = mutableListOf<String>()
            var finished = false
            fun finish() {
                if (finished) return
                finished = true
                runCatching { sr.destroy() }
                runCatching { readSide.close() }
                val text = segments.joinToString(" ").trim()
                if (cont.isActive) cont.resume(text.ifBlank { null })
            }
            fun collect(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }?.let { segments += it }
            }
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onError(error: Int) {
                    Log.w(TAG, "Recognizer error $error (onDevice=$onDevice)")
                    lastAudioError = error
                    finish()
                }
                override fun onResults(results: Bundle?) { collect(results); finish() }
                override fun onSegmentResults(segmentResults: Bundle) { collect(segmentResults) }
                override fun onEndOfSegmentedSession() { finish() }
            })
            val locale = localeTag(language)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, readSide)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, pcm.sampleRate)
                putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
            }
            // Writer thread: stream the samples, then a little silence so the last word is closed.
            Thread {
                runCatching {
                    ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { os ->
                        os.write(pcm.data)
                        os.write(ByteArray(pcm.sampleRate)) // 0.5 s silence
                    }
                }
            }.start()
            sr.startListening(intent)
            // Safety net if the service never answers.
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 110_000)
            cont.invokeOnCancellation { finish() }
        }
    }

    companion object {
        private const val TAG = "DayflowExtract"
        const val AUDIO_LANGUAGE_MISSING = -13
        private val LANGUAGE_ERRORS = setOf(SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
        private const val MAX_PCM_BYTES = 16_000 * 2 * 60 * 6 // ~6 min mono 16 kHz
    }
}
