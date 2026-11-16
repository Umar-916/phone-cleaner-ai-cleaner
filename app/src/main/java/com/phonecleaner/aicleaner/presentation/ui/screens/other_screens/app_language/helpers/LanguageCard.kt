package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun LanguageCard(
    item: LanguageItem,
    selected: Boolean,
    highlighted: Boolean,
    onClick: (() -> Unit)? = null
) {
    val colors = CardDefaults.cardColors(
        containerColor = if (highlighted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        }
    )
    val shape = RoundedCornerShape(16.sdp)
    val cardModifier = Modifier
        .fillMaxWidth()
        .height(52.sdp)
    val border = BorderStroke(
        width = 0.4.sdp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.sdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageFlagIcon(flagCode = item.flagCode)

            Spacer(modifier = Modifier.width(12.sdp))

            Text(
                text = if (highlighted) item.name else item.displayLabel,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.ssp,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                    fontWeight = FontWeight.Medium,
                    color = if (highlighted) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                )
            )

            LanguageRadio(
                selected = selected,
                highlighted = highlighted
            )
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = { content() }
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = { content() }
        )
    }
}
