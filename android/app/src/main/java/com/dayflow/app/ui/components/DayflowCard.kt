package com.dayflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dayflow.app.core.designsystem.CardShape
import com.dayflow.app.core.designsystem.DayflowBorder

@Composable
fun DayflowCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = DayflowBorder,
    padding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = CardShape)
            .padding(padding)
    ) {
        content()
    }
}
