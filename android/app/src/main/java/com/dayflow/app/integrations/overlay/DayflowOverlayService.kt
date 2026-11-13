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

    /**
     * Compose in a Service context needs a real LifecycleOwner / ViewModelStoreOwner /
     * SavedStateRegistryOwner attached to the view, otherwise attaching the ComposeView
     * crashes with "ViewTreeLifecycleOwner not found". This tiny owner provides that
     * plumbing for overlay views added via WindowManager.
     */
    private class OverlayComposeOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val store = ViewModelStore()
        private val savedStateController = SavedStateRegistryController.create(this)

        init {
            savedStateController.performRestore(null)
        }

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val viewModelStore: ViewModelStore get() = store
        override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

        fun handleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        fun destroyAndClear() {
            handleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        }
    }

    private var composeOwner: OverlayComposeOwner? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        scope.launch {
            NotificationInsightBus.latest.collectLatest { insight ->
                if (insight == null) {
                    removeOverlay()
                } else if (insight.sender != DayflowAccessibilityService.SCREEN_SENDER) {
                    // Only ambient message overlays are shown as popups; screen
                    // intelligence insights are surfaced in the Intelligence tab.
                    showOverlay(insight.sender, insight.content)
                } else {
                    removeOverlay()
                }
            }
        }
    }

    private fun showOverlay(sender: String, content: String) {
        if (overlayView != null) {
            overlayView?.setContent {
                DayflowMessageOverlay(
                    messageSender = sender,
                    messageContent = content,
                    visible = true,
                    onAccept = { removeOverlay() },
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
                    visible = true,
                    onAccept = { removeOverlay() },
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
