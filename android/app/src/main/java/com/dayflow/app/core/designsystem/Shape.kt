package com.dayflow.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Dayflow Corner Radii Tokens
 * Editorial softened corners: cards (16-24dp), buttons (12-16dp), badges (full/pill).
 */
val DayflowShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val CardShape = RoundedCornerShape(18.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(50)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
