package com.phonecleaner.aicleaner.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Brand palette shared by every screen, so feature colors stay linked to the app identity. */
object Brand {
    val BlueLight = Color(0xFF43B0FF)
    val Blue = Color(0xFF1F6FEB)
    val BlueDark = Color(0xFF1245B8)
    val Cyan = Color(0xFF22D3EE)
    val Gold = Color(0xFFFFC53D)
    val GoldDeep = Color(0xFFFF9F1C)
    val Good = Color(0xFF22B573)
    val Danger = Color(0xFFE5484D)

    val Junk = Color(0xFFFF8A1F)
    val Process = Color(0xFFE5484D)
    val Duplicates = Color(0xFF2F8BFF)
    val Apps = Color(0xFF22B573)
    val WhatsApp = Color(0xFF25D366)
    val Photo = Color(0xFF7C5CFC)
    val Ai = Color(0xFF9B5CFF)
    val Battery = Color(0xFF16A34A)

    val HeroTop = Color(0xFF2F8BFF)
    val HeroBottom = Color(0xFF1245B8)

    val Primary: Brush get() = Brush.horizontalGradient(listOf(BlueLight, Blue))
    val Hero: Brush get() = Brush.verticalGradient(listOf(HeroTop, HeroBottom))
    val ProBadge: Brush get() = Brush.linearGradient(listOf(Gold, GoldDeep))
}
