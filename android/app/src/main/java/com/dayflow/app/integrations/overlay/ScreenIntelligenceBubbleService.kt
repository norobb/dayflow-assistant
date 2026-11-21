package com.dayflow.app.integrations.overlay

import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dayflow.app.DayflowApplication
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.DayflowTheme
import com.dayflow.app.core.util.DayflowLocaleProvider
import com.dayflow.app.integrations.accessibility.DayflowAccessibilityService
import com.dayflow.app.integrations.accessibility.context.CaptureTrigger
import com.dayflow.app.integrations.accessibility.context.ScreenCandidate
import com.dayflow.app.integrations.accessibility.context.ScreenCandidateSaver
import com.dayflow.app.integrations.accessibility.context.ScreenIntelligenceRunner
import com.dayflow.app.integrations.accessibility.control.ScreenActionBus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Floating Dayflow bubble over other apps. Tapping it reads the current
 * screen (via the accessibility service), and shows what Dayflow understood
 * with an explicit "Add" per item — nothing is saved without a tap.
 */
/** Whether a Dayflow activity is on screen (set by [com.dayflow.app.MainActivity]). */
object AppVisibility {
    val inForeground = kotlinx.coroutines.flow.MutableStateFlow(false)
}

class ScreenIntelligenceBubbleService : LifecycleService() {

    private lateinit var windowManager: WindowManager
    private var view: ComposeView? = null
    private var owner: OverlayComposeOwner? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val app = application as DayflowApplication
        lifecycleScope.launch {
            val language = runCatching { app.preferencesRepository.getLanguage().first() }.getOrDefault("en")
            attach(language)
        }
    }

    private fun attach(language: String) {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 420
        }
        val o = OverlayComposeOwner().also {
            it.handleEvent(Lifecycle.Event.ON_CREATE)
            it.handleEvent(Lifecycle.Event.ON_START)
            it.handleEvent(Lifecycle.Event.ON_RESUME)
        }
        owner = o
        view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(o)
            setViewTreeViewModelStoreOwner(o)
            setViewTreeSavedStateRegistryOwner(o)
            setContent {
                DayflowTheme {
                    DayflowLocaleProvider(language) {
                        BubbleContent(
                            onDrag = { dx, dy ->
                                params.x = (params.x - dx.toInt()).coerceAtLeast(0)
                                params.y = (params.y + dy.toInt()).coerceAtLeast(0)
                                runCatching { windowManager.updateViewLayout(this, params) }
                            },
                            onCapture = ::capture,
                            onAdd = ::add,
                            onOpenAccessibility = {
                                runCatching {
                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }
                        )
                    }
                }
            }
        }
        runCatching { windowManager.addView(view, params) }.onFailure { stopSelf() }
    }

    private fun capture(): Boolean {
        if (DayflowAccessibilityService.instance == null) return false
        (application as DayflowApplication).soundAndHaptics.playClick()
        ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.CAPTURE, trigger = CaptureTrigger.BUBBLE))
        return true
    }

    private fun add(candidate: ScreenCandidate) {
        val app = application as DayflowApplication
        app.applicationScope.launch {
            ScreenCandidateSaver.save(candidate, app.itemCommitter)
            app.soundAndHaptics.playSuccess()
        }
    }

    override fun onDestroy() {
        view?.let { v -> runCatching { windowManager.removeView(v) } }
        view = null
        owner?.destroyAndClear()
        owner = null
        super.onDestroy()
    }
}

@Composable
private fun BubbleContent(
    onDrag: (Float, Float) -> Unit,
    onCapture: () -> Boolean,
    onAdd: (ScreenCandidate) -> Unit,
    onOpenAccessibility: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var noAccess by remember { mutableStateOf(false) }
    val added = remember { mutableStateOf(setOf<String>()) }
    val proposals by ScreenIntelligenceRunner.proposals.collectAsState()
    val analyzing by ScreenIntelligenceRunner.analyzing.collectAsState()
    val bubbleLabel = stringResource(R.string.bubble_understand_screen)
    // Inside Dayflow the Intelligence screen does the same job; keep the bubble out of the way.
    val dayflowInFront by AppVisibility.inForeground.collectAsState()
    if (dayflowInFront) return

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .semantics { contentDescription = bubbleLabel }
                .pointerInput(Unit) { detectDragGestures { change, drag -> change.consume(); onDrag(drag.x, drag.y) } }
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (expanded) {
                            expanded = false
                        } else {
                            added.value = emptySet()
                            noAccess = !onCapture()
                            expanded = true
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            com.dayflow.app.ui.components.DayflowSymbol(Modifier.size(30.dp), contentDescription = null)
        }
        if (expanded) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.bubble_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    when {
                        noAccess -> {
                            Text(stringResource(R.string.bubble_need_access), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = onOpenAccessibility) { Text(stringResource(R.string.intel_grant_access)) }
                        }
                        analyzing -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.share_analyzing), style = MaterialTheme.typography.bodySmall)
                        }
                        proposals.isEmpty() -> Text(
                            stringResource(R.string.bubble_nothing),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> proposals.take(4).forEach { c ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(c.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
                                    if (c.scheduleLabel.isNotBlank() || c.location != null) {
                                        Text(
                                            listOfNotNull(c.scheduleLabel.ifBlank { null }, c.location).joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                val done = c.id in added.value
                                TextButton(enabled = !done, onClick = { onAdd(c); added.value = added.value + c.id }) {
                                    Text(stringResource(if (done) R.string.intel_added else R.string.intel_add))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
