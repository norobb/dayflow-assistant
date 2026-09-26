package com.dayflow.app.ui.tasks

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Circle
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
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.domain.model.DayflowTask
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowPrimaryButton
import com.dayflow.app.ui.components.DayflowTopBar
import com.dayflow.app.ui.components.SourceBadge

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf("") }

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

            // Tasks Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.tasks_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayflowText
                        )
                        Text(
                            text = stringResource(
                                R.string.tasks_completed_count,
                                tasks.count { it.completed },
                                tasks.size
                            ),
                            fontSize = 12.sp,
                            color = DayflowTextMuted
                        )
                    }

                    DayflowPrimaryButton(
                        text = stringResource(R.string.tasks_add_button),
                        icon = Icons.Default.Add,
                        onClick = { showAddDialog = true }
                    )
                }
            }

            if (tasks.isEmpty()) {
                item {
                    DayflowCard {
                        Text(
                            text = stringResource(R.string.tasks_empty_state),
                            fontSize = 13.sp,
                            color = DayflowTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTask(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) }
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
                    text = stringResource(R.string.tasks_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = DayflowText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(stringResource(R.string.tasks_title_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDueDate,
                        onValueChange = { newDueDate = it },
                        label = { Text(stringResource(R.string.tasks_due_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addTask(newTitle, newDueDate)
                        newTitle = ""
                        newDueDate = ""
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
private fun TaskItemRow(
    task: DayflowTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    DayflowCard(modifier = Modifier.clickable { onToggle() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (task.completed) DayflowAccentGreen else DayflowBorder,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.completed) DayflowTextMuted else DayflowText,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (!task.dueDate.isNullOrBlank()) {
                    Text(
                        text = task.dueDate,
                        fontSize = 11.sp,
                        color = DayflowTextMuted
                    )
                }
            }

            SourceBadge(label = task.sourceType.identifier.replaceFirstChar { it.uppercase() })

            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = DayflowTextMuted.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDelete() }
            )
        }
    }
}
