package com.dayflow.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.domain.model.SourceType
import com.dayflow.app.integrations.document.DocumentParser
import com.dayflow.app.integrations.voice.VoiceRecorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniInputSheet(
    onDismiss: () -> Unit,
    onAnalyze: (SourceType, String?) -> Unit,
    isAnalyzing: Boolean = false
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(SourceType.CUSTOM) }
    var inputText by remember { mutableStateOf("") }
    var isVoiceRecording by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val voiceRecorder = remember { VoiceRecorder(context) }
    val documentParser = remember { DocumentParser(context) }

    // Launcher for selecting screenshot / photo
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = documentParser.getFileName(uri)
            inputText = "Picked Image: ${selectedFileName}\nReceipt / Appointment Screenshot detected."
        }
    }

    // Launcher for selecting document / PDF
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = documentParser.getFileName(uri)
            val extracted = documentParser.extractSnippet(uri)
            inputText = "Picked Document: ${selectedFileName}\n$extracted"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (voiceRecorder.isRecording.value) {
                voiceRecorder.cancelRecording()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (voiceRecorder.isRecording.value) voiceRecorder.cancelRecording()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.input_sheet_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    icon = Icons.AutoMirrored.Outlined.ShortText,
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
                        if (inputText.isBlank()) {
                            inputText = "Doctor Appointment: Dr. Julia Stein — Zahnheilkunde — Tuesday 14:30"
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.input_tab_pdf),
                    selected = selectedTab == SourceType.PDF,
                    onClick = {
                        selectedTab = SourceType.PDF
                        if (inputText.isBlank()) {
                            inputText = "Flight & Hotel Confirmation: Barcelona (BCN) · Oct 12 - 16 · Flight LH1812"
                        }
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
                SourceType.SCREENSHOT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayflowSecondaryButton(
                            text = selectedFileName ?: stringResource(R.string.input_pick_image),
                            icon = Icons.Outlined.UploadFile,
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                SourceType.PDF -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayflowSecondaryButton(
                            text = selectedFileName ?: stringResource(R.string.input_pick_pdf),
                            icon = Icons.Outlined.UploadFile,
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                SourceType.VOICE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
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
                                    .background(if (isVoiceRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (!isVoiceRecording) {
                                            val started = voiceRecorder.startRecording()
                                            isVoiceRecording = started
                                        } else {
                                            val audioPath = voiceRecorder.stopRecording()
                                            isVoiceRecording = false
                                            onAnalyze(SourceType.VOICE, audioPath ?: "Voice memo note: Remind me to call Marcus tomorrow at 5pm")
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
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.input_text_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Understand Action
            if (selectedTab != SourceType.VOICE) {
                DayflowPrimaryButton(
                    text = if (isAnalyzing) stringResource(R.string.input_action_analyzing) else stringResource(R.string.input_action_understand),
                    icon = Icons.Outlined.AutoAwesome,
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
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
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
