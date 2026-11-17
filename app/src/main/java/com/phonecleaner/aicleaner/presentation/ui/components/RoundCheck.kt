package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundCheck(
    checked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val fill by animateColorAsState(if (checked) color else Color.Transparent, tween(180), label = "roundCheck")
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .border(if (checked) 0.dp else 1.6.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(size * 0.66f))
    }
}
