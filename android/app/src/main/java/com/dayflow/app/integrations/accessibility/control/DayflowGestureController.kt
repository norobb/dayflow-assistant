package com.dayflow.app.integrations.accessibility.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.random.Random

/**
 * Injects taps, long-presses and swipes through the accessibility service.
 *
 * AURA's GestureBuilder/GestureDispatcher pattern, re-implemented: humanized
 * motion by default, one continuous stroke per gesture, and — mirroring
 * AURA's SemanticClicker escape hatch — a node-based [ACTION_CLICK] fallback
 * for apps (common on heavily skinned OEM builds) whose input pipeline filters
 * accessibility-injected touches.
 */
class DayflowGestureController(
    private val service: AccessibilityService,
    private val random: Random = Random.Default
) {

    /**
     * Taps at screen coordinates with humanized jitter. Returns false if the
     * OS rejected the gesture; the caller may then try the node fallback.
     */
    fun tap(x: Float, y: Float): Boolean {
        val p = HumanizedMotion.tapPoint(x, y, random)
        val stroke = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    pathOf(listOf(p, p)), 0L, TAP_DURATION_MS
                )
            )
            .build()
        return dispatch(stroke)
    }

    fun longPress(x: Float, y: Float): Boolean {
        val p = HumanizedMotion.tapPoint(x, y, random)
        val stroke = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    pathOf(listOf(p, p)), 0L, LONG_PRESS_DURATION_MS
                )
            )
            .build()
        return dispatch(stroke)
    }

    /** Humanized curved swipe. Returns false if the OS rejected the stroke. */
    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val path = HumanizedMotion.swipePath(x1, y1, x2, y2, random)
        val segments = HumanizedMotion.swipeSegments(path)
        if (segments.isEmpty()) return false

        val builder = GestureDescription.Builder()
        var startTime = 0L
        var cursor = path.first()
        var continuousStroke: GestureDescription.StrokeDescription? = null

        for ((end, duration) in segments) {
            val stroke = GestureDescription.StrokeDescription(
                pathOf(listOf(cursor, end)), startTime, duration
            )
            continuousStroke = stroke
            builder.addStroke(stroke)
            startTime += duration
            cursor = end
        }
        return dispatch(builder.build())
    }

    /**
     * Node-level click fallback for the element with id [elementId], using the
     * snapshot provided by [ScreenActionBus]. Mirrors AURA's SemanticClicker:
     * when coordinate dispatch is filtered by the app, ACTION_CLICK still
     * navigates because it is answered by the app process itself.
     */
    fun clickElementById(elementId: Int): Boolean {
        val element = ScreenActionBus.elements.value
            .firstOrNull { it.id == elementId } ?: return false
        val root = service.rootInActiveWindow ?: return false
        val node = findNodeAt(root, element.centerX, element.centerY) ?: return false
        return try {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } catch (_: Exception) {
            false
        } finally {
            try { node.recycle() } catch (_: Exception) {}
        }
    }

    private fun findNodeAt(
        node: AccessibilityNodeInfo,
        x: Int,
        y: Int
    ): AccessibilityNodeInfo? {
        if (node.isClickable && boundsContain(node, x, y)) return node
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: Exception) { null } ?: continue
            if (boundsContain(child, x, y)) {
                val found = findNodeAt(child, x, y)
                if (found != null) return found
            }
        }
        return null
    }

    private fun boundsContain(node: AccessibilityNodeInfo, x: Int, y: Int): Boolean {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        return rect.contains(x, y)
    }

    private fun dispatch(description: GestureDescription): Boolean {
        return service.dispatchGesture(description, null, null)
    }

    private fun pathOf(points: List<HumanizedMotion.Point>): Path {
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (p in points.drop(1)) path.lineTo(p.x, p.y)
        return path
    }

    companion object {
        const val TAP_DURATION_MS = 60L
        const val LONG_PRESS_DURATION_MS = 500L
    }
}
