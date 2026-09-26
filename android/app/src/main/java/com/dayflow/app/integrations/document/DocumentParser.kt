package com.dayflow.app.integrations.document

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStreamReader

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
        return name
    }

    fun extractSnippet(uri: Uri, maxChars: Int = 1000): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val sb = StringBuilder()
                var line = reader.readLine()
                while (line != null && sb.length < maxChars) {
                    sb.append(line).append("\n")
                    line = reader.readLine()
                }
                sb.toString().trim()
            } ?: "Document contents"
        } catch (_: Exception) {
            "Document contents"
        }
    }
}
