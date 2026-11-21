package com.dayflow.app.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Soft synthesized UI tones (the website's Web Audio sounds) plus haptics.
 * Both respect the user's settings; [soundEnabled]/[hapticEnabled] are kept in
 * sync with preferences by the application.
 */
class SoundAndHaptics(private val context: Context) {

    @Volatile var soundEnabled: Boolean = true
    @Volatile var hapticEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private class Note(val freq: Double, val startMs: Int, val durMs: Int)

    // Pre-rendered once; playing is just a buffer copy.
    private val click by lazy { render(listOf(Note(880.0, 0, 40)), 0.18) }
    private val toggle by lazy { render(listOf(Note(660.0, 0, 50), Note(990.0, 35, 60)), 0.16) }
    private val analyze by lazy { render(listOf(Note(523.25, 0, 90), Note(659.25, 70, 90), Note(783.99, 140, 140)), 0.14) }
    private val success by lazy { render(listOf(Note(523.25, 0, 260), Note(783.99, 60, 300)), 0.16) } // fifth
    private val error by lazy { render(listOf(Note(311.13, 0, 120), Note(261.63, 110, 180)), 0.16) }

    fun playClick() { play(click); performHaptic(8) }
    fun playToggle() { play(toggle); performHaptic(12) }
    fun playAnalyze() { play(analyze); performHaptic(8) }
    fun playSuccess() { play(success); performHaptic(8) }
    fun playError() { play(error); performHaptic(40) }

    private fun render(notes: List<Note>, gain: Double): ShortArray {
        val total = notes.maxOf { it.startMs + it.durMs } + 20
        val out = DoubleArray(RATE * total / 1000)
        notes.forEach { n ->
            val start = RATE * n.startMs / 1000
            val len = RATE * n.durMs / 1000
            for (i in 0 until len) {
                val t = i.toDouble() / RATE
                val attack = (i / (RATE * 0.005)).coerceAtMost(1.0)
                val env = attack * exp(-4.0 * i / len)
                val idx = start + i
                if (idx < out.size) out[idx] += sin(2 * PI * n.freq * t) * env * gain
            }
        }
        return ShortArray(out.size) { (out[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort() }
    }

    private fun play(samples: ShortArray) {
        if (!soundEnabled) return
        runCatching {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder().setSampleRate(RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            track.write(samples, 0, samples.size)
            track.setNotificationMarkerPosition(samples.size - 1)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        }
    }

    fun performHaptic(milliseconds: Long) {
        if (!hapticEnabled) return
        try {
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            // Vibration may be restricted; never crash for feedback.
        }
    }

    companion object {
        private const val RATE = 22050
    }
}
