package com.dayflow.app.integrations.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import com.dayflow.app.integrations.accessibility.DayflowAccessibilityService
import com.dayflow.app.integrations.notification.NotificationInsightBus
import com.dayflow.app.ui.components.DayflowMessageOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DayflowOverlayService : LifecycleService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    private var composeOwner: OverlayComposeOwner? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        scope.launch {
            NotificationInsightBus.latest.collectLatest { insight ->
                if (insight == null) {
                    removeOverlay()
                } else if (insight.sender != DayflowAccessibilityService.SCREEN_SENDER &&
                    insight.suggestionId != null && android.provider.Settings.canDrawOverlays(this@DayflowOverlayService)
                ) {
                    // Only messages in which Dayflow found something get a popup.
                    showOverlay(insight.sender, insight.text, insight.content, insight.suggestionId)
                } else {
                    removeOverlay()
                }
            }
        }
    }

    private fun accept(suggestionId: String) {
        removeOverlay()
        val app = application as com.dayflow.app.DayflowApplication
        val suggestion = com.dayflow.app.integrations.notification.PendingSuggestions.take(suggestionId) ?: return
        app.applicationScope.launch {
            app.itemCommitter.commit(suggestion.result)
            app.notificationHelper.cancel(suggestionId)
            app.soundAndHaptics.playSuccess()
        }
    }

    private fun showOverlay(sender: String, content: String, summary: String, suggestionId: String) {
        if (overlayView != null) {
            overlayView?.setContent {
                DayflowMessageOverlay(
                    messageSender = sender,
                    messageContent = content,
                    detectedSummary = summary,
                    visible = true,
                    onAccept = { accept(suggestionId) },
                    onDismiss = { removeOverlay() }
                )
            }
            return
        }

        val owner = OverlayComposeOwner().also {
            it.handleEvent(Lifecycle.Event.ON_CREATE)
            it.handleEvent(Lifecycle.Event.ON_START)
            it.handleEvent(Lifecycle.Event.ON_RESUME)
            composeOwner = it
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                DayflowMessageOverlay(
                    messageSender = sender,
                    messageContent = content,
                    detectedSummary = summary,
                    visible = true,
                    onAccept = { accept(suggestionId) },
                    onDismiss = { removeOverlay() }
                )
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }
        windowManager?.addView(view, params)
        overlayView = view
    }

    private fun removeOverlay() {
        overlayView?.let { v ->
            try { windowManager?.removeView(v) } catch (_: Exception) {}
        }
        overlayView = null
        composeOwner?.destroyAndClear()
        composeOwner = null
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        scope.cancel()
    }
}
