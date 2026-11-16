package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

/** Gradient call-to-action used on sheets, dialogs and full-width screen buttons. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                if (enabled) Brand.Primary
                else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            style = TextStyle(fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * Permission request presented as a bottom sheet instead of a system-style dialog: explains what the
 * access is used for, then hands over to the real permission flow through [onAllow].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionPrompt(
    icon: ImageVector,
    title: String,
    body: String,
    bullets: List<String>,
    allowLabel: String,
    notNowLabel: String,
    onAllow: () -> Unit,
    onDismiss: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(
                title,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 21.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                body,
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                bullets.forEach { bullet ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Brand.Good, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(10.dp))
                        Text(
                            bullet,
                            color = MaterialTheme.colorScheme.tertiary,
                            style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
                        )
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            GradientButton(allowLabel, onAllow)
            Spacer(Modifier.height(6.dp))
            Text(
                notNowLabel,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onDismiss).padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
