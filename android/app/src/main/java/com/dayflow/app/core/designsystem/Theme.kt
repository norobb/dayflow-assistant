package com.dayflow.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DayflowSignatureLightColorScheme = lightColorScheme(
    primary = DayflowPrimaryLightMode,
    onPrimary = Color.White,
    primaryContainer = DayflowPrimarySoftLight,
    onPrimaryContainer = DayflowPrimaryLightMode,
    secondary = DayflowTextMutedLight,
    onSecondary = Color.White,
    background = DayflowBgLight,
    onBackground = DayflowTextLight,
    surface = DayflowCardLight,
    onSurface = DayflowTextLight,
    surfaceVariant = DayflowCardWhiteLight,
    onSurfaceVariant = DayflowTextMutedLight,
    outline = DayflowBorderLight
)

private val DayflowSignatureDarkColorScheme = darkColorScheme(
    primary = DayflowPrimaryDarkMode,
    onPrimary = Color.White,
    primaryContainer = DayflowPrimarySoftDark,
    onPrimaryContainer = Color.White,
    secondary = DayflowTextMutedDark,
    onSecondary = Color.Black,
    background = DayflowBgDark,
    onBackground = DayflowTextDark,
    surface = DayflowCardDark,
    onSurface = DayflowTextDark,
    surfaceVariant = DayflowCardElevatedDark,
    onSurfaceVariant = DayflowTextMutedDark,
    outline = DayflowBorderDark
)

private val M3LightColorScheme = lightColorScheme(
    primary = M3LightPrimary,
    onPrimary = M3LightOnPrimary,
    primaryContainer = M3LightPrimaryContainer,
    onPrimaryContainer = M3LightOnPrimaryContainer,
    background = M3LightBackground,
    onBackground = M3LightOnBackground,
    surface = M3LightSurface,
    onSurface = M3LightOnSurface
)

private val M3DarkColorScheme = darkColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = M3DarkPrimaryContainer,
    onPrimaryContainer = M3DarkOnPrimaryContainer,
    background = M3DarkBackground,
    onBackground = M3DarkOnBackground,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface
)

@Composable
fun DayflowTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    designSystemMode: DesignSystemMode = DesignSystemMode.DAYFLOW_SIGNATURE,
    content: @Composable () -> Unit
) {
    val isDark = when (appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (designSystemMode) {
        DesignSystemMode.DAYFLOW_SIGNATURE -> if (isDark) DayflowSignatureDarkColorScheme else DayflowSignatureLightColorScheme
        DesignSystemMode.MATERIAL3 -> if (isDark) M3DarkColorScheme else M3LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DayflowTypography,
        shapes = DayflowShapes,
        content = content
    )
}
