package com.dayflow.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayflow.app.R
import com.dayflow.app.core.actions.SmartAction
import com.dayflow.app.core.actions.SmartActionPlanner
import com.dayflow.app.core.util.DateResolver
import com.dayflow.app.domain.model.DayflowAnalysisResult

/**
 * Human-in-the-loop review: every detected item can be unchecked or edited
 * before anything is saved, and proposed smart actions run only on a tap.
 */
@Composable
fun VerificationCard(
    result: DayflowAnalysisResult,
    onAccept: (DayflowAnalysisResult) -> Unit,
    onDismiss: () -> Unit,
    sender: String? = null
) {
    val language = LocalConfiguration.current.locales[0].language
    var current by remember(result.id) { mutableStateOf(result) }
    var excluded by remember(result.id) { mutableStateOf(setOf<String>()) }
    var editing by remember { mutableStateOf<EditTarget?>(null) }
    var showSource by remember { mutableStateOf(false) }
    val actions = remember(current) { SmartActionPlanner.plan(current, sender, language) }
    val empty = current.events.isEmpty() && current.tasks.isEmpty() && current.reminders.isEmpty()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.verify_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (current.hasError && empty) {
                Text(current.errorMessage ?: current.summaryText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            } else {
                if (current.summaryText.isNotBlank()) {
                    Text(current.summaryText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                if (current.insight.visible && current.insight.text.isNotBlank()) {
                    Text(current.insight.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                current.events.forEach { ev ->
                    ItemRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = stringResource(R.string.verify_event),
                        title = ev.title,
                        detail = listOf(ev.dateLabel, ev.time, ev.location.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "),
                        checked = ev.id !in excluded,
                        onCheck = { excluded = if (it) excluded - ev.id else excluded + ev.id },
                        onEdit = { editing = EditTarget(ev.id, ev.title, listOf(ev.dateLabel, ev.time).filter { s -> s.isNotBlank() }.joinToString(" "), ev.location.orEmpty(), true) }
                    )
                }
                current.tasks.forEach { tk ->
                    ItemRow(
                        icon = Icons.Outlined.CheckCircleOutline,
                        label = stringResource(R.string.verify_task),
                        title = tk.title,
                        detail = tk.dueDate.orEmpty(),
                        checked = tk.id !in excluded,
                        onCheck = { excluded = if (it) excluded - tk.id else excluded + tk.id },
                        onEdit = { editing = EditTarget(tk.id, tk.title, tk.dueDate.orEmpty(), "", false) }
                    )
                }
                current.reminders.forEach { rm ->
                    ItemRow(
                        icon = Icons.Outlined.NotificationsNone,
                        label = stringResource(R.string.verify_reminder),
                        title = rm.title,
                        detail = rm.timeLabel,
                        checked = rm.id !in excluded,
                        onCheck = { excluded = if (it) excluded - rm.id else excluded + rm.id },
                        onEdit = { editing = EditTarget(rm.id, rm.title, rm.timeLabel, "", false) }
                    )
                }
                if (current.inputSnippet.isNotBlank()) {
                    TextButton(onClick = { showSource = !showSource }) {
                        Text(stringResource(if (showSource) R.string.verify_hide_source else R.string.verify_show_source))
                    }
                    if (showSource) {
                        Text(current.inputSnippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (actions.isNotEmpty()) {
                    Text(stringResource(R.string.verify_actions), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    SmartActionChips(actions)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!empty) TextButton(onClick = onDismiss) { Text(stringResource(R.string.verify_discard)) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (empty) onDismiss() else onAccept(
                            current.copy(
                                events = current.events.filterNot { it.id in excluded },
                                tasks = current.tasks.filterNot { it.id in excluded },
                                reminders = current.reminders.filterNot { it.id in excluded }
                            )
                        )
                    },
                    enabled = empty || (current.events + current.tasks + current.reminders).size > excluded.size,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(if (empty) R.string.action_close else R.string.verify_confirm), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    editing?.let { target ->
        EditDialog(target, onDismiss = { editing = null }) { title, whenText, location ->
            val millis = DateResolver.resolveMillis(whenText)
            current = current.copy(
                events = current.events.map {
                    if (it.id != target.id) it else {
                        val parsedTime = DateResolver.findTime(whenText)?.label
                        val date = DateResolver.findDate(whenText)
                        it.copy(
                            title = title, location = location.ifBlank { null },
                            time = parsedTime ?: it.time,
                            dateLabel = date?.text?.replaceFirstChar { c -> c.titlecase() } ?: it.dateLabel,
                            startMillis = millis ?: it.startMillis
                        )
                    }
                },
                tasks = current.tasks.map { if (it.id != target.id) it else it.copy(title = title, dueDate = whenText.ifBlank { null }, dueMillis = millis ?: it.dueMillis) },
                reminders = current.reminders.map { if (it.id != target.id) it else it.copy(title = title, timeLabel = whenText, scheduledTimeMillis = millis ?: it.scheduledTimeMillis) }
            )
            editing = null
        }
    }
}

private data class EditTarget(val id: String, val title: String, val whenText: String, val location: String, val hasLocation: Boolean)

@Composable
private fun ItemRow(
    icon: ImageVector,
    label: String,
    title: String,
    detail: String,
    checked: Boolean,
    onCheck: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheck)
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.verify_edit), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditDialog(target: EditTarget, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(target.title) }
    var whenText by remember { mutableStateOf(target.whenText) }
    var location by remember { mutableStateOf(target.location) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.verify_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.verify_field_title)) }, singleLine = true)
                OutlinedTextField(whenText, { whenText = it }, label = { Text(stringResource(R.string.verify_field_when)) }, singleLine = true)
                if (target.hasLocation) OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.verify_field_location)) }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title.trim().ifBlank { target.title }, whenText.trim(), location.trim()) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

/** Chips for smart actions; message drafts open a confirmation dialog first. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartActionChips(actions: List<SmartAction>) {
    val context = LocalContext.current
    var draft by remember { mutableStateOf<SmartAction?>(null) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            val (icon, label) = when (action.kind) {
                SmartAction.Kind.OPEN_LINK -> Icons.Outlined.Link to stringResource(R.string.action_open_link)
                SmartAction.Kind.NAVIGATE -> Icons.Outlined.Map to stringResource(R.string.action_navigate)
                SmartAction.Kind.CALL -> Icons.Outlined.Call to stringResource(R.string.action_call)
                SmartAction.Kind.EMAIL -> Icons.Outlined.Email to stringResource(R.string.action_email)
                SmartAction.Kind.REPLY_DRAFT -> Icons.Outlined.Sms to stringResource(R.string.action_reply)
                SmartAction.Kind.LATE_DRAFT -> Icons.Outlined.Sms to stringResource(R.string.action_running_late)
            }
            AssistChip(
                onClick = { if (action.needsConfirmation) draft = action else SmartActionPlanner.launch(context, action) },
                label = { Text(label) },
                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) }
            )
        }
    }
    draft?.let { action -> DraftConfirmDialog(action, onDismiss = { draft = null }) }
}

/** "Draft preview · Needs confirmation" — the message is only handed to a messenger after Verify & Confirm. */
@Composable
fun DraftConfirmDialog(action: SmartAction, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var text by remember { mutableStateOf(action.draft) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.draft_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.draft_needs_confirmation), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                if (action.target.isNotBlank()) Text(stringResource(R.string.draft_to, action.target), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Text(stringResource(R.string.draft_trust_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = {
                SmartActionPlanner.launch(context, action, text)
                onDismiss()
            }) { Text(stringResource(R.string.draft_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
