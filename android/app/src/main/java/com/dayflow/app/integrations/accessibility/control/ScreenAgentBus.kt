package com.dayflow.app.integrations.accessibility.control

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bus for the Screen Agent (natural-language goal → on-screen execution).
 * The UI posts a [goal] plus the API key/model; the agent publishes a
 * running log of [Step]s and its [running] state.
 */
object ScreenAgentBus {

    data class Step(
        val index: Int,
        val action: String,
        val reason: String,
        val done: Boolean = false
    )

    private val _goal = MutableStateFlow("")
    val goal: StateFlow<String> = _goal.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _steps = MutableStateFlow<List<Step>>(emptyList())
    val steps: StateFlow<List<Step>> = _steps.asStateFlow()

    fun start(goal: String) {
        _goal.value = goal
        _steps.value = emptyList()
        _running.value = true
    }

    fun publish(step: Step) {
        _steps.value = _steps.value + step
    }

    fun finish() {
        _running.value = false
    }

    fun cancel() {
        _running.value = false
    }
}
