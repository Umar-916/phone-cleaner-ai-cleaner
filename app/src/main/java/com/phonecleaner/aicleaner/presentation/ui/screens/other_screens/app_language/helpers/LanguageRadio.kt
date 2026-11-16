package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import network.chaintech.sdpcomposemultiplatform.sdp

@Composable
fun LanguageRadio(
    selected: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val ringColor = if (highlighted) Color.White else MaterialTheme.colorScheme.surface
    val fillColor = if (highlighted) Color.White else MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.size(18.sdp)) {
        val strokeWidth = 1.6.dp.toPx()
        drawCircle(
            color = ringColor,
            radius = (size.minDimension - strokeWidth) / 2f,
            style = Stroke(width = strokeWidth)
        )
        if (selected) {
            drawCircle(
                color = fillColor,
                radius = size.minDimension * 0.22f
            )
        }
    }
}
