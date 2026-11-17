package com.phonecleaner.aicleaner.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.phonecleaner.aicleaner.core.common.ApplySystemBarColors
import com.phonecleaner.aicleaner.core.common.SetStatusBarIconsDarkWithReset
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeKeys

private const val TEXT_SCALE = 0.88f

private val DarkColorScheme = darkColorScheme(
    primary = colorPrimaryDark,
    onPrimary = Color.White,
    background = backGroundColorDark,
    secondary = secondaryColorDark,
    tertiary = tertiaryColorDark,
    surface = surfaceColorDark,
    onTertiary = onTertiaryColorDark,
    onSecondary = onSecondaryColorDark,
    onSurface = onSurfaceColorDark,
    onSurfaceVariant = onSurfaceVariantDark,
    onSecondaryFixed = onSecondaryFixedDark,
    onSecondaryFixedVariant = onSecondaryFixedVariantDark,
    onTertiaryFixed = onTertiaryFixedDark,
    secondaryContainer = Color(0xFF4A2A7A),
    onSecondaryContainer = Color(0xFFE3D0FF),
)

private val LightColorScheme = lightColorScheme(
    primary = colorPrimaryBoth,
    onPrimary = Color.White,
    background = backGroundColorLight,
    secondary = secondaryColorLight,
    tertiary = tertiaryColorLight,
    surface = surfaceColorLight,
    onTertiary = onTertiaryColorLight,
    onSecondary = onSecondaryColorLight,
    onSurface = onSurfaceColorLight,
    onSurfaceVariant = onSurfaceVariantLight,
    onSecondaryFixed = onSecondaryFixedLight,
    onSecondaryFixedVariant = onSecondaryFixedVariantLight,
    onTertiaryFixed = onTertiaryFixedLight,
    secondaryContainer = Color(0xFFE8D8FB),
    onSecondaryContainer = Color(0xFF4A1A8C),

)

@Composable
fun PhoneCleanerProTheme(
    themeKey: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeKey) {
        ThemeKeys.DARK -> true
        ThemeKeys.LIGHT -> false
        else -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    ApplySystemBarColors(
        statusBarColor = Color.Transparent,
        navigationBarColor = if (darkTheme) backGroundColorDark else backGroundColorLight,
        darkIcons = !darkTheme
    )
    SetStatusBarIconsDarkWithReset(darkIcons = !darkTheme)

    // The whole app reads a little smaller than the system font scale: every sp size is scaled by this factor.
    val density = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, density.fontScale * TEXT_SCALE)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
