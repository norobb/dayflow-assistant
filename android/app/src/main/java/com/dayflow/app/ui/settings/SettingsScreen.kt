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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.dayflow.app.core.util.PermissionUtils
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.AppearanceMode
import com.dayflow.app.core.designsystem.DesignSystemMode
import com.dayflow.app.core.update.UpdateStatus
import com.dayflow.app.ui.components.DayflowCard
import com.dayflow.app.ui.components.DayflowPrimaryButton
import com.dayflow.app.ui.components.DayflowSecondaryButton

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Section: Appearance
            item {
                SectionHeader(stringResource(R.string.settings_section_appearance))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Appearance Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppearanceOptionChip(
                                label = "Light",
                                selected = uiState.appearanceMode == AppearanceMode.LIGHT,
                                onClick = { viewModel.setAppearanceMode(AppearanceMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                            AppearanceOptionChip(
                                label = "Dark",
                                selected = uiState.appearanceMode == AppearanceMode.DARK,
                                onClick = { viewModel.setAppearanceMode(AppearanceMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                            AppearanceOptionChip(
                                label = "System",
                                selected = uiState.appearanceMode == AppearanceMode.SYSTEM,
                                onClick = { viewModel.setAppearanceMode(AppearanceMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section: Design System
            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Design Language",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppearanceOptionChip(
                                label = "Dayflow Signature",
                                selected = uiState.designSystemMode == DesignSystemMode.DAYFLOW_SIGNATURE,
                                onClick = { viewModel.setDesignSystemMode(DesignSystemMode.DAYFLOW_SIGNATURE) },
                                modifier = Modifier.weight(1f)
                            )
                            AppearanceOptionChip(
                                label = "Material 3",
                                selected = uiState.designSystemMode == DesignSystemMode.MATERIAL3,
                                onClick = { viewModel.setDesignSystemMode(DesignSystemMode.MATERIAL3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section: Language
            item {
                SectionHeader(stringResource(R.string.settings_section_language))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_select_language),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        LanguageRow(
                            name = "English",
                            code = "en",
                            selected = uiState.language == "en",
                            onSelect = { viewModel.setLanguage("en") }
                        )
                        LanguageRow(
                            name = "Deutsch",
                            code = "de",
                            selected = uiState.language == "de",
                            onSelect = { viewModel.setLanguage("de") }
                        )
                        LanguageRow(
                            name = "Español",
                            code = "es",
                            selected = uiState.language == "es",
                            onSelect = { viewModel.setLanguage("es") }
                        )
                    }
                }
            }

            // Section: AI Configuration
            item {
                SectionHeader(stringResource(R.string.settings_section_ai))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "AI Provider & Model",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        AiProviderRow(
                            title = "Gemini AI (Real Multimodal)",
                            selected = uiState.aiProvider == "gemini",
                            onSelect = { viewModel.setAiProvider("gemini") }
                        )
                        AiProviderRow(
                            title = "Local Offline Engine",
                            selected = uiState.aiProvider == "local",
                            onSelect = { viewModel.setAiProvider("local") }
                        )

                        if (uiState.aiProvider == "gemini") {
                            OutlinedTextField(
                                value = uiState.geminiApiKey,
                                onValueChange = { viewModel.setGeminiApiKey(it) },
                                label = { Text("Gemini API Key") },
                                placeholder = { Text("AIzaSy...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = uiState.geminiModel,
                                onValueChange = { viewModel.setGeminiModel(it) },
                                label = { Text("Model Identifier") },
                                placeholder = { Text("gemini-3.6-flash") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }

            // Section: Haptics & Feedback
            item {
                SectionHeader("INTERACTION & HAPTICS")
            }

            item {
                DayflowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Vibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_haptics),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = uiState.hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section: Permissions & System Integrations
            item {
                SectionHeader("PERMISSIONS & OVERLAY INTEGRATIONS")
            }

            item {
                val context = LocalContext.current
                var notifGranted by remember { mutableStateOf(PermissionUtils.hasNotificationPermission(context)) }
                var calGranted by remember { mutableStateOf(PermissionUtils.hasCalendarPermission(context)) }
                var micGranted by remember { mutableStateOf(PermissionUtils.hasMicrophonePermission(context)) }
                var photosGranted by remember { mutableStateOf(PermissionUtils.hasPhotosPermission(context)) }

                val notifLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted -> notifGranted = granted }

                val micLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted -> micGranted = granted }

                val photosLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted -> photosGranted = granted }

                val calLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { map -> calGranted = map.values.all { it } }

                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "System Permissions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        PermissionRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications Permission",
                            granted = notifGranted,
                            onRequest = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )

                        PermissionRow(
                            icon = Icons.Outlined.CalendarToday,
                            title = "Calendar Sync Permission",
                            granted = calGranted,
                            onRequest = {
                                calLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                )
                            }
                        )

                        PermissionRow(
                            icon = Icons.Outlined.Mic,
                            title = "Microphone Permission",
                            granted = micGranted,
                            onRequest = {
                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )

                        PermissionRow(
                            icon = Icons.Outlined.Image,
                            title = "Photos & Media Access",
                            granted = photosGranted,
                            onRequest = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    photosLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    photosLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        )

                        PermissionRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notification Listener (Message Overlay)",
                            granted = false,
                            onRequest = {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Section: Auto-Update
            item {
                SectionHeader(stringResource(R.string.settings_section_updates))
            }

            item {
                DayflowCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_section_updates),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Status Info
                        val statusText = when (val status = updateStatus) {
                            is UpdateStatus.Idle -> stringResource(R.string.settings_version)
                            is UpdateStatus.Checking -> stringResource(R.string.settings_status_checking)
                            is UpdateStatus.NoUpdateAvailable -> stringResource(R.string.settings_status_up_to_date, status.currentVersion)
                            is UpdateStatus.UpdateAvailable -> stringResource(R.string.settings_status_update_available, status.versionTag)
                            is UpdateStatus.Downloading -> stringResource(R.string.settings_status_downloading, status.progress)
                            is UpdateStatus.ReadyToInstall -> "Ready to install update"
                            is UpdateStatus.Error -> "Update error: ${status.message}"
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (updateStatus is UpdateStatus.UpdateAvailable) {
                            val available = updateStatus as UpdateStatus.UpdateAvailable
                            DayflowPrimaryButton(
                                text = stringResource(R.string.settings_btn_install_update),
                                icon = Icons.Outlined.Download,
                                onClick = { viewModel.downloadAndInstallUpdate(available) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            DayflowSecondaryButton(
                                text = stringResource(R.string.settings_btn_check_updates),
                                icon = Icons.Outlined.Refresh,
                                onClick = { viewModel.checkForUpdates() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section: Reset & Data
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_privacy_info),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // About Brand Footer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dayflow_symbol_official),
                            contentDescription = "Dayflow Symbol",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_version),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onRequest() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (granted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (granted) "Granted" else "Grant",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun AppearanceOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
