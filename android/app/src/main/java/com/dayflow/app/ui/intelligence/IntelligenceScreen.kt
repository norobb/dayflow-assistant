package com.dayflow.app.ui.intelligence

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dayflow.app.R
import com.dayflow.app.integrations.accessibility.control.ScreenActionBus
import com.dayflow.app.ui.components.ProductStateBadge
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowLogoHeader
import com.dayflow.app.ui.components.DayflowMessageOverlay
import com.dayflow.app.integrations.notification.NotificationInsightBus
import com.dayflow.app.integrations.accessibility.DayflowAccessibilityService
import com.dayflow.app.ui.components.DayflowSecondaryButton

@Composable
fun IntelligenceScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverlayDemo by remember { mutableStateOf(false) }

    // Pulsing animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val insights by NotificationInsightBus.history.collectAsState()
    val screenInsights = insights.filter { it.sender == DayflowAccessibilityService.SCREEN_SENDER }
    val notificationInsights = insights.filterNot { it.sender == DayflowAccessibilityService.SCREEN_SENDER }

    val screenElements by ScreenActionBus.elements.collectAsState()
    val actionResult by ScreenActionBus.result.collectAsState()
    val serviceActive = DayflowAccessibilityService.instance != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayflowLogoHeader(symbolSize = 32.dp)
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.intelligence_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Pulsing Coming Soon Badge
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .blur(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.intelligence_badge_beta),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Transparent
                        )
                    }
                    ProductStateBadge(text = stringResource(R.string.intelligence_badge_beta))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Overview Card with Blurred Pulsing Glow Backdrop
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Ambient blurred glow card behind overview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .scale(pulseScale)
                                .blur(24.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.4f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        )

                        DayflowCard {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.intelligence_headline),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.intelligence_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Screen Control panel (AURA-inspired control layer).
                item {
                    ScreenControlPanel(
                        elements = screenElements,
                        result = actionResult,
                        serviceActive = serviceActive
                    )
                }

                if (screenInsights.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.intelligence_screen_section),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            screenInsights.forEach { insight ->
                                DayflowCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = insight.packageName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = insight.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 4
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (notificationInsights.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.intelligence_notif_section),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            notificationInsights.forEach { insight ->
                                DayflowCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = insight.sender,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = insight.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.intelligence_flow_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StreamChannelCard(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            title = stringResource(R.string.intelligence_stream_chat),
                            desc = stringResource(R.string.intelligence_stream_chat_desc)
                        )
                        DayflowSecondaryButton(
                            text = "Test Message Overlay Preview",
                            icon = Icons.Outlined.ChatBubbleOutline,
                            onClick = { showOverlayDemo = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (showOverlayDemo) {
                            DayflowMessageOverlay(
                                messageSender = "Alex (WhatsApp)",
                                messageContent = "Hey! Let's meet at 18:00 tomorrow at Central Station Platform 4.",
                                visible = true,
                                onDismiss = { showOverlayDemo = false },
                                onAccept = { showOverlayDemo = false }
                            )
                        }
                    }
                }

                item {
                    StreamChannelCard(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = stringResource(R.string.intelligence_stream_receipts),
                        desc = stringResource(R.string.intelligence_stream_receipts_desc)
                    )
                }

                item {
                    StreamChannelCard(
                        icon = Icons.Outlined.Description,
                        title = stringResource(R.string.intelligence_stream_docs),
                        desc = stringResource(R.string.intelligence_stream_docs_desc)
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StreamChannelCard(
    icon: ImageVector,
    title: String,
    desc: String
) {    DayflowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * AURA-inspired control panel: scan the foreground screen into numbered
 * interactive elements, then act on them (tap / scroll / back) through the
 * accessibility service's humanized gesture pipeline. Requests flow through
 * [ScreenActionBus]; the safety gate inside the service blocks banking and
 * credential typing regardless of what this UI offers.
 */
@Composable
private fun ScreenControlPanel(
    elements: List<ScreenActionBus.ScreenElement>,
    result: ScreenActionBus.ActionResult,
    serviceActive: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.intelligence_control_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        DayflowCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!serviceActive) {
                    Text(
                        text = stringResource(R.string.intelligence_control_no_service),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayflowSecondaryButton(
                            text = stringResource(R.string.intelligence_control_scan),
                            icon = Icons.Outlined.AutoAwesome,
                            onClick = { ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SCAN)) },
                            modifier = Modifier.weight(1f)
                        )
                        DayflowSecondaryButton(
                            text = stringResource(R.string.intelligence_control_back),
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            onClick = { ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.BACK)) },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayflowSecondaryButton(
                            text = stringResource(R.string.intelligence_control_swipe_up),
                            icon = Icons.Outlined.AutoAwesome,
                            onClick = { ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SWIPE_UP)) },
                            modifier = Modifier.weight(1f)
                        )
                        DayflowSecondaryButton(
                            text = stringResource(R.string.intelligence_control_swipe_down),
                            icon = Icons.Outlined.AutoAwesome,
                            onClick = { ScreenActionBus.post(ScreenActionBus.Request(ScreenActionBus.Action.SWIPE_DOWN)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Action status line.
                    when (val r = result) {
                        is ScreenActionBus.ActionResult.Done -> Text(
                            text = r.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        is ScreenActionBus.ActionResult.Failed -> Text(
                            text = r.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        is ScreenActionBus.ActionResult.Running -> Text(
                            text = "…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        is ScreenActionBus.ActionResult.Idle -> {}
                    }

                    // Numbered interactive elements from the last scan.
                    if (elements.isEmpty()) {
                        if (result is ScreenActionBus.ActionResult.Done) {
                            Text(
                                text = stringResource(R.string.intelligence_control_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            elements.take(12).forEach { element ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${element.id}.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = element.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = stringResource(R.string.intelligence_control_tap),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                ScreenActionBus.post(
                                                    ScreenActionBus.Request(
                                                        ScreenActionBus.Action.TAP_ELEMENT,
                                                        element.id
                                                    )
                                                )
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.intelligence_control_safety),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
