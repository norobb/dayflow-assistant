package com.dayflow.app.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dayflow.app.R
import com.dayflow.app.integrations.notification.NotificationInsightBus
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowLogoHeader

/**
 * Screen intelligence: permissions, "understand this screen" with verified
 * proposals, the screen map (operate elements), the screen agent, recent
 * captures, and messages Dayflow understood from notifications.
 */
@Composable
fun IntelligenceScreen(
    viewModel: IntelligenceViewModel,
    onSettingsClick: () -> Unit,
    onReviewNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val notifications by NotificationInsightBus.history.collectAsState()
    var goal by rememberSaveable { mutableStateOf("") }

    // Permissions change in system settings; re-read them when the user comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshAccessState() }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DayflowLogoHeader(symbolSize = 32.dp)
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                Text(
                    stringResource(R.string.intelligence_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, lineHeight = 38.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stringResource(R.string.intelligence_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                AccessCard(
                    accessibilityActive = state.accessibilityActive,
                    notificationAccess = state.notificationAccess,
                    overlayPermission = state.overlayPermission,
                    ambientObservation = state.ambientObservation,
                    floatingBubble = state.floatingBubble,
                    onOpenAccessibility = viewModel::openAccessibilitySettings,
                    onOpenNotificationAccess = viewModel::openNotificationAccessSettings,
                    onToggleAmbient = viewModel::setAmbientObservation,
                    onToggleBubble = viewModel::setFloatingBubble
                )
            }
            item {
                CaptureCard(
                    capture = state.capture,
                    proposals = state.proposals,
                    addedIds = state.addedIds,
                    capturing = state.capturing,
                    analyzing = state.analyzing,
                    serviceActive = state.accessibilityActive,
                    showText = state.showText,
                    onCapture = viewModel::captureNow,
                    onAdd = viewModel::accept,
                    onAddAll = viewModel::acceptAll,
                    onToggleText = viewModel::toggleText
                )
            }
            val understood = notifications.filter { it.suggestionId != null }
            if (understood.isNotEmpty()) {
                item {
                    DayflowCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.intelligence_notif_section),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            understood.take(5).forEach { n ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(n.sender, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(n.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    TextButton(onClick = { onReviewNotification("${n.title}: ${n.text}") }) {
                                        Text(stringResource(R.string.notif_action_review))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                ScreenMapCard(
                    capture = state.capture,
                    elements = state.elements,
                    result = state.actionResult,
                    serviceActive = state.accessibilityActive,
                    onScan = viewModel::scan,
                    onTap = viewModel::tapElement
                )
            }
            item {
                AgentCard(
                    goal = goal,
                    onGoalChange = { goal = it },
                    running = state.agentRunning,
                    steps = state.agentSteps,
                    serviceActive = state.accessibilityActive,
                    onRun = { viewModel.runAgent(goal) },
                    onStop = viewModel::stopAgent
                )
            }
            item {
                HistorySection(history = state.history, onReview = viewModel::review, onClear = viewModel::clearHistory)
            }
            item { Spacer(Modifier.height(136.dp)) }
        }

        state.status?.let { status ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 120.dp),
                dismissAction = { TextButton(onClick = viewModel::dismissStatus) { Text(stringResource(R.string.action_close)) } }
            ) {
                Text(statusMessage(status))
            }
        }
    }
}

@Composable
private fun statusMessage(status: UiStatus): String = when (status.code) {
    IntelligenceViewModel.STATUS_ADDED -> stringResource(R.string.intel_added)
    IntelligenceViewModel.STATUS_ADDED_ALL -> stringResource(R.string.intel_status_added_all, status.args.firstOrNull() ?: "")
    IntelligenceViewModel.STATUS_NEED_ACCESS -> stringResource(R.string.intelligence_capture_need_access)
    IntelligenceViewModel.STATUS_BUBBLE_ON -> stringResource(R.string.intel_status_bubble_on)
    IntelligenceViewModel.STATUS_BUBBLE_OFF -> stringResource(R.string.intel_status_bubble_off)
    IntelligenceViewModel.STATUS_BUBBLE_PERMISSION -> stringResource(R.string.intelligence_bubble_desc_permission)
    else -> status.code
}
