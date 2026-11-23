package com.dayflow.app.ui.tutorial

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.AppearanceMode
import com.dayflow.app.core.designsystem.DayflowAccentChoices
import com.dayflow.app.core.designsystem.accentFromHex
import com.dayflow.app.core.designsystem.rememberReducedMotion
import com.dayflow.app.core.util.PermissionUtils
import com.dayflow.app.ui.components.DayflowLogoHeader
import com.dayflow.app.ui.components.DayflowSymbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * First-run intro: what Dayflow does, pick an accent color, the two notification modes
 * (accessibility vs. root/LSPosed) and every permission in one place.
 */
@Composable
fun TutorialScreen(
    accentHex: String,
    onAccentChange: (String) -> Unit,
    appearanceMode: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit,
    onFinishTutorial: () -> Unit
) {
    val pageCount = 5
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val primary = MaterialTheme.colorScheme.primary
    val isLast = pagerState.currentPage == pageCount - 1

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(Brush.radialGradient(listOf(primary.copy(alpha = 0.16f), Color.Transparent), radius = 1100f, center = androidx.compose.ui.geometry.Offset(900f, 0f)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayflowLogoHeader(symbolSize = 26.dp)
                if (!isLast) {
                    TextButton(onClick = onFinishTutorial) {
                        Text(stringResource(R.string.tutorial_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        0 -> WelcomePage()
                        1 -> HowPage()
                        2 -> ColorPage(accentHex, onAccentChange, appearanceMode, onAppearanceChange)
                        3 -> ModesPage()
                        else -> PermissionsPage()
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { i ->
                    val selected = pagerState.currentPage == i
                    val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dot")
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (selected) primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) onFinishTutorial()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).padding(bottom = 0.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(
                    stringResource(if (isLast) R.string.tutorial_get_started else R.string.tutorial_next),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ---------------------------------------------------------------- pages

@Composable
private fun ColumnScope.WelcomePage() {
    val reduced = rememberReducedMotion()
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.92f, targetValue = if (reduced) 0.92f else 1.06f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale"
    )
    Spacer(Modifier.height(24.dp))
    Box(Modifier.align(Alignment.CenterHorizontally).size(190.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.matchParentSize().scale(pulse).clip(CircleShape)
                .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), Color.Transparent)))
        )
        Box(
            Modifier.size(124.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) { DayflowSymbol(Modifier.size(68.dp)) }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.onb_welcome_title),
        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold, lineHeight = 42.sp),
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(stringResource(R.string.onb_welcome_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(R.string.onb_src_message, R.string.onb_src_screenshot, R.string.onb_src_pdf, R.string.onb_src_voice).forEach {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.HowPage() {
    PageTitle(R.string.onb_how_title, R.string.onb_how_body)
    Step(1, Icons.Outlined.IosShare, R.string.onb_step1_title, R.string.onb_step1_body)
    Step(2, Icons.Outlined.AutoAwesome, R.string.onb_step2_title, R.string.onb_step2_body)
    Step(3, Icons.Outlined.TaskAlt, R.string.onb_step3_title, R.string.onb_step3_body)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(stringResource(R.string.onb_privacy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun ColumnScope.ColorPage(
    accentHex: String,
    onAccentChange: (String) -> Unit,
    appearanceMode: AppearanceMode,
    onAppearanceChange: (AppearanceMode) -> Unit
) {
    PageTitle(R.string.onb_color_title, R.string.onb_color_body)
    PreviewCard()
    // Burgundy is stored as "" so the hand-tuned default scheme stays exact.
    val selected = accentHex.ifBlank { DayflowAccentChoices.first() }
    listOf(DayflowAccentChoices.take(4), DayflowAccentChoices.drop(4)).forEach { rowColors ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            rowColors.forEach { hex ->
                val color = accentFromHex(hex) ?: return@forEach
                val isSel = hex.equals(selected, true)
                val ring by animateColorAsState(if (isSel) MaterialTheme.colorScheme.onBackground else Color.Transparent, label = "ring")
                val label = stringResource(R.string.onb_color_swatch, hex)
                Box(
                    Modifier.size(60.dp).border(2.dp, ring, CircleShape).padding(5.dp).clip(CircleShape).background(color)
                        .clickable(role = Role.RadioButton) { onAccentChange(if (hex == DayflowAccentChoices.first()) "" else hex) }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center
                ) { if (isSel) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            AppearanceMode.SYSTEM to R.string.settings_mode_system,
            AppearanceMode.LIGHT to R.string.settings_mode_light,
            AppearanceMode.DARK to R.string.settings_mode_dark
        ).forEach { (mode, res) ->
            val sel = mode == appearanceMode
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(role = Role.RadioButton) { onAppearanceChange(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(res), fontSize = 13.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    color = if (sel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** A miniature "Today" card so the color choice is seen in context, not as a bare dot. */
@Composable
private fun PreviewCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.onb_preview_today), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(4.dp).height(36.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.onb_preview_event), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text("14:30", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                stringResource(R.string.onb_preview_add),
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.ModesPage() {
    val context = LocalContext.current
    PageTitle(R.string.onb_modes_title, R.string.onb_modes_body)
    ModeCard(
        Icons.Outlined.Accessibility, R.string.onb_mode_a11y_title, R.string.onb_mode_a11y_badge, R.string.onb_mode_a11y_body,
        action = R.string.onb_mode_a11y_action
    ) { open(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    ModeCard(Icons.Outlined.Terminal, R.string.onb_mode_root_title, R.string.onb_mode_root_badge, R.string.onb_mode_root_body)
}

@Composable
private fun ColumnScope.PermissionsPage() {
    val context = LocalContext.current
    // Several grants happen in system settings; re-check once a second while this page is shown.
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { tick++ }
    val runtime = buildList {
        if (Build.VERSION.SDK_INT >= 33) { add(Manifest.permission.POST_NOTIFICATIONS); add(Manifest.permission.READ_MEDIA_IMAGES) }
        add(Manifest.permission.READ_CALENDAR); add(Manifest.permission.WRITE_CALENDAR); add(Manifest.permission.RECORD_AUDIO)
    }.toTypedArray()

    PageTitle(R.string.onb_perm_title, R.string.onb_perm_body)
    androidx.compose.runtime.key(tick) {
        OutlinedButton(onClick = { launcher.launch(runtime) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
            Text(stringResource(R.string.onb_perm_all), fontWeight = FontWeight.Bold)
        }
        PermRow(Icons.Outlined.Notifications, R.string.settings_perm_notifications, R.string.onb_perm_notif_why, PermissionUtils.hasNotificationPermission(context)) {
            launcher.launch(runtime)
        }
        PermRow(Icons.Outlined.CalendarToday, R.string.settings_perm_calendar, R.string.onb_perm_cal_why, PermissionUtils.hasCalendarPermission(context)) { launcher.launch(runtime) }
        PermRow(Icons.Outlined.Mic, R.string.settings_perm_microphone, R.string.onb_perm_mic_why, PermissionUtils.hasMicrophonePermission(context)) { launcher.launch(runtime) }
        PermRow(Icons.Outlined.Image, R.string.settings_perm_photos, R.string.onb_perm_photos_why, PermissionUtils.hasPhotosPermission(context)) { launcher.launch(runtime) }
        PermRow(
            Icons.Outlined.NotificationsActive, R.string.settings_perm_notif_listener, R.string.onb_perm_listener_why,
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        ) { open(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        PermRow(Icons.Outlined.Accessibility, R.string.settings_perm_accessibility, R.string.onb_perm_a11y_why, accessibilityOn(context)) {
            open(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        PermRow(Icons.Outlined.Layers, R.string.onb_perm_overlay, R.string.onb_perm_overlay_why, Settings.canDrawOverlays(context)) {
            open(context, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarms = context.getSystemService(AlarmManager::class.java)
            PermRow(Icons.Outlined.Alarm, R.string.onb_perm_alarm, R.string.onb_perm_alarm_why, alarms?.canScheduleExactAlarms() == true) {
                open(context, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
            }
        }
    }
}

// ---------------------------------------------------------------- pieces

@Composable
private fun PageTitle(title: Int, body: Int) {
    Text(stringResource(title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
    Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Step(n: Int, icon: ImageVector, title: Int, body: Int) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("$n · " + stringResource(title), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModeCard(icon: ImageVector, title: Int, badge: Int, body: Int, action: Int? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(
                stringResource(badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) TextButton(onClick = onAction, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(stringResource(action), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PermRow(icon: ImageVector, title: Int, why: Int, granted: Boolean, onRequest: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !granted, onClick = onRequest).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(why), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (granted) {
            Icon(Icons.Outlined.Check, stringResource(R.string.permission_granted), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        } else {
            Text(
                stringResource(R.string.permission_grant), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private fun accessibilityOn(context: Context): Boolean = runCatching {
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().contains(context.packageName)
}.getOrDefault(false)

private fun open(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
