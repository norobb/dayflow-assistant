package com.dayflow.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun formatCurrentTime(): String {
        return timeFormat.format(Date())
    }

    fun formatDateTime(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun parseTimeToMillis(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 12
                val minute = parts[1].toIntOrNull() ?: 0
                val now = java.util.Calendar.getInstance()
                now.set(java.util.Calendar.HOUR_OF_DAY, hour)
                now.set(java.util.Calendar.MINUTE, minute)
                now.set(java.util.Calendar.SECOND, 0)
                now.timeInMillis
            } else {
                System.currentTimeMillis() + 3600000L
            }
        } catch (_: Exception) {
            System.currentTimeMillis() + 3600000L
        }
    }
}
