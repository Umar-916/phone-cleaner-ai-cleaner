package com.phonecleaner.aicleaner.presentation.ui.components

import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val textColor by animateColorAsState(
                if (selected) Color.White else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                tween(200), label = "tabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (selected) Modifier.background(Brand.Primary) else Modifier)
                    .tapPlainClickable("app_tabs_tap_1") { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp),
                    color = textColor,
                    style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun PillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        tween(200), label = "chipContainer"
    )
    val content by animateColorAsState(
        if (selected) Color.White else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
        tween(200), label = "chipContent"
    )
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .tapPlainClickable("app_tabs_tap_2", onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            maxLines = 1,
            softWrap = false,
            color = content,
            style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
        )
    }
}
