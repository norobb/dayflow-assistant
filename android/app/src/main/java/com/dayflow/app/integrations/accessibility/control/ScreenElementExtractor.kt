package com.dayflow.app.integrations.accessibility.control

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Extracts the actionable elements of the active window as a numbered list.
 *
 * Re-implements the pattern from AURA's UITreeExtractor: bounded depth-first
 * traversal, an element budget so huge screens can't stall the service, and
 * visibility filtering via screen bounds. Only interactive nodes (clickable /
 * long-clickable / editable) are kept, each reduced to its label and center
 * point — the "numbered elements" contract the control layer acts on.
 */
class ScreenElementExtractor(
    private val service: AccessibilityService,
    private val ownPackage: () -> String
) {

    /** Element ids are stable within one scan session and increment per scan. */
    private var nextId = 1

    fun resetIds() {
        nextId = 1
    }

    /**
     * Walks the active window tree and returns numbered interactive elements.
     * Returns an empty list when no window content can be retrieved.
     *
     * Our own activity window is indexed when it is the foreground window —
     * control must also work inside Dayflow itself. Only Dayflow's floating
     * overlay windows are filtered, mirroring AURA's OwnAppDetector intent:
     * never perceive our own popup as part of the app being driven.
     */
    fun extract(): List<ScreenActionBus.ScreenElement> {
        val root = service.rootInActiveWindow ?: return emptyList()
        val own = ownPackage()
        val includeOwn = root.packageName?.toString() == own
        val screenBounds = Rect().also { service.displayBounds(it) }
        val out = mutableListOf<ScreenActionBus.ScreenElement>()
        walk(root, screenBounds, out, 0, own, includeOwn)
        return out
    }

    private fun walk(
        node: AccessibilityNodeInfo,
        screenBounds: Rect,
        out: MutableList<ScreenActionBus.ScreenElement>,
        depth: Int,
        own: String,
        includeOwn: Boolean
    ) {
        if (out.size >= MAX_ELEMENTS) return
        if (depth > MAX_DEPTH) return

        val bounds = Rect().also { node.getBoundsInScreen(it) }

        if (isInteractive(node) && Rect.intersects(bounds, screenBounds) && bounds.width() > 0) {
            // Skip our own package unless we are driving our own screen —
            // Dayflow never indexes its own floating overlay windows.
            val nodePackage = node.packageName?.toString().orEmpty()
            if (includeOwn || nodePackage != own) {
                out += ScreenActionBus.ScreenElement(
                    id = nextId++,
                    label = labelFor(node),
                    className = node.className?.toString().orEmpty(),
                    isEditable = node.isEditable,
                    centerX = bounds.centerX(),
                    centerY = bounds.centerY()
                )
            }
        }

        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: Exception) { null } ?: continue
            walk(child, screenBounds, out, depth + 1, own, includeOwn)
        }
    }

    private fun isInteractive(node: AccessibilityNodeInfo): Boolean {
        return node.isClickable || node.isLongClickable || node.isEditable
    }

    private fun labelFor(node: AccessibilityNodeInfo): String {
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank()) return text.take(MAX_LABEL_LENGTH)
        val desc = node.contentDescription?.toString()?.trim()
        if (!desc.isNullOrBlank()) return desc.take(MAX_LABEL_LENGTH)
        val hint = node.hintText?.toString()?.trim()
        if (!hint.isNullOrBlank()) return hint.take(MAX_LABEL_LENGTH)
        // Fall back to a readable class name, e.g. "ImageButton".
        val cls = node.className?.toString()?.substringAfterLast('.').orEmpty()
        return if (cls.isBlank()) "element" else cls
    }

    private fun AccessibilityService.displayBounds(out: Rect) {
        val metrics = resources.displayMetrics
        out.set(0, 0, metrics.widthPixels, metrics.heightPixels)
    }

    companion object {
        const val MAX_DEPTH = 60
        const val MAX_ELEMENTS = 200
        const val MAX_LABEL_LENGTH = 80
    }
}
