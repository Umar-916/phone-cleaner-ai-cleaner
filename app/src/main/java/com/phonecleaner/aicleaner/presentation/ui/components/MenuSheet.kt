package com.phonecleaner.aicleaner.presentation.ui.components

import com.phonecleaner.aicleaner.core.common.tapClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R

class MenuAction(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
fun MenuSheet(actions: List<MenuAction>, onDismiss: () -> Unit) {
    FloatingSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth()) {
            actions.forEach { action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .tapClickable("menu_sheet_click") { onDismiss(); action.onClick() }
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(action.icon, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.size(18.dp))
                    Text(
                        action.label,
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}
