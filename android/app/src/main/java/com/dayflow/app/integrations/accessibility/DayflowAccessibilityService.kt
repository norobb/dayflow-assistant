package com.dayflow.app.integrations.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.graphics.toArgb
import android.view.accessibility.AccessibilityNodeInfo
import com.dayflow.app.integrations.accessibility.context.CaptureTrigger
import com.dayflow.app.integrations.accessibility.context.ScreenCapture
import com.dayflow.app.integrations.accessibility.context.ScreenCaptureStore
import com.dayflow.app.integrations.accessibility.context.ScreenObservationSettings
import com.dayflow.app.integrations.accessibility.control.DayflowGestureController
import com.dayflow.app.integrations.accessibility.control.DayflowSafetyGate
import com.dayflow.app.integrations.accessibility.control.ScreenActionBus
import com.dayflow.app.integrations.accessibility.control.ScreenElementExtractor
import com.dayflow.app.integrations.notification.NotificationInsightBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Screen Intelligence service.
 *
 * Owns everything that needs an AccessibilityService:
 *  - reading the active window (text + interactive elements) on request,
 *  - carefully observing screens in the background when the user enabled it,
 *  - performing taps, swipes and back actions through the humanized gesture
 *    pipeline, always after [DayflowSafetyGate] has approved the foreground app.
 *
 * The UI never touches these APIs directly; it posts [ScreenActionBus.Request]s
 * and observes the results.
 */
class DayflowAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastPublishedContent: String? = null
    private var lastPublishAt: Long = 0L

    private lateinit var extractor: ScreenElementExtractor
    private lateinit var gestures: DayflowGestureController
    private val safetyGate = DayflowSafetyGate()
    private var shadeChips: NotificationShadeChips? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        connected.value = true
        extractor = ScreenElementExtractor(this) { packageName }
        gestures = DayflowGestureController(this)
        val chips = NotificationShadeChips(this).also { shadeChips = it }
        (application as? com.dayflow.app.DayflowApplication)?.preferencesRepository?.let { prefs ->
            scope.launch { prefs.boolPref(com.dayflow.app.domain.repository.PrefKeys.SHADE_CHIPS, true).collect { chips.enabled = it } }
            scope.launch {
                prefs.stringPref(com.dayflow.app.domain.repository.PrefKeys.ACCENT_COLOR).collect { hex ->
                    com.dayflow.app.core.designsystem.accentFromHex(hex)?.let { chips.accentArgb = it.toArgb() }
                }
            }
        }

        scope.launch {
            ScreenActionBus.requests.collectLatest { request ->
                if (request != null) handleRequest(request)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        shadeChips?.onEvent(event)
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        // Passive observation is opt-in. Without it the service only acts on
        // explicit requests from the UI.
        if (!ScreenObservationSettings.ambientEnabled) return

        scope.launch {
            try {
                // The event's package can be SystemUI (notification shade), while
                // the window we actually read is the app below it. Trust the root.
                val root: AccessibilityNodeInfo = rootInActiveWindow ?: return@launch
                val foreground = root.packageName?.toString().orEmpty()

                // Never perceive our own screens — Dayflow's UI is not "context".
                if (foreground == packageName) return@launch
                if (!safetyGate.checkPackage(foreground).allowed) return@launch

                val texts = collectTexts(root, MAX_INSIGHT_TEXTS)
                if (texts.isEmpty()) return@launch

                val content = texts.joinToString(" ").take(MAX_CONTENT_LENGTH)
                if (content.isBlank()) return@launch

                // Rate limit (1 publish / second) and dedupe identical screens so
                // frequent TYPE_WINDOW_CONTENT_CHANGED events don't flood the bus.
                val now = System.currentTimeMillis()
                if (content == lastPublishedContent && now - lastPublishAt < DEDUPE_WINDOW_MS) {
                    return@launch
                }
                if (now - lastPublishAt < MIN_PUBLISH_INTERVAL_MS) {
                    return@launch
                }
                lastPublishedContent = content
                lastPublishAt = now

                val label = appLabelFor(foreground)
                NotificationInsightBus.publish(
                    NotificationInsightBus.Insight(
                        packageName = foreground,
                        sender = SCREEN_SENDER,
                        content = content,
                        title = label,
                        text = content
                    )
                )
                ScreenCaptureStore.publish(
                    ScreenCapture(
                        packageName = foreground,
                        appLabel = label,
                        texts = texts,
                        trigger = CaptureTrigger.AMBIENT
                    )
                )
            } catch (_: Exception) {
                // Never crash the accessibility service; silently ignore.
            }
        }
    }

    private fun collectTexts(node: AccessibilityNodeInfo, limit: Int): List<String> {
        val out = LinkedHashSet<String>()
        traverse(node, out, limit, 0)
        return out.toList()
    }

    private fun traverse(
        node: AccessibilityNodeInfo,
        out: MutableSet<String>,
        limit: Int,
        depth: Int
    ) {
        if (out.size >= limit || depth > MAX_TRAVERSAL_DEPTH) return
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { out += it.take(MAX_LABEL_LENGTH) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotBlank() }
            ?.let { out += it.take(MAX_LABEL_LENGTH) }
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: Exception) { null } ?: continue
            traverse(child, out, limit, depth + 1)
        }
    }

    private fun appLabelFor(pkg: String): String {
        if (pkg.isBlank()) return "Screen"
        if (pkg == packageName) return "Dayflow"
        return try {
            val info = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            pkg.substringAfterLast('.')
        }
    }

    // ---------------------------------------------------------------------
    // Control layer: capture / scan / tap / swipe / back, always gated.
    // ---------------------------------------------------------------------

    private suspend fun handleRequest(request: ScreenActionBus.Request) {
        ScreenActionBus.beginHandling()
        try {
            val root = rootInActiveWindow
            val foreground = root?.packageName?.toString().orEmpty()
            val gate = safetyGate.checkPackage(foreground)
            if (!gate.allowed) {
                ScreenActionBus.report(
                    ScreenActionBus.ActionResult.Failed(
                        ScreenActionBus.Codes.GATE_BLOCKED,
                        listOf(appLabelFor(foreground))
                    )
                )
                return
            }

            when (request.action) {
                ScreenActionBus.Action.CAPTURE -> {
                    // Asked from inside Dayflow: understand the app the user came from
                    // (last observed screen), never Dayflow's own UI.
                    if (foreground == packageName) {
                        val previous = ScreenCaptureStore.lastAmbient?.takeIf { it.packageName != packageName }
                            ?: ScreenCaptureStore.history.value.firstOrNull { it.packageName != packageName }
                        if (previous == null) {
                            ScreenActionBus.report(ScreenActionBus.ActionResult.Failed(ScreenActionBus.Codes.CAPTURE_USE_BUBBLE))
                        } else {
                            ScreenCaptureStore.publish(previous.copy(id = java.util.UUID.randomUUID().toString(), trigger = request.trigger))
                            ScreenActionBus.report(
                                ScreenActionBus.ActionResult.Done(ScreenActionBus.Codes.CAPTURE_DONE, listOf(previous.texts.size.toString()))
                            )
                        }
                        return
                    }
                    if (root == null) {
                        ScreenActionBus.report(
                            ScreenActionBus.ActionResult.Failed(ScreenActionBus.Codes.CAPTURE_EMPTY)
                        )
                        return
                    }
                    extractor.resetIds()
                    val elements = extractor.extract()
                    val texts = collectTexts(root, MAX_CAPTURE_TEXTS)
                    val metrics = resources.displayMetrics
                    val capture = ScreenCapture(
                        packageName = foreground,
                        appLabel = appLabelFor(foreground),
                        texts = texts,
                        elements = elements,
                        screenWidth = metrics.widthPixels,
                        screenHeight = metrics.heightPixels,
                        trigger = request.trigger
                    )
                    if (capture.isEmpty) {
                        ScreenActionBus.report(
                            ScreenActionBus.ActionResult.Failed(ScreenActionBus.Codes.CAPTURE_EMPTY)
                        )
                        return
                    }
                    ScreenCaptureStore.publish(capture)
                    ScreenActionBus.report(
                        ScreenActionBus.ActionResult.Done(
                            ScreenActionBus.Codes.CAPTURE_DONE,
                            listOf(texts.size.toString())
                        )
                    )
                }

                ScreenActionBus.Action.SCAN -> {
                    extractor.resetIds()
                    val elements = extractor.extract()
                    ScreenActionBus.publishElements(elements)
                    ScreenActionBus.report(
                        ScreenActionBus.ActionResult.Done(
                            ScreenActionBus.Codes.SCAN_DONE,
                            listOf(elements.size.toString(), foreground)
                        )
                    )
                }

                ScreenActionBus.Action.TAP_ELEMENT -> {
                    val dispatched = gestures.clickElementById(request.elementId)
                    if (dispatched) {
                        ScreenActionBus.report(
                            ScreenActionBus.ActionResult.Done(
                                ScreenActionBus.Codes.TAP_DONE,
                                listOf(request.elementId.toString())
                            )
                        )
                        return
                    }
                    // Coordinate fallback via humanized gesture.
                    val element = ScreenActionBus.elements.value
                        .firstOrNull { it.id == request.elementId }
                    val ok = element != null &&
                        gestures.tap(element.centerX.toFloat(), element.centerY.toFloat())
                    ScreenActionBus.report(
                        if (ok) {
                            ScreenActionBus.ActionResult.Done(
                                ScreenActionBus.Codes.TAP_DONE,
                                listOf(request.elementId.toString())
                            )
                        } else {
                            ScreenActionBus.ActionResult.Failed(
                                ScreenActionBus.Codes.TAP_FAILED,
                                listOf(request.elementId.toString())
                            )
                        }
                    )
                }

                ScreenActionBus.Action.SWIPE_UP -> {
                    val m = resources.displayMetrics
                    val cx = m.widthPixels / 2f
                    val ok = gestures.swipe(cx, m.heightPixels * 0.75f, cx, m.heightPixels * 0.30f)
                    ScreenActionBus.report(
                        if (ok) ScreenActionBus.ActionResult.Done(ScreenActionBus.Codes.SWIPE_UP)
                        else ScreenActionBus.ActionResult.Failed(ScreenActionBus.Codes.SWIPE_REJECTED)
                    )
                }

                ScreenActionBus.Action.SWIPE_DOWN -> {
                    val m = resources.displayMetrics
                    val cx = m.widthPixels / 2f
                    val ok = gestures.swipe(cx, m.heightPixels * 0.30f, cx, m.heightPixels * 0.75f)
                    ScreenActionBus.report(
                        if (ok) ScreenActionBus.ActionResult.Done(ScreenActionBus.Codes.SWIPE_DOWN)
                        else ScreenActionBus.ActionResult.Failed(ScreenActionBus.Codes.SWIPE_REJECTED)
                    )
                }

                ScreenActionBus.Action.BACK -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    ScreenActionBus.report(ScreenActionBus.ActionResult.Done(ScreenActionBus.Codes.BACK))
                }
            }
        } catch (e: Exception) {
            ScreenActionBus.report(
                ScreenActionBus.ActionResult.Failed(
                    ScreenActionBus.Codes.ERROR,
                    listOf(e.message ?: "unknown")
                )
            )
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        connected.value = false
        shadeChips?.destroy()
        scope.cancel()
    }

    companion object {
        /** Live connection state for the UI (the service binds asynchronously after app start). */
        val connected = kotlinx.coroutines.flow.MutableStateFlow(false)
        const val SCREEN_SENDER = "Screen Intelligence"
        const val MAX_CONTENT_LENGTH = 500
        const val MIN_PUBLISH_INTERVAL_MS = 1_000L
        const val DEDUPE_WINDOW_MS = 10_000L
        const val MAX_INSIGHT_TEXTS = 60
        const val MAX_CAPTURE_TEXTS = 220
        const val MAX_TRAVERSAL_DEPTH = 40
        const val MAX_LABEL_LENGTH = 200

        /** Live service reference; null whenever the service is not connected. */
        @Volatile
        var instance: DayflowAccessibilityService? = null
            private set
    }
}
