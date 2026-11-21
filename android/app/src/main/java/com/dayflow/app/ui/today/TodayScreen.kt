package com.dayflow.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.DayflowApplication
import com.dayflow.app.R
import com.dayflow.app.core.actions.SmartAction
import com.dayflow.app.core.actions.SmartActionPlanner
import com.dayflow.app.core.util.DateResolver
import com.dayflow.app.domain.model.DayflowEvent
import com.dayflow.app.domain.model.DayflowInsight
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowLogoHeader
import com.dayflow.app.ui.components.DraftConfirmDialog
import com.dayflow.app.ui.components.SourceBadge
import com.dayflow.app.ui.components.VerificationCard
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onSettingsClick: () -> Unit,
    onViewTasks: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    val currentDateStr = SimpleDateFormat("EEEE, d. MMMM", locale).format(Date())
    var confirmReset by remember { mutableStateOf(false) }
    var lateDraft by remember { mutableStateOf<SmartAction?>(null) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayflowLogoHeader(symbolSize = 32.dp)
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            Text(
                stringResource(R.string.today_title),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                currentDateStr.replaceFirstChar { it.titlecase(locale) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (state.events.isEmpty() && state.tasks.isEmpty() && state.reminders.isEmpty()) {
                TodayEmptyState()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    state.insight?.let { insight ->
                        item(key = "insight") {
                            InsightBanner(insight, onViewTasks = onViewTasks, onSnooze = viewModel::snoozeInsight)
                        }
                    }
                    if (state.understood.isNotEmpty()) {
                        item(key = "understood") { UnderstoodSection(state.understood) }
                    }
                    if (state.events.isNotEmpty()) {
                        item(key = "h-timeline") { SectionTitle(stringResource(R.string.today_section_timeline)) }
                        items(state.events, key = { "e-" + it.id }) { event ->
                            TimelineEventCard(
                                event = event,
                                language = state.language,
                                onDelete = { viewModel.deleteEvent(event) },
                                onRunningLate = {
                                    lateDraft = SmartAction(
                                        SmartAction.Kind.LATE_DRAFT, "",
                                        SmartActionPlanner.lateDraft(event, null, state.language)
                                    )
                                }
                            )
                        }
                    }
                    if (state.tasks.isNotEmpty()) {
                        item(key = "h-tasks") { SectionTitle(stringResource(R.string.today_section_tasks)) }
                        items(state.tasks.sortedBy { it.completed }.take(8), key = { "t-" + it.id }) { task ->
                            TodayTaskCard(task, onToggle = { viewModel.toggleTask(task.id) })
                        }
                    }
                    if (state.reminders.isNotEmpty()) {
                        item(key = "h-reminders") { SectionTitle(stringResource(R.string.today_section_reminders)) }
                        items(state.reminders.take(8), key = { "r-" + it.id }) { reminder ->
                            ReminderRow(reminder, onDone = { viewModel.dismissReminder(reminder.id) })
                        }
                    }
                    item(key = "reset") {
                        TextButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.today_reset))
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (state.isAnalyzing) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, modifier = Modifier.padding(32.dp)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.today_understanding), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        state.pendingVerification?.let { result ->
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                VerificationCard(result = result, onAccept = viewModel::acceptVerification, onDismiss = viewModel::dismissVerification)
            }
        }

        if (state.failedCalendarEvents.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = viewModel::retryCalendarInsertion) { Text(stringResource(R.string.calendar_retry)) } },
                dismissAction = { TextButton(onClick = viewModel::dismissCalendarWarning) { Text(stringResource(R.string.calendar_dismiss)) } }
            ) {
                Text(pluralStringResource(R.plurals.calendar_open_failed, state.failedCalendarEvents.size, state.failedCalendarEvents.size))
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.today_reset)) },
            text = { Text(stringResource(R.string.today_reset_confirm)) },
            confirmButton = { TextButton(onClick = { viewModel.resetDay(); confirmReset = false }) { Text(stringResource(R.string.today_reset)) } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
    lateDraft?.let { DraftConfirmDialog(it, onDismiss = { lateDraft = null }) }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun TodayEmptyState() {
    Column(
        Modifier.fillMaxSize().padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.today_empty_title), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.today_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun InsightBanner(insight: DayflowInsight, onViewTasks: () -> Unit, onSnooze: () -> Unit) {
    DayflowCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(insight.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(2.dp))
                    Text(insight.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onSnooze) { Text(stringResource(R.string.today_insight_snooze)) }
                TextButton(onClick = onViewTasks) { Text(stringResource(R.string.today_insight_view_tasks)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnderstoodSection(items: List<UnderstoodItem>) {
    DayflowCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.understood_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(
                pluralStringResource(R.plurals.understood_count, items.size, items.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.take(12).forEach { item ->
                    val icon = when (item.kind) {
                        UnderstoodItem.Kind.EVENT -> Icons.Outlined.CalendarMonth
                        UnderstoodItem.Kind.TASK -> Icons.Outlined.CheckCircleOutline
                        UnderstoodItem.Kind.REMINDER -> Icons.Outlined.NotificationsNone
                    }
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${item.title} · ${sourceLabel(item.sourceType)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun sourceLabel(type: SourceType): String = stringResource(
    when (type) {
        SourceType.MESSAGE -> R.string.source_message
        SourceType.SCREENSHOT -> R.string.source_screenshot
        SourceType.PDF -> R.string.source_pdf
        SourceType.VOICE -> R.string.source_voice
        SourceType.CUSTOM -> R.string.source_text
        SourceType.DEFAULT -> R.string.source_manual
    }
)

@Composable
private fun TimelineEventCard(event: DayflowEvent, language: String, onDelete: () -> Unit, onRunningLate: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as DayflowApplication
    val zone = ZoneId.systemDefault()
    val day = event.startMillis?.let {
        DateResolver.friendlyLabel(Instant.ofEpochMilli(it).atZone(zone).toLocalDate(), LocalDate.now(zone), language)
    } ?: event.dateLabel
    val isRoutine = event.id.startsWith("routine-")
    val upcoming = (event.startMillis ?: 0) > System.currentTimeMillis()
    DayflowCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOf(day, event.time).filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary
                )
                SourceBadge(
                    label = when {
                        isRoutine -> stringResource(R.string.source_routine)
                        event.external -> stringResource(R.string.source_calendar)
                        else -> sourceLabel(event.sourceType)
                    }
                )
            }
            Text(event.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (!event.location.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!event.location.isNullOrBlank()) {
                    IconButton(onClick = { SmartActionPlanner.launch(context, SmartAction(SmartAction.Kind.NAVIGATE, event.location)) }) {
                        Icon(Icons.Outlined.Map, contentDescription = stringResource(R.string.action_navigate), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (upcoming) {
                    IconButton(onClick = onRunningLate) {
                        Icon(Icons.Outlined.Sms, contentDescription = stringResource(R.string.action_running_late), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                event.calendarEventId?.let { id ->
                    IconButton(onClick = { app.calendarService.openInCalendar(id) }) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = stringResource(R.string.action_open_calendar), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!event.external) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(task: DayflowTask, onToggle: () -> Unit) {
    DayflowCard(modifier = Modifier.clickable(onClickLabel = stringResource(R.string.task_toggle)) { onToggle() }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                if (task.completed) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = stringResource(if (task.completed) R.string.task_completed else R.string.task_open),
                tint = if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    task.title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (!task.dueDate.isNullOrBlank()) {
                    Text(task.dueDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SourceBadge(label = sourceLabel(task.sourceType))
        }
    }
}

@Composable
private fun ReminderRow(reminder: DayflowReminder, onDone: () -> Unit) {
    val fmt = remember { SimpleDateFormat("EEE HH:mm", java.util.Locale.getDefault()) }
    DayflowCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(fmt.format(Date(reminder.scheduledTimeMillis)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDone) { Text(stringResource(R.string.notif_action_done)) }
        }
    }
}
