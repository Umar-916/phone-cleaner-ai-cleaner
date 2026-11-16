package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

/** Pill switch with a gradient track and a white thumb that glides between the two states. */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val offset by animateDpAsState(if (checked) 20.dp else 0.dp, tween(200), label = "switchThumb")
    val off = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
    val offBorder by animateColorAsState(if (checked) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.45f), tween(200), label = "switchBorder")
    Box(
        modifier = modifier
            .width(46.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(if (checked) Brand.Primary else androidx.compose.ui.graphics.SolidColor(off))
            .border(1.dp, offBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = onCheckedChange != null
            ) { onCheckedChange?.invoke(!checked) }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(20.dp)
                .shadow(3.dp, CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

/** Rounded square checkbox with a filled brand state and a visible outline in both themes. */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(7.dp)
    val outline = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(shape)
                .background(if (checked) Brand.Primary else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
                .border(if (checked) 0.dp else 2.dp, outline, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = onCheckedChange != null
                ) { onCheckedChange?.invoke(!checked) },
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/** Thin gradient progress bar with rounded ends, used for storage and other determinate values. */
@Composable
fun AppProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
    brush: androidx.compose.ui.graphics.Brush = Brand.Primary
) {
    Box(modifier = modifier.fillMaxWidth().height(height).clip(CircleShape).background(trackColor)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(brush)
        )
    }
}

/** Slider with a slim gradient track and a white thumb ringed in the brand colour. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val inactive = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .shadow(3.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(5.dp, Brand.Blue, CircleShape)
            )
        },
        track = { state: SliderState ->
            val range = state.valueRange.endInclusive - state.valueRange.start
            val fraction = if (range <= 0f) 0f else ((state.value - state.valueRange.start) / range).coerceIn(0f, 1f)
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(inactive)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(CircleShape)
                        .background(Brand.Primary)
                )
            }
        }
    )
}
