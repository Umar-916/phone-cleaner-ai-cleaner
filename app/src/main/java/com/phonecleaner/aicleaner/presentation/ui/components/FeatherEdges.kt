package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Fades the four borders of the content into whatever is behind it, so artwork that carries its own flat
 * backdrop (the mascot pictures) melts into the screen background instead of showing a rectangle.
 */
fun Modifier.featheredEdges(fraction: Float = 0.10f): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent, fraction to Color.Black, (1f - fraction) to Color.Black, 1f to Color.Transparent,
                startX = 0f, endX = size.width
            ),
            blendMode = BlendMode.DstIn
        )
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent, fraction to Color.Black, (1f - fraction) to Color.Black, 1f to Color.Transparent,
                startY = 0f, endY = size.height
            ),
            blendMode = BlendMode.DstIn
        )
    }
