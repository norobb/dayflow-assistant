package com.dayflow.app.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext

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

/** Accent swatches offered in the intro and settings (all readable with white text). */
val DayflowAccentChoices = listOf(
    "641C24", // Burgundy (Dayflow)
    "9A4A2B", // Terracotta
    "8C5E28", // Ochre
    "2E5C38", // Forest
    "1D5C63", // Teal
    "1F3A5F", // Ink blue
    "4A2C5E", // Plum
    "2F2F33"  // Graphite
)

fun accentFromHex(hex: String): Color? = hex.takeIf { it.length == 6 }?.toLongOrNull(16)?.let { Color(0xFF000000 or it) }

/** Re-tints the signature scheme around [accent]; null keeps the burgundy defaults. */
private fun ColorScheme.withAccent(accent: Color?, dark: Boolean): ColorScheme {
    if (accent == null) return this
    return copy(
        primary = if (dark) lerp(accent, Color.White, 0.3f) else accent,
        primaryContainer = lerp(accent, background, if (dark) 0.7f else 0.88f),
        onPrimaryContainer = if (dark) Color.White else accent
    )
}

@Composable
fun DayflowTheme(
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    designSystemMode: DesignSystemMode = DesignSystemMode.DAYFLOW_SIGNATURE,
    accentHex: String = "",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = when (appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (designSystemMode) {
        DesignSystemMode.DAYFLOW_SIGNATURE -> (if (isDark) DayflowSignatureDarkColorScheme else DayflowSignatureLightColorScheme)
            .withAccent(accentFromHex(accentHex), isDark)
        DesignSystemMode.MATERIAL3 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) M3DarkColorScheme else M3LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DayflowTypography,
        shapes = DayflowShapes,
        content = content
    )
}
