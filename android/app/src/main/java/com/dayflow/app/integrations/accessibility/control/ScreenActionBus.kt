package com.dayflow.app.integrations.accessibility.control

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bridge between the Intelligence screen (UI) and
 * [com.dayflow.app.integrations.accessibility.DayflowAccessibilityService]
 * (the context that owns screen perception and gesture injection).
 *
 * The UI posts [Action] requests; the service consumes them and answers with
 * an [ActionResult] plus, after a scan, a fresh [elementSnapshot]. This keeps
 * all AccessibilityService-only APIs inside the service while the Compose UI
 * stays a plain observer.
 */
object ScreenActionBus {

    /** One actionable element of the currently perceived screen. */
    data class ScreenElement(
        val id: Int,
        val label: String,
        val className: String,
        val isEditable: Boolean,
        val centerX: Int,
        val centerY: Int
    )

    enum class Action { SCAN, TAP_ELEMENT, SWIPE_UP, SWIPE_DOWN, BACK }

    data class Request(
        val action: Action,
        val elementId: Int = -1
    )

    sealed class ActionResult {
        data object Idle : ActionResult()
        data object Running : ActionResult()
        data class Done(val message: String) : ActionResult()
        data class Failed(val message: String) : ActionResult()
    }

    private val _requests = MutableStateFlow<Request?>(null)
    val requests: StateFlow<Request?> = _requests.asStateFlow()

    private val _elements = MutableStateFlow<List<ScreenElement>>(emptyList())
    val elements: StateFlow<List<ScreenElement>> = _elements.asStateFlow()

    private val _result = MutableStateFlow<ActionResult>(ActionResult.Idle)
    val result: StateFlow<ActionResult> = _result.asStateFlow()

    /** Called by the UI to enqueue a control action. */
    fun post(request: Request) {
        _requests.value = request
    }

    /** Called by the service when it starts handling a request. */
    fun beginHandling() {
        _result.value = ActionResult.Running
    }

    /** Called by the service after a scan. */
    fun publishElements(elements: List<ScreenElement>) {
        _elements.value = elements
    }

    /** Called by the service to report the outcome of a request. */
    fun report(result: ActionResult) {
        _result.value = result
        _requests.value = null
    }
}
