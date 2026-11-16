package com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R

/** One row of a settings card: tinted icon, title, optional value, then an arrow or a switch. */
@Composable
fun SettingItem(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    switchChecked: Boolean? = null,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    switchChecked != null -> Modifier.clickable { onSwitchChange?.invoke(!switchChecked) }
                    onClick != null -> Modifier.clickable(onClick = onClick)
                    else -> Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
                )
            }
        }
        if (value != null) {
            Text(
                value,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 13.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
            Spacer(Modifier.width(6.dp))
        }
        if (switchChecked != null) {
            Switch(checked = switchChecked, onCheckedChange = onSwitchChange)
        } else {
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
    )
}
