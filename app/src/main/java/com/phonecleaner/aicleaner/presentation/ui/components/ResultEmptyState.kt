package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts

@Composable
fun ResultEmptyState(
    icon: ImageVector,
    accent: Color,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(104.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 22.sp, fontFamily = AppFonts.Bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            body,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontFamily = AppFonts.Medium)
        )
        Spacer(Modifier.height(32.dp))
        GradientButton(actionLabel, onAction)
    }
}
