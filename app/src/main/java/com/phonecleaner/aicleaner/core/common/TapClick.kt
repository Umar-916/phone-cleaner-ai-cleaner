package com.phonecleaner.aicleaner.core.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger

fun tapEvent(event: String?) {
    AnalyticsLogger.tap(event ?: "${Constants.currentScreenName}_tap")
}

fun Modifier.tapClickable(event: String? = null, enabled: Boolean = true, onClick: () -> Unit): Modifier =
    clickable(enabled = enabled) {
        tapEvent(event)
        onClick()
    }

fun (() -> Unit).tapped(event: String? = null): () -> Unit = {
    tapEvent(event)
    this()
}

fun Modifier.tapPlainClickable(event: String? = null, enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled
    ) {
        tapEvent(event)
        onClick()
    }
}
