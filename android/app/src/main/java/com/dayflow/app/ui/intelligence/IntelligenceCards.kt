package com.dayflow.app.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.DayflowAccentAmber
import com.dayflow.app.core.designsystem.DayflowAccentGreen
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.integrations.accessibility.context.CandidateKind
import com.dayflow.app.integrations.accessibility.context.ScreenCandidate
import com.dayflow.app.integrations.accessibility.context.ScreenCapture
import com.dayflow.app.integrations.accessibility.control.ScreenActionBus
import com.dayflow.app.ui.components.SourceBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared label/icon/colour mapping for detected candidates. */
object CandidateStyle {

    fun icon(kind: CandidateKind): ImageVector = when (kind) {
        CandidateKind.EVENT -> Icons.Outlined.CalendarMonth
        CandidateKind.TASK -> Icons.Outlined.CheckCircleOutline
        CandidateKind.REMINDER -> Icons.Outlined.NotificationsNone
    }

    @Composable
    fun label(kind: CandidateKind): String = stringResource(
        when (kind) {
            CandidateKind.EVENT -> R.string.intelligence_proposal_event
            CandidateKind.TASK -> R.string.intelligence_proposal_task
            CandidateKind.REMINDER -> R.string.intelligence_proposal_reminder
        }
    )

    fun accent(kind: CandidateKind): Color = when (kind) {
        CandidateKind.EVENT -> Color(0xFF641C24)
        CandidateKind.TASK -> DayflowAccentGreen
        CandidateKind.REMINDER -> DayflowAccentAmber
    }
}

/**
 * The primary surface of the tab: read the current screen, show what was
 * understood, and let the user add it to their day one item at a time.
 */
@Composable
fun CaptureCard(
    capture: ScreenCapture?,
    proposals: List<ScreenCandidate>,
    addedIds: Set<String>,
    capturing: Boolean,
    analyzing: Boolean,
    serviceActive: Boolean,
    showText: Boolean,
    onCapture: () -> Unit,
    onAdd: (ScreenCandidate) -> Unit,
    onAddAll: () -> Unit,
    onToggleText: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surface)
            .border(1.dp, primary.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CandidateStyle.icon(CandidateKind.EVENT),
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.intelligence_capture_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurface
                )
                Text(
                    text = stringResource(R.string.intelligence_capture_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        androidx.compose.material3.Button(
            onClick = onCapture,
            enabled = serviceActive && !capturing,
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
        ) {
            if (capturing || analyzing) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.intelligence_capture_reading))
            } else {
                Text(
                    text = stringResource(R.string.intelligence_capture_action),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!serviceActive) {
            Text(
                text = stringResource(R.string.intelligence_capture_need_access),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (capture == null) {
            Text(
                text = stringResource(R.string.intelligence_capture_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CaptureResult(
                capture = capture,
                proposals = proposals,
                addedIds = addedIds,
                showText = showText,
                onAdd = onAdd,
                onAddAll = onAddAll,
                onToggleText = onToggleText
            )
        }
    }
}

@Composable
private fun CaptureResult(
    capture: ScreenCapture,
    proposals: List<ScreenCandidate>,
    addedIds: Set<String>,
    showText: Boolean,
    onAdd: (ScreenCandidate) -> Unit,
    onAddAll: () -> Unit,
    onToggleText: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(capture.capturedAt))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.intelligence_capture_from,
                    capture.appLabel.ifBlank { capture.packageName },
                    time
                ),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SourceBadge(label = stringResource(R.string.intelligence_source_screen))
        }

        if (proposals.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.intelligence_capture_empty_proposals),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.intelligence_capture_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
        } else {
            proposals.forEach { candidate ->
                ProposalRow(
                    candidate = candidate,
                    added = candidate.id in addedIds,
                    onAdd = { onAdd(candidate) }
                )
            }

            val pending = proposals.count { it.id !in addedIds }
            if (pending > 1) {
                androidx.compose.material3.Button(
                    onClick = onAddAll,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                ) {
                    Text(
                        text = stringResource(R.string.intelligence_proposal_add_all, pending),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = stringResource(
                if (showText) R.string.intelligence_capture_text_hide
                else R.string.intelligence_capture_text_show
            ),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleText)
                .heightIn(min = 40.dp)
                .padding(vertical = 10.dp)
        )

        if (showText) {
            Text(
                text = capture.text.take(900),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun ProposalRow(
    candidate: ScreenCandidate,
    added: Boolean,
    onAdd: () -> Unit
) {
    val accent = CandidateStyle.accent(candidate.kind)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CandidateStyle.icon(candidate.kind),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = candidate.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = CandidateStyle.label(candidate.kind),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
                if (candidate.scheduleLabel.isNotBlank()) {
                    Text("·", color = muted, fontSize = 11.sp)
                    Text(
                        text = candidate.scheduleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = muted
                    )
                }
                if (!candidate.location.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = muted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = candidate.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        if (added) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.intelligence_proposal_added),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onAdd)
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.intelligence_proposal_add),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accent
                )
            }
        }
    }
}

/**
 * A miniature, interactive screen: every interactive accessibility node is a
 * numbered rectangle at its real position. Tapping one performs the tap in
 * the target app. This is the honest version of an "AI overlay" — what the
 * service can actually see and touch, not a rendered screenshot.
 */
@Composable
fun ScreenMapCard(
    capture: ScreenCapture?,
    elements: List<ScreenActionBus.ScreenElement>,
    result: ScreenActionBus.ActionResult,
    serviceActive: Boolean,
    onScan: () -> Unit,
    onTap: (Int) -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.intelligence_map_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.intelligence_map_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
            if (serviceActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onScan)
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.intelligence_map_scan),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (elements.isEmpty()) {
            Text(
                text = stringResource(R.string.intelligence_map_empty),
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
        } else {
            ScreenMap(
                elements = elements,
                screenWidth = capture?.screenWidth?.takeIf { it > 0 } ?: 1080,
                screenHeight = capture?.screenHeight?.takeIf { it > 0 } ?: 2400,
                onTap = onTap
            )
            Text(
                text = stringResource(R.string.intelligence_map_legend, elements.size),
                style = MaterialTheme.typography.labelSmall,
                color = muted
            )
        }

        when (val r = result) {
            is ScreenActionBus.ActionResult.Done -> Text(
                text = resultText(r),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            is ScreenActionBus.ActionResult.Failed -> Text(
                text = resultText(r),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }
    }
}

@Composable
private fun ScreenMap(
    elements: List<ScreenActionBus.ScreenElement>,
    screenWidth: Int,
    screenHeight: Int,
    onTap: (Int) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val ratio = screenWidth.toFloat() / screenHeight.toFloat()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .height(340.dp)
                .aspectRatio(ratio)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    RoundedCornerShape(18.dp)
                )
        ) {
            val scaleX = maxWidth.value / screenWidth.toFloat()
            val scaleY = maxHeight.value / screenHeight.toFloat()
            elements.take(24).forEach { element ->
                val left = (element.left * scaleX).dp
                val top = (element.top * scaleY).dp
                val width = ((element.right - element.left) * scaleX).dp
                val height = ((element.bottom - element.top) * scaleY).dp
                if (width < 6.dp || height < 6.dp) return@forEach

                Box(
                    modifier = Modifier
                        .padding(start = left.coerceAtLeast(0.dp), top = top.coerceAtLeast(0.dp))
                        .size(width = width, height = height)
                        .clip(RoundedCornerShape(5.dp))
                        .background(primary.copy(alpha = 0.10f))
                        .border(1.dp, primary.copy(alpha = 0.45f), RoundedCornerShape(5.dp))
                        .clickable { onTap(element.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = element.id.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun resultText(result: ScreenActionBus.ActionResult): String = when (result) {
    is ScreenActionBus.ActionResult.Done ->
        stringResource(statusStringRes(result.code), *result.args.toTypedArray())
    is ScreenActionBus.ActionResult.Failed ->
        stringResource(statusStringRes(result.code), *result.args.toTypedArray())
    is ScreenActionBus.ActionResult.Running ->
        stringResource(R.string.intelligence_status_working)
    ScreenActionBus.ActionResult.Idle -> ""
}

/** Maps a [ScreenActionBus.ActionResult] code to its string resource. */
fun statusStringRes(code: String): Int = when (code) {
    ScreenActionBus.Codes.CAPTURE_DONE -> R.string.intelligence_status_captured
    ScreenActionBus.Codes.CAPTURE_EMPTY -> R.string.intelligence_status_capture_empty
    ScreenActionBus.Codes.CAPTURE_USE_BUBBLE -> R.string.intelligence_status_use_bubble
    ScreenActionBus.Codes.SCAN_DONE -> R.string.intelligence_status_scanned
    ScreenActionBus.Codes.TAP_DONE -> R.string.intelligence_status_tapped
    ScreenActionBus.Codes.TAP_FAILED -> R.string.intelligence_status_tap_failed
    ScreenActionBus.Codes.SWIPE_UP -> R.string.intelligence_status_swipe_up
    ScreenActionBus.Codes.SWIPE_DOWN -> R.string.intelligence_status_swipe_down
    ScreenActionBus.Codes.SWIPE_REJECTED -> R.string.intelligence_status_swipe_rejected
    ScreenActionBus.Codes.BACK -> R.string.intelligence_status_back
    ScreenActionBus.Codes.GATE_BLOCKED -> R.string.intelligence_status_gate
    else -> R.string.intelligence_status_error
}
