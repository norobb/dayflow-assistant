package com.dayflow.app.integrations.document

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class DocumentParser(private val context: Context) {

    fun getFileName(uri: Uri): String {
        var name = "Document"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return sanitizeFileName(name)
    }

    fun extractSnippet(uri: Uri, maxChars: Int = 1000): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                extractSnippetFromStream(stream, maxChars)
            } ?: "Document contents"
        } catch (_: Exception) {
            "Document contents"
        }
    }

    companion object {
        /**
         * SECURITY ENHANCEMENT: Sanitize display names to prevent path traversal
         * by extracting the simple filename and stripping control characters.
         */
        fun sanitizeFileName(rawName: String?): String {
            if (rawName.isNullOrBlank()) return "Document"
            val cleanName = rawName
                .substringAfterLast('/')
                .substringAfterLast('\\')
                .replace(Regex("^\\.\\.+"), "")
                .replace(Regex("[\\x00-\\x1F\\x7F]"), "")
                .trim()
            return cleanName.ifEmpty { "Document" }.take(255)
        }

        /**
         * SECURITY ENHANCEMENT: Read stream in fixed-size buffers up to maxChars limit.
         * Prevents unbounded line reading (readLine()) and OOM DoS on large single-line files.
         */
        fun extractSnippetFromStream(stream: InputStream, maxChars: Int = 1000): String {
            val reader = InputStreamReader(stream, Charsets.UTF_8)
            return extractSnippetFromReader(reader, maxChars)
        }

        fun extractSnippetFromReader(reader: Reader, maxChars: Int = 1000): String {
            val limit = maxChars.coerceIn(1, 10000)
            val sb = StringBuilder()
            val buffer = CharArray(512)
            var charsRead = 0
            while (sb.length < limit && reader.read(buffer, 0, minOf(buffer.size, limit - sb.length)).also { charsRead = it } != -1) {
                sb.append(buffer, 0, charsRead)
            }
            return sb.toString().trim()
        }

        /**
         * SECURITY ENHANCEMENT: Read byte stream with a hard byte limit (default 10MB).
         * Prevents Out-Of-Memory (OOM) Denial of Service (DoS) attacks from oversized shared files.
         * Returns null if total stream size exceeds maxBytes limit.
         */
        fun readBytesWithLimit(stream: InputStream, maxBytes: Int = 10 * 1024 * 1024): ByteArray? {
            val buffer = ByteArray(8192)
            val output = java.io.ByteArrayOutputStream()
            var totalBytes = 0
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                totalBytes += bytesRead
                if (totalBytes > maxBytes) {
                    return null
                }
                output.write(buffer, 0, bytesRead)
            }
            return output.toByteArray()
        }
    }
}
