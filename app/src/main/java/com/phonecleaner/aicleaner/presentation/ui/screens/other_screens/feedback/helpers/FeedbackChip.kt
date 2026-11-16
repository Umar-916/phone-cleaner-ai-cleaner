package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.colorWhite
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun FeedbackChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.sdp),
        shape = RoundedCornerShape(14.sdp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = text,
                fontSize = 12.ssp,
                fontFamily = FontFamily(
                    Font(R.font.google_sans_flex_medium)
                ),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = if (selected)
                    colorWhite
                else
                    MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChipPreview() {
    MaterialTheme {
        FeedbackChip(
            text = "Crashes",
            selected = false,
            onClick = {}
        )
    }
}