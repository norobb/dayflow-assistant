package com.dayflow.app.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.domain.model.DayflowReminder
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowPrimaryButton
import com.dayflow.app.ui.components.DayflowTopBar
import com.dayflow.app.ui.components.SourceBadge

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newTimeLabel by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Reminders Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.reminders_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayflowText
                        )
                        Text(
                            text = stringResource(R.string.reminders_count, reminders.size),
                            fontSize = 12.sp,
                            color = DayflowTextMuted
                        )
                    }

                    DayflowPrimaryButton(
                        text = stringResource(R.string.reminders_add_button),
                        icon = Icons.Default.Add,
                        onClick = { showAddDialog = true }
                    )
                }
            }

            if (reminders.isEmpty()) {
                item {
                    DayflowCard {
                        Text(
                            text = stringResource(R.string.reminders_empty_state),
                            fontSize = 13.sp,
                            color = DayflowTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderItemRow(
                        reminder = reminder,
                        onDismiss = { viewModel.dismissReminder(reminder.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.reminders_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = DayflowText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(stringResource(R.string.reminders_title_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTimeLabel,
                        onValueChange = { newTimeLabel = it },
                        label = { Text(stringResource(R.string.reminders_time_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addReminder(newTitle, newTimeLabel)
                        newTitle = ""
                        newTimeLabel = ""
                        showAddDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_save), color = DayflowPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = DayflowTextMuted)
                }
            }
        )
    }
}

@Composable
private fun ReminderItemRow(
    reminder: DayflowReminder,
    onDismiss: () -> Unit
) {
    DayflowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = DayflowPrimary,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DayflowText
                )
                Text(
                    text = reminder.timeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = DayflowPrimary
                )
            }

            SourceBadge(label = reminder.sourceType.identifier.replaceFirstChar { it.uppercase() })

            TextButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = DayflowPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(R.string.action_done),
                    color = DayflowPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
