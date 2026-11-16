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

private val DarkColorScheme = darkColorScheme(
    primary = colorPrimaryBoth,
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
    secondaryContainer = Color(0xFF1D3358),
    onSecondaryContainer = Color(0xFFBFD7FF),
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
    secondaryContainer = Color(0xFFD6E5FF),
    onSecondaryContainer = Color(0xFF123A8C),

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
