package com.dayflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.R
import com.dayflow.app.core.designsystem.DayflowBorder
import com.dayflow.app.core.designsystem.DayflowPrimary
import com.dayflow.app.core.designsystem.DayflowText
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.ui.navigation.Screen

@Composable
fun DayflowBottomBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    onOpenInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bar surface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White.copy(alpha = 0.95f))
                .border(width = 0.5.dp, color = DayflowBorder.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
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

            // Space in middle for floating FAB
            Box(modifier = Modifier.size(52.dp))

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

        // Central floating "Understand / Input" FAB
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(52.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .background(color = DayflowPrimary, shape = CircleShape)
                .clip(CircleShape)
                .clickable { onOpenInput() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.nav_understand),
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
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
    val color = if (selected) DayflowPrimary else DayflowTextMuted

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
