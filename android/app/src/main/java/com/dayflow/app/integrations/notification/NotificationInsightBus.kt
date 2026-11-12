package com.dayflow.app.integrations.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process bus that bridges the [DayflowNotificationListenerService] (which runs
 * in the system notification-listener context) with the Compose UI. The service
 * publishes newly understood notifications here; the Intelligence screen and the
 * Message Overlay observe them.
 */
object NotificationInsightBus {

    data class Insight(
        val packageName: String,
        val sender: String,
        val content: String,
        val title: String,
        val text: String,
        val understoodAt: Long = System.currentTimeMillis()
    )

    private val _latest = MutableStateFlow<Insight?>(null)
    val latest: StateFlow<Insight?> = _latest.asStateFlow()

    private val _history = MutableStateFlow<List<Insight>>(emptyList())
    val history: StateFlow<List<Insight>> = _history.asStateFlow()

    fun publish(insight: Insight) {
        _latest.value = insight
        _history.value = (listOf(insight) + _history.value).take(20)
    }

    fun clear() {
        _latest.value = null
        _history.value = emptyList()
    }
}
