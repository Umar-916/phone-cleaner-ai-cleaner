package com.phonecleaner.aicleaner.core.common

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

/** Parses `#RRGGBB` / `#AARRGGBB` strings coming from the backend; null when blank or malformed. */
fun String?.toComposeColorOrNull(): Color? {
    if (isNullOrBlank()) return null
    return runCatching { Color(this.toColorInt()) }.getOrNull()
}
