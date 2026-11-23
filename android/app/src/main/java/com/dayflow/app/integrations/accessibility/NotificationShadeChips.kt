package com.dayflow.app.integrations.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import com.dayflow.app.MainActivity
import com.dayflow.app.R
import com.dayflow.app.integrations.notification.NotificationHelper

/**
 * No-root mode: while the notification shade is open, pins a small "Dayflow" chip to the
 * bottom-right edge of every notification. Tapping it opens Dayflow's review screen with
 * that notification's text. Rows that already carry the root (LSPosed) button are skipped.
 * Text is only read to place the chips; nothing is stored until the user taps.
 */
class NotificationShadeChips(private val service: AccessibilityService) {

    @Volatile var enabled = true
    @Volatile var accentArgb = 0xFF641C24.toInt()

    private val wm = service.getSystemService(WindowManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val chips = mutableListOf<View>()
    private var shown: List<Pair<Rect, String>> = emptyList()
    private val refresh = Runnable { update() }

    fun onEvent(event: AccessibilityEvent) {
        val fromShade = event.packageName == SYSTEMUI
        if (!fromShade && event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED && chips.isEmpty()) return
        // Chips would trail behind a scroll; hide them and re-place once it settles.
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) clear()
        handler.removeCallbacks(refresh)
        handler.postDelayed(refresh, 150)
    }

    fun destroy() {
        handler.removeCallbacks(refresh)
        clear()
    }

    private fun update() {
        val targets = if (enabled) findTargets() else emptyList()
        // Our own chip windows fire TYPE_WINDOWS_CHANGED too: only touch views when something moved.
        if (targets == shown) return
        clear()
        targets.forEach { (bounds, text) -> addChip(bounds, text) }
        shown = targets
    }

    private fun findTargets(): List<Pair<Rect, String>> {
        val out = mutableListOf<Pair<Rect, String>>()
        val shade = service.windows.firstOrNull {
            it.type == AccessibilityWindowInfo.TYPE_SYSTEM && it.root?.packageName == SYSTEMUI
        }?.root ?: return out
        val stack = shade.findAccessibilityNodeInfosByViewId("$SYSTEMUI:id/notification_stack_scroller").firstOrNull() ?: return out
        val visible = Rect().also { stack.getBoundsInScreen(it) }
        for (i in 0 until stack.childCount) {
            val row = stack.getChild(i) ?: continue
            val bounds = Rect().also { row.getBoundsInScreen(it) }
            if (bounds.height() < dp(56) || bounds.bottom > visible.bottom || bounds.top < visible.top) continue
            val texts = LinkedHashSet<String>().also { collect(row, it, 0) }
            // Section headers / footer buttons have no real content; root mode already has its own button.
            if (texts.size < 2 || texts.any { "Dayflow" in it }) continue
            out += bounds to texts.joinToString("\n").take(4000)
        }
        return out
    }

    private fun collect(node: AccessibilityNodeInfo, out: MutableSet<String>, depth: Int) {
        if (depth > 12 || out.size >= 20) return
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { out += it }
        for (i in 0 until node.childCount) node.getChild(i)?.let { collect(it, out, depth + 1) }
    }

    private fun addChip(row: Rect, text: String) {
        val chip = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(10), 0)
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(accentArgb) }
            elevation = dp(3).toFloat()
            contentDescription = service.getString(R.string.shade_chip_label)
            addView(ImageView(service).apply {
                setImageResource(R.drawable.dayflow_symbol_mono)
                setColorFilter(0xFFFFFFFF.toInt())
            }, LinearLayout.LayoutParams(dp(14), dp(14)).apply { marginEnd = dp(5) })
            addView(TextView(service).apply {
                setText(R.string.shade_chip_short)
                setTextColor(0xFFFFFFFF.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            setOnClickListener { open(text) }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, dp(28),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            // Straddle the row's bottom edge on the right, clear of the large icon and expand chevron.
            x = service.resources.displayMetrics.widthPixels - row.right + dp(14)
            y = row.bottom - dp(14)
        }
        runCatching { wm.addView(chip, params); chips += chip }
    }

    private fun open(text: String) {
        service.startActivity(Intent(service, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationHelper.EXTRA_REVIEW_TEXT, text)
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
        }
        clear()
    }

    private fun clear() {
        chips.forEach { runCatching { wm.removeView(it) } }
        chips.clear()
        shown = emptyList()
    }

    private fun dp(v: Int) = (v * service.resources.displayMetrics.density).toInt()

    private companion object {
        const val SYSTEMUI = "com.android.systemui"
    }
}
