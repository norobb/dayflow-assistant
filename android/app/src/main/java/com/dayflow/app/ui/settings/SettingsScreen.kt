package com.dayflow.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.BuildConfig
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.ButtonShape
import com.dayflow.app.core.designsystem.DayflowBg
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowPrimarySoft
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.core.update.UpdateStatus
import com.dayflow.app.ui.components.DayflowPrimaryButton
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowSecondaryButton

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DayflowBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = DayflowText
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DayflowText
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Section: Language
            item {
                SectionHeader(stringResource(R.string.settings_language))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageRow(
                            name = stringResource(R.string.settings_language_en),
                            code = "en",
                            selected = uiState.language == "en",
                            onSelect = { viewModel.setLanguage("en") }
                        )
                        LanguageRow(
                            name = stringResource(R.string.settings_language_de),
                            code = "de",
                            selected = uiState.language == "de",
                            onSelect = { viewModel.setLanguage("de") }
                        )
                        LanguageRow(
                            name = stringResource(R.string.settings_language_es),
                            code = "es",
                            selected = uiState.language == "es",
                            onSelect = { viewModel.setLanguage("es") }
                        )
                    }
                }
            }

            // Section: Sound & Feedback
            item {
                SectionHeader(stringResource(R.string.settings_section_haptics))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_sound_effects),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = DayflowText
                            )
                            Switch(
                                checked = uiState.isSoundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DayflowPrimary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_haptics),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = DayflowText
                            )
                            Switch(
                                checked = uiState.isHapticEnabled,
                                onCheckedChange = { viewModel.setHapticEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DayflowPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Section: AI Provider
            item {
                SectionHeader(stringResource(R.string.settings_section_ai))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AiProviderRow(
                            title = stringResource(R.string.settings_ai_provider_local),
                            selected = uiState.aiProvider == "local",
                            onSelect = { viewModel.setAiProvider("local") }
                        )
                        AiProviderRow(
                            title = stringResource(R.string.settings_ai_provider_gemini),
                            selected = uiState.aiProvider == "gemini",
                            onSelect = { viewModel.setAiProvider("gemini") }
                        )
                    }
                }
            }

            // Section: Auto Updates
            item {
                SectionHeader(stringResource(R.string.settings_section_updates))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.settings_update_channel),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DayflowTextMuted
                        )

                        UpdateChannelRow(
                            title = stringResource(R.string.settings_channel_releases),
                            selected = uiState.updateChannel == "releases",
                            onSelect = { viewModel.setUpdateChannel("releases") }
                        )

                        UpdateChannelRow(
                            title = stringResource(R.string.settings_channel_ci),
                            selected = uiState.updateChannel == "ci",
                            onSelect = { viewModel.setUpdateChannel("ci") }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status display
                        when (val status = uiState.updateStatus) {
                            is UpdateStatus.Checking -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = DayflowPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_status_checking),
                                        fontSize = 12.sp,
                                        color = DayflowTextMuted
                                    )
                                }
                            }
                            is UpdateStatus.NoUpdateAvailable -> {
                                Text(
                                    text = stringResource(R.string.settings_status_up_to_date, BuildConfig.VERSION_NAME),
                                    fontSize = 12.sp,
                                    color = DayflowTextMuted
                                )
                            }
                            is UpdateStatus.UpdateAvailable -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = stringResource(R.string.settings_status_update_available, status.versionTag),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DayflowPrimary
                                    )
                                    if (status.releaseNotes.isNotEmpty()) {
                                        Text(
                                            text = status.releaseNotes,
                                            fontSize = 11.sp,
                                            color = DayflowTextMuted,
                                            maxLines = 2
                                        )
                                    }
                                    DayflowPrimaryButton(
                                        text = stringResource(R.string.settings_btn_install_update),
                                        icon = Icons.Outlined.Download,
                                        onClick = { viewModel.downloadAndInstallUpdate(status) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            is UpdateStatus.Downloading -> {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.settings_status_downloading, status.progress),
                                        fontSize = 12.sp,
                                        color = DayflowPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            is UpdateStatus.ReadyToInstall -> {
                                DayflowPrimaryButton(
                                    text = stringResource(R.string.settings_btn_install_update),
                                    icon = Icons.Outlined.SystemUpdate,
                                    onClick = { viewModel.downloadAndInstallUpdate(
                                        UpdateStatus.UpdateAvailable(
                                            versionTag = "Downloaded",
                                            releaseNotes = "",
                                            downloadUrl = status.apkFile.absolutePath,
                                            channel = uiState.updateChannel
                                        )
                                    ) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            is UpdateStatus.Error -> {
                                Text(
                                    text = status.message,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB00020)
                                )
                            }
                            UpdateStatus.Idle -> {
                                // Default state
                            }
                        }

                        DayflowSecondaryButton(
                            text = stringResource(R.string.settings_btn_check_updates),
                            icon = Icons.Outlined.Refresh,
                            onClick = { viewModel.checkForUpdates() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section: Privacy & Reset
            item {
                SectionHeader(stringResource(R.string.settings_section_privacy))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = DayflowPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_privacy_info),
                                fontSize = 12.sp,
                                color = DayflowTextMuted
                            )
                        }

                        DayflowSecondaryButton(
                            text = stringResource(R.string.settings_reset_data),
                            icon = Icons.Outlined.Refresh,
                            onClick = { viewModel.resetData() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // About Brand
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dayflow_symbol_official),
                            contentDescription = "Dayflow Symbol",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_version),
                            fontSize = 11.sp,
                            color = DayflowTextMuted
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = DayflowTextMuted,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun LanguageRow(
    name: String,
    code: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .background(if (selected) DayflowPrimarySoft else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) DayflowPrimary else DayflowText
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = DayflowPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AiProviderRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .background(if (selected) DayflowPrimarySoft else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) DayflowPrimary else DayflowText,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = DayflowPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun UpdateChannelRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ButtonShape)
            .background(if (selected) DayflowPrimarySoft else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) DayflowPrimary else DayflowText,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = DayflowPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
