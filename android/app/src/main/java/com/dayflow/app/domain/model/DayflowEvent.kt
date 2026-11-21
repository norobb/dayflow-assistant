package com.dayflow.app.domain.model

data class DayflowEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String,
    val title: String,
    val location: String? = null,
    val dateLabel: String = "Today",
    val sourceType: SourceType = SourceType.DEFAULT,
    val createdAt: Long = System.currentTimeMillis(),
    /** Resolved start; null when the source gave no usable date/time. */
    val startMillis: Long? = null,
    /** Row id in the system calendar once written there. */
    val calendarEventId: Long? = null,
    /** True for items read live from the device calendar (not stored by Dayflow). */
    val external: Boolean = false
)
