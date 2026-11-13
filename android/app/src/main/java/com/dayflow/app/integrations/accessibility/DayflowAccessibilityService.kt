package com.dayflow.app.integrations.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
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
 * Screen Intelligence service. Reads the active window's text content via
 * AccessibilityNodeInfo traversal and publishes understood insights to the
 * shared [NotificationInsightBus], where the Intelligence screen and the
 * Message Overlay observe them.
 *
 * Since the control layer was added (AURA-inspired, re-implemented natively),
 * this service also owns [ScreenElementExtractor], [DayflowGestureController]
 * and the [DayflowSafetyGate]: the Intelligence screen asks for scans, taps
 * and swipes through [ScreenActionBus], and this service performs them —
 * only after the gate approves the foreground package.
 */
class DayflowAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastPublishedContent: String? = null
    private var lastPublishAt: Long = 0L

    private lateinit var extractor: ScreenElementExtractor
    private lateinit var gestures: DayflowGestureController
    private val safetyGate = DayflowSafetyGate()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        extractor = ScreenElementExtractor(this) { packageName }
        gestures = DayflowGestureController(this)

        scope.launch {
            ScreenActionBus.requests.collectLatest { request ->
                if (request != null) handleRequest(request)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        // Never perceive our own screens — Dayflow's UI is not "context".
        val pkg = event.packageName?.toString().orEmpty()
        if (pkg == packageName) return

        scope.launch {
            try {
                val root: AccessibilityNodeInfo = rootInActiveWindow ?: return@launch
                val texts = mutableListOf<String>()
                traverse(root, texts)
                if (texts.isEmpty()) return@launch

                // Deduplicate labels while preserving order, then cap the payload.
                val content = texts.distinct().joinToString(" ").take(MAX_CONTENT_LENGTH)
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

                val foreground = pkg.ifBlank { "screen" }
                if (!safetyGate.checkPackage(foreground).allowed) {
                    // Restricted app (banking/auth): stay passive, no insight.
                    return@launch
                }

                NotificationInsightBus.publish(
                    NotificationInsightBus.Insight(
                        packageName = foreground,
                        sender = SCREEN_SENDER,
                        content = content,
                        title = "Screen understood",
                        text = content
                    )
                )
            } catch (_: Exception) {
                // Never crash the accessibility service; silently ignore.
            }
        }
    }

    private fun traverse(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverse(child, out)
        }
    }

    // ---------------------------------------------------------------------
    // Control layer: scan / tap / swipe / back, always gated.
    // ---------------------------------------------------------------------

    private suspend fun handleRequest(request: ScreenActionBus.Request) {
        ScreenActionBus.beginHandling()
        try {
            val root = rootInActiveWindow
            val foreground = root?.packageName?.toString().orEmpty()
            val gate = safetyGate.checkPackage(foreground)
            if (!gate.allowed) {
                ScreenActionBus.report(ScreenActionBus.ActionResult.Failed(gate.reason))
                return
            }

            when (request.action) {
                ScreenActionBus.Action.SCAN -> {
                    extractor.resetIds()
                    val elements = extractor.extract()
                    ScreenActionBus.publishElements(elements)
                    ScreenActionBus.report(
                        ScreenActionBus.ActionResult.Done(
                            "scanned ${elements.size} elements in $foreground"
                        )
                    )
                }

                ScreenActionBus.Action.TAP_ELEMENT -> {
                    val dispatched = gestures.clickElementById(request.elementId)
                    val message = if (dispatched) {
                        "tapped element #${request.elementId} (node action)"
                    } else {
                        // Coordinate fallback via humanized gesture.
                        val element = ScreenActionBus.elements.value
                            .firstOrNull { it.id == request.elementId }
                        if (element != null && gestures.tap(element.centerX.toFloat(), element.centerY.toFloat())) {
                            "tapped element #${request.elementId} (gesture)"
                        } else {
                            ScreenActionBus.report(
                                ScreenActionBus.ActionResult.Failed("could not tap element #${request.elementId}")
                            )
                            return
                        }
                    }
                    ScreenActionBus.report(ScreenActionBus.ActionResult.Done(message))
                }

                ScreenActionBus.Action.SWIPE_UP -> {
                    val m = resources.displayMetrics
                    val cx = m.widthPixels / 2f
                    val ok = gestures.swipe(cx, m.heightPixels * 0.75f, cx, m.heightPixels * 0.30f)
                    ScreenActionBus.report(
                        if (ok) ScreenActionBus.ActionResult.Done("swiped up")
                        else ScreenActionBus.ActionResult.Failed("swipe rejected")
                    )
                }

                ScreenActionBus.Action.SWIPE_DOWN -> {
                    val m = resources.displayMetrics
                    val cx = m.widthPixels / 2f
                    val ok = gestures.swipe(cx, m.heightPixels * 0.30f, cx, m.heightPixels * 0.75f)
                    ScreenActionBus.report(
                        if (ok) ScreenActionBus.ActionResult.Done("swiped down")
                        else ScreenActionBus.ActionResult.Failed("swipe rejected")
                    )
                }

                ScreenActionBus.Action.BACK -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    ScreenActionBus.report(ScreenActionBus.ActionResult.Done("back"))
                }
            }
        } catch (e: Exception) {
            ScreenActionBus.report(
                ScreenActionBus.ActionResult.Failed("error: ${e.message ?: "unknown"}")
            )
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
    }

    companion object {
        const val SCREEN_SENDER = "Screen Intelligence"
        const val MAX_CONTENT_LENGTH = 500
        const val MIN_PUBLISH_INTERVAL_MS = 1_000L
        const val DEDUPE_WINDOW_MS = 10_000L

        /** Live service reference; null whenever the service is not connected. */
        @Volatile
        var instance: DayflowAccessibilityService? = null
            private set
    }
}
