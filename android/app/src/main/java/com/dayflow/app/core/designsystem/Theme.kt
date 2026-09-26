package com.dayflow.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DayflowLightColorScheme = lightColorScheme(
    primary = DayflowPrimary,
    onPrimary = Color.White,
    primaryContainer = DayflowPrimarySoft,
    onPrimaryContainer = DayflowPrimary,
    secondary = DayflowTextMuted,
    onSecondary = Color.White,
    background = DayflowBg,
    onBackground = DayflowText,
    surface = DayflowCard,
    onSurface = DayflowText,
    surfaceVariant = DayflowCardWhite,
    onSurfaceVariant = DayflowTextMuted,
    outline = DayflowBorder
)

@Composable
fun DayflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dayflow uses its signature warm linen and burgundy aesthetic
    MaterialTheme(
        colorScheme = DayflowLightColorScheme,
        typography = DayflowTypography,
        shapes = DayflowShapes,
        content = content
    )
}
