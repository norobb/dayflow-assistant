package com.dayflow.app.integrations.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
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

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        scope.launch {
            NotificationInsightBus.latest.collectLatest { insight ->
                if (insight == null) {
                    removeOverlay()
                } else {
                    showOverlay(insight.sender, insight.content)
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
        val view = ComposeView(this).apply {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        scope.cancel()
    }
}
