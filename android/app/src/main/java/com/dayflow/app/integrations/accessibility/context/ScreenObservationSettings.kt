package com.dayflow.app.integrations.accessibility.context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether Dayflow may read screens passively (without the user asking).
 *
 * Kept as a volatile flag so the accessibility service can check it on every
 * window event without touching DataStore from the main thread. The
 * Intelligence screen owns the switch and mirrors it here.
 */
object ScreenObservationSettings {

    @Volatile
    var ambientEnabled: Boolean = false
        private set

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun setAmbientEnabled(value: Boolean) {
        ambientEnabled = value
        _enabled.value = value
    }
}
