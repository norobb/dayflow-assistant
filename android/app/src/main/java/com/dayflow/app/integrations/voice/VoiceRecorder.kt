package com.dayflow.app.integrations.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.dayflow.app.core.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    fun startRecording(): Boolean {
        if (!PermissionUtils.hasMicrophonePermission(context)) return false

        val outputFile = File(context.cacheDir, "dayflow_voice_${System.currentTimeMillis()}.m4a")
        currentOutputFile = outputFile

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _isRecording.value = true
            true
        } catch (_: IOException) {
            recorder.release()
            false
        } catch (_: IllegalStateException) {
            recorder.release()
            false
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording.value) return null

        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}

        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
        _amplitude.value = 0f

        return currentOutputFile?.absolutePath
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}

        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
        currentOutputFile?.delete()
        currentOutputFile = null
    }

    fun pollAmplitude() {
        val maxAmp = try {
            mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
        } catch (_: Exception) {
            0f
        }
        _amplitude.value = (maxAmp / 32767f).coerceIn(0f, 1f)
    }
}
