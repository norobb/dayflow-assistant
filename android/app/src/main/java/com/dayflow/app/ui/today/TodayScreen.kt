package com.dayflow.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.ButtonShape
import com.dayflow.app.core.designsystem.CardShape
import com.dayflow.app.core.designsystem.DayflowAccentGreen
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowBorder
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowPrimarySoft
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowTopBar
import com.dayflow.app.ui.components.SourceBadge
import com.dayflow.app.ui.components.VerificationCard

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DayflowBg)
    ) {
        DayflowTopBar(onSettingsClick = onSettingsClick)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Verification surface (Human-in-the-Loop)
            item {
                AnimatedVisibility(
                    visible = uiState.pendingVerification != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    uiState.pendingVerification?.let { result ->
                        VerificationCard(
                            result = result,
                            onAddCalendar = { viewModel.addCalendarEvent(result) },
                            onAddTask = { viewModel.addTask(result) },
                            onAddReminder = { viewModel.addReminder(result) },
                            onAddBoth = { viewModel.addBoth(result) }
                        )
                    }
                }
            }

            // Proactive Context Insight Banner
            item {
                uiState.insight?.let { insight ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(DayflowPrimarySoft)
                            .border(width = 1.dp, color = DayflowPrimary.copy(alpha = 0.2f), shape = CardShape)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = insight.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DayflowPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = insight.text,
                                    fontSize = 11.sp,
                                    color = DayflowText.copy(alpha = 0.85f),
                                    lineHeight = 15.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = DayflowPrimary.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.snoozeInsight() }
                            )
                        }
                    }
                }
            }

            // Today Timeline Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_schedule),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                    Text(
                        text = "${uiState.events.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DayflowTextMuted
                    )
                }
            }

            if (uiState.events.isEmpty()) {
                item {
                    DayflowCard {
                        Text(
                            text = stringResource(R.string.today_no_events),
                            fontSize = 12.sp,
                            color = DayflowTextMuted
                        )
                    }
                }
            } else {
                items(uiState.events, key = { it.id }) { event ->
                    TimelineEventCard(event = event)
                }
            }

            // Tasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_pending_tasks),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                    Text(
                        text = "${uiState.tasks.count { it.completed }}/${uiState.tasks.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DayflowTextMuted
                    )
                }
            }

            items(uiState.tasks, key = { it.id }) { task ->
                TodayTaskCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task.id) }
                )
            }

            // Active Reminders Section
            if (uiState.reminders.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.today_active_reminders),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                }

                items(uiState.reminders, key = { it.id }) { reminder ->
                    DayflowCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = reminder.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DayflowText
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.NotificationsNone,
                                        contentDescription = null,
                                        tint = DayflowPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = reminder.timeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DayflowPrimary
                                    )
                                }
                            }
                            SourceBadge(label = reminder.sourceType.identifier.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun TimelineEventCard(event: DayflowEvent) {
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.time,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DayflowPrimary
                )
                SourceBadge(
                    label = if (event.sourceType == SourceType.DEFAULT) {
                        stringResource(R.string.today_scheduled_badge)
                    } else {
                        stringResource(R.string.today_organized_badge)
                    }
                )
            }
            Text(
                text = event.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DayflowText
            )
            if (!event.location.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = DayflowPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 11.sp,
                        color = DayflowTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    task: DayflowTask,
    onToggle: () -> Unit
) {
    DayflowCard(
        modifier = Modifier.clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (task.completed) "Completed" else "Incomplete",
                tint = if (task.completed) DayflowAccentGreen else DayflowBorder,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.completed) DayflowTextMuted else DayflowText,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (!task.dueDate.isNullOrBlank()) {
                    Text(
                        text = task.dueDate,
                        fontSize = 10.sp,
                        color = DayflowTextMuted
                    )
                }
            }
            SourceBadge(label = task.sourceType.identifier.replaceFirstChar { it.uppercase() })
        }
    }
}
