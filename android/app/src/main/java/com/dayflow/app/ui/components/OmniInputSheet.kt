package com.dayflow.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material.icons.outlined.Sparkles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.ButtonShape
import com.dayflow.app.core.designsystem.CardShape
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowBorder
import com.dayflow.app.core.designsystem.DayflowCard
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowPrimarySoft
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.core.designsystem.PillShape
import com.dayflow.app.domain.model.SourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniInputSheet(
    onDismiss: () -> Unit,
    onAnalyze: (SourceType, String?) -> Unit,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(SourceType.CUSTOM) }
    var inputText by remember { mutableStateOf("") }
    var isVoiceRecording by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DayflowBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.input_sheet_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DayflowText
                    )
                    Text(
                        text = stringResource(R.string.input_sheet_subtitle),
                        fontSize = 12.sp,
                        color = DayflowTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, DayflowBorder, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DayflowText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Input Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ButtonShape)
                    .background(Color.White)
                    .border(1.dp, DayflowBorder, ButtonShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    icon = Icons.Outlined.ShortText,
                    label = stringResource(R.string.input_tab_text),
                    selected = selectedTab == SourceType.CUSTOM,
                    onClick = { selectedTab = SourceType.CUSTOM },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    icon = Icons.Outlined.Image,
                    label = stringResource(R.string.input_tab_image),
                    selected = selectedTab == SourceType.SCREENSHOT,
                    onClick = {
                        selectedTab = SourceType.SCREENSHOT
                        inputText = "Doctor Appointment: Dr. Julia Stein — Zahnheilkunde — Tuesday 14:30"
                    },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.input_tab_pdf),
                    selected = selectedTab == SourceType.PDF,
                    onClick = {
                        selectedTab = SourceType.PDF
                        inputText = "Flight & Hotel Confirmation: Barcelona (BCN) · Oct 12 - 16 · Flight LH1812"
                    },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    icon = Icons.Outlined.Mic,
                    label = stringResource(R.string.input_tab_voice),
                    selected = selectedTab == SourceType.VOICE,
                    onClick = { selectedTab = SourceType.VOICE },
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Content
            when (selectedTab) {
                SourceType.VOICE -> {
                    // Voice Recorder UI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(CardShape)
                            .background(Color.White)
                            .border(1.dp, DayflowBorder, CardShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (isVoiceRecording) Color(0xFFC4384B) else DayflowPrimary)
                                    .clickable {
                                        isVoiceRecording = !isVoiceRecording
                                        if (!isVoiceRecording) {
                                            onAnalyze(SourceType.VOICE, null)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVoiceRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = if (isVoiceRecording) stringResource(R.string.input_voice_recording) else stringResource(R.string.input_voice_record),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DayflowText
                            )
                        }
                    }
                }
                else -> {
                    // Text / Document / Screenshot input field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(CardShape)
                            .background(Color.White)
                            .border(1.dp, DayflowBorder, CardShape)
                            .padding(14.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_text_hint),
                                fontSize = 13.sp,
                                color = DayflowTextMuted
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = DayflowText,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Quick Scenario Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Presets",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayflowTextMuted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        PresetChip(
                            label = stringResource(R.string.input_preset_message_label),
                            onClick = {
                                selectedTab = SourceType.MESSAGE
                                onAnalyze(SourceType.MESSAGE, null)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            label = stringResource(R.string.input_preset_screenshot_label),
                            onClick = {
                                selectedTab = SourceType.SCREENSHOT
                                onAnalyze(SourceType.SCREENSHOT, null)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            label = stringResource(R.string.input_preset_pdf_label),
                            onClick = {
                                selectedTab = SourceType.PDF
                                onAnalyze(SourceType.PDF, null)
                            }
                        )
                    }
                    item {
                        PresetChip(
                            label = stringResource(R.string.input_preset_voice_label),
                            onClick = {
                                selectedTab = SourceType.VOICE
                                onAnalyze(SourceType.VOICE, null)
                            }
                        )
                    }
                }
            }

            // Understand Action
            if (selectedTab != SourceType.VOICE) {
                DayflowPrimaryButton(
                    text = if (isAnalyzing) stringResource(R.string.input_action_analyzing) else stringResource(R.string.input_action_understand),
                    icon = Icons.Outlined.Sparkles,
                    isLoading = isAnalyzing,
                    onClick = {
                        val type = if (inputText.isNotBlank()) selectedTab else SourceType.MESSAGE
                        onAnalyze(type, inputText.ifBlank { null })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) DayflowPrimarySoft else Color.Transparent
    val contentColor = if (selected) DayflowPrimary else DayflowTextMuted

    Row(
        modifier = modifier
            .clip(ButtonShape)
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(Color.White)
            .border(1.dp, DayflowBorder, PillShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = DayflowText
        )
    }
}
