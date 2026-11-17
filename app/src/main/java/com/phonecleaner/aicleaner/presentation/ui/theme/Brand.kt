package com.phonecleaner.aicleaner.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color

object Brand {
    val BlueLight = Color(0xFFA66BF0)
    val Blue = Color(0xFF7A2ACB)
    val BlueDark = Color(0xFF5A1AA6)
    val Cyan = Color(0xFFB98CFF)
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

    val HeroTop = Color(0xFF9B54E8)
    val HeroBottom = Color(0xFF6A1FC0)

    val Primary: Brush get() = Brush.horizontalGradient(listOf(BlueLight, Blue))
    val Hero: Brush get() = Brush.verticalGradient(listOf(HeroTop, HeroBottom))
    val ProBadge: Brush get() = Brush.linearGradient(listOf(Gold, GoldDeep))
}

object SplashPurple {
    val top = Color(0xFFE8D6FC)
    val bottom = Color(0xFFEEDFFD)
}

val SplashInk = Color(0xFF250B3B)

@Composable
fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

object OnboardingPalette {
    val top: Color @Composable get() = if (isDarkSurface()) Color(0xFF1F1233) else Color(0xFFF1E4FE)
    val bottom: Color @Composable get() = if (isDarkSurface()) Color(0xFF150B24) else Color(0xFFE8D6FC)
    val purple: Color @Composable get() = if (isDarkSurface()) Color(0xFF8B3FE0) else Color(0xFF7A2ACB)
    val ink: Color @Composable get() = if (isDarkSurface()) Color.White else Color(0xFF250B3B)
    val muted: Color @Composable get() = if (isDarkSurface()) Color(0xFFB9A6D6) else Color(0xFF6D5A85)
    val dot: Color @Composable get() = if (isDarkSurface()) Color(0xFF4A2A7A) else Color(0xFFD4B6F6)
}

@Composable
fun softCardColor(): Color =
    androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary, if (isDarkSurface()) 0.12f else 0.07f)
