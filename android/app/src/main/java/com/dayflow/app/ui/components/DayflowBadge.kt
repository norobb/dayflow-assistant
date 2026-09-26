package com.dayflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayflow.app.core.designsystem.DayflowAccentAmber
import com.dayflow.app.core.designsystem.DayflowBorder
import com.dayflow.app.core.designsystem.DayflowCard
import com.dayflow.app.core.designsystem.DayflowShapes
import com.dayflow.app.core.designsystem.DayflowTextMuted
import com.dayflow.app.core.designsystem.PillShape

@Composable
fun ProductStateBadge(
    text: String = "COMING SOON",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = DayflowAccentAmber.copy(alpha = 0.12f),
                shape = DayflowShapes.small
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = DayflowAccentAmber,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun SourceBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = DayflowCard,
                shape = PillShape
            )
            .border(
                width = 1.dp,
                color = DayflowBorder,
                shape = PillShape
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = DayflowTextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
