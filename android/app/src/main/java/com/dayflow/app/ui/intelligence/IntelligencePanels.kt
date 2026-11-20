package com.dayflow.app.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.integrations.accessibility.context.ScreenCapture
import com.dayflow.app.ui.components.DayflowCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Access & privacy block. Every capability that lets Dayflow read a screen is
 * visible here with its live state, and can be switched off in place. Nothing
 * is enabled behind the user's back.
 */
@Composable
fun AccessCard(
    accessibilityActive: Boolean,
    notificationAccess: Boolean,
    overlayPermission: Boolean,
    ambientObservation: Boolean,
    floatingBubble: Boolean,
    onOpenAccessibility: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onToggleAmbient: (Boolean) -> Unit,
    onToggleBubble: (Boolean) -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.intelligence_access_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AccessRow(
                icon = Icons.Outlined.Visibility,
                title = stringResource(R.string.intelligence_access_screen),
                desc = stringResource(R.string.intelligence_access_screen_desc),
                active = accessibilityActive,
                actionLabel = stringResource(
                    if (accessibilityActive) R.string.intelligence_access_active
                    else R.string.intelligence_access_enable
                ),
                onAction = onOpenAccessibility
            )

            AccessRow(
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.intelligence_access_notifications),
                desc = stringResource(R.string.intelligence_access_notifications_desc),
                active = notificationAccess,
                actionLabel = stringResource(
                    if (notificationAccess) R.string.intelligence_access_active
                    else R.string.intelligence_access_enable
                ),
                onAction = onOpenNotificationAccess
            )

            ToggleRow(
                icon = if (floatingBubble) Icons.Outlined.PictureInPictureAlt else Icons.Outlined.PictureInPictureAlt,
                title = stringResource(R.string.intelligence_bubble_title),
                desc = stringResource(
                    if (overlayPermission) R.string.intelligence_bubble_desc
                    else R.string.intelligence_bubble_desc_permission
                ),
                checked = floatingBubble,
                onToggle = onToggleBubble
            )

            ToggleRow(
                icon = if (ambientObservation) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                title = stringResource(R.string.intelligence_observe_title),
                desc = stringResource(R.string.intelligence_observe_desc),
                checked = ambientObservation,
                onToggle = onToggleAmbient
            )

            Text(
                text = stringResource(R.string.intelligence_privacy_note),
                style = MaterialTheme.typography.labelSmall,
                color = muted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun AccessRow(
    icon: ImageVector,
    title: String,
    desc: String,
    active: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onAction)
                .heightIn(min = 40.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

/** Natural-language goal → step-by-step on-screen execution. */
@Composable
fun AgentCard(
    goal: String,
    onGoalChange: (String) -> Unit,
    running: Boolean,
    steps: List<com.dayflow.app.integrations.accessibility.control.ScreenAgentBus.Step>,
    serviceActive: Boolean,
    onRun: () -> Unit,
    onStop: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.intelligence_agent_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.intelligence_agent_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = muted
                    )
                }
            }

            OutlinedTextField(
                value = goal,
                onValueChange = onGoalChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.intelligence_agent_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !running && serviceActive,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgentSuggestion(stringResource(R.string.intelligence_agent_suggest_scroll)) {
                    onGoalChange(it)
                }
                AgentSuggestion(stringResource(R.string.intelligence_agent_suggest_search)) {
                    onGoalChange(it)
                }
                AgentSuggestion(stringResource(R.string.intelligence_agent_suggest_back)) {
                    onGoalChange(it)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRun,
                    enabled = !running && goal.isNotBlank() && serviceActive,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp)
                ) {
                    Text(stringResource(R.string.intelligence_agent_run), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onStop,
                    enabled = running,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.7f)
                        .heightIn(min = 46.dp)
                ) {
                    Text(stringResource(R.string.intelligence_agent_stop))
                }
            }

            if (steps.isEmpty()) {
                Text(
                    text = stringResource(
                        if (serviceActive) R.string.intelligence_agent_empty
                        else R.string.intelligence_agent_no_service
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.takeLast(6).forEach { step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (step.done) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step.index.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (step.done) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = step.action,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (step.reason.isNotBlank()) {
                                    Text(
                                        text = step.reason,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentSuggestion(text: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable { onClick(text) }
            .heightIn(min = 36.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Recent captures, newest first. Tapping one re-runs detection on it. */
@Composable
fun HistorySection(
    history: List<ScreenCapture>,
    onReview: (ScreenCapture) -> Unit,
    onClear: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.intelligence_history_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (history.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.intelligence_history_clear),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClear)
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.intelligence_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
        } else {
            history.take(6).forEach { capture ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onReview(capture) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = capture.appLabel.ifBlank { capture.packageName },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = capture.text,
                            style = MaterialTheme.typography.labelSmall,
                            color = muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(capture.capturedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = muted
                    )
                }
            }
        }
    }
}
