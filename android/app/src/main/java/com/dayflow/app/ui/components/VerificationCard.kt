package com.dayflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Sparkles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.ButtonShape
import com.dayflow.app.core.designsystem.CardShape
import com.dayflow.app.core.designsystem.DayflowAccentGreen
import com.dayflow.app.core.designsystem.DayflowBorder
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowPrimarySoft
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.domain.model.DayflowAnalysisResult

@Composable
fun VerificationCard(
    result: DayflowAnalysisResult,
    onAddCalendar: () -> Unit,
    onAddTask: () -> Unit,
    onAddReminder: () -> Unit,
    onAddBoth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarAdded by remember { mutableStateOf(false) }
    var taskAdded by remember { mutableStateOf(false) }
    var reminderAdded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.White)
            .border(width = 1.dp, color = DayflowBorder, shape = CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Verification Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(ButtonShape)
                        .background(DayflowPrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sparkles,
                        contentDescription = null,
                        tint = DayflowPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.verification_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                    Text(
                        text = result.summary,
                        fontSize = 11.sp,
                        color = DayflowTextMuted,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Detected Events
        result.events.forEach { ev ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ButtonShape)
                    .background(Color(0xFFFAF6F0))
                    .border(width = 0.5.dp, color = DayflowBorder, shape = ButtonShape)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ev.dateLabel} · ${ev.time}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayflowPrimary
                        )
                        SourceBadge(label = stringResource(R.string.verification_event_label))
                    }
                    Text(
                        text = ev.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DayflowText
                    )
                    if (!ev.location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = DayflowPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ev.location,
                                fontSize = 11.sp,
                                color = DayflowTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Detected Tasks
        result.tasks.forEach { tk ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ButtonShape)
                    .background(Color(0xFFFAF6F0))
                    .border(width = 0.5.dp, color = DayflowBorder, shape = ButtonShape)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tk.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DayflowText
                        )
                        if (!tk.dueDate.isNullOrBlank()) {
                            Text(
                                text = tk.dueDate,
                                fontSize = 11.sp,
                                color = DayflowTextMuted
                            )
                        }
                    }
                    SourceBadge(label = stringResource(R.string.verification_task_label))
                }
            }
        }

        // Action Buttons with Success Feedback
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (result.events.isNotEmpty()) {
                DayflowPrimaryButton(
                    text = if (calendarAdded) stringResource(R.string.verification_btn_added) else stringResource(R.string.verification_btn_add_calendar),
                    icon = if (calendarAdded) Icons.Default.Check else Icons.Outlined.CalendarToday,
                    onClick = {
                        calendarAdded = true
                        onAddCalendar()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.tasks.isNotEmpty()) {
                    DayflowSecondaryButton(
                        text = if (taskAdded) stringResource(R.string.verification_btn_added) else stringResource(R.string.verification_btn_add_task),
                        icon = if (taskAdded) Icons.Default.Check else Icons.Outlined.CheckCircleOutline,
                        onClick = {
                            taskAdded = true
                            onAddTask()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (result.reminders.isNotEmpty()) {
                    DayflowSecondaryButton(
                        text = if (reminderAdded) stringResource(R.string.verification_btn_added) else stringResource(R.string.verification_btn_add_reminder),
                        icon = if (reminderAdded) Icons.Default.Check else Icons.Outlined.NotificationsNone,
                        onClick = {
                            reminderAdded = true
                            onAddReminder()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
