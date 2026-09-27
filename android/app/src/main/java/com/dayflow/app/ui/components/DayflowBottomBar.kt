package com.dayflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.ui.navigation.Screen

@Composable
fun DayflowBottomBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = navInsets.calculateBottomPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Outlined.CalendarToday,
                    label = stringResource(R.string.nav_today),
                    selected = currentRoute == Screen.Today.route,
                    onClick = { onNavigate(Screen.Today) }
                )

                NavItem(
                    icon = Icons.Outlined.CheckCircleOutline,
                    label = stringResource(R.string.nav_tasks),
                    selected = currentRoute == Screen.Tasks.route,
                    onClick = { onNavigate(Screen.Tasks) }
                )

                // Central Integrated Action Item
                CentralOrganizeButton(onClick = onOpenInput)

                NavItem(
                    icon = Icons.Outlined.NotificationsNone,
                    label = stringResource(R.string.nav_reminders),
                    selected = currentRoute == Screen.Reminders.route,
                    onClick = { onNavigate(Screen.Reminders) }
                )

                NavItem(
                    icon = Icons.Outlined.Radar,
                    label = stringResource(R.string.nav_intelligence),
                    selected = currentRoute == Screen.Intelligence.route,
                    onClick = { onNavigate(Screen.Intelligence) }
                )
            }
        }
    }
}

@Composable
private fun CentralOrganizeButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.nav_understand),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.nav_understand),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val targetColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val iconColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(150), label = "nav_color")

    val pillColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            color = iconColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
