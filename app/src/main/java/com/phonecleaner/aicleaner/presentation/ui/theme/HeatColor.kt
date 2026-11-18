package com.phonecleaner.aicleaner.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val HeatLavender = Color(0xFFEBDDFB)
val HeatPeach = Color(0xFFFFE0D2)
val HeatPink = Color(0xFFF08A9C)
val HeatRed = Color(0xFFE23B3B)

fun heatColor(t: Float): Color = when {
    t < 0.35f -> lerp(HeatLavender, HeatPeach, t / 0.35f)
    t < 0.7f -> lerp(HeatPeach, HeatPink, (t - 0.35f) / 0.35f)
    else -> lerp(HeatPink, HeatRed, ((t - 0.7f) / 0.3f).coerceIn(0f, 1f))
}
