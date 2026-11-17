package com.phonecleaner.aicleaner.presentation.ui.components

import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette

@Composable
fun StopProcessSheet(onCancel: () -> Unit, onDismiss: () -> Unit) {
    FloatingSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.junk_stop_title),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.junk_stop_body),
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(27.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple)))
                    .tapClickable("stop_process_sheet_click", onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.junk_stop_cancel),
                    color = Color.White,
                    style = TextStyle(fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
                )
            }
            Text(
                stringResource(R.string.junk_stop_dismiss),
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).tapClickable("stop_process_sheet_click_2", onClick = onDismiss).padding(14.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
