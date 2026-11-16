package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun LanguageDoneButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 8.sdp)
            .height(32.sdp)
            .widthIn(min = 72.sdp),
        shape = RoundedCornerShape(20.sdp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(
            horizontal = 16.sdp,
            vertical = 0.sdp
        )
    ) {
        Text(
            text = stringResource(R.string.done),
            fontSize = 13.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
            fontWeight = FontWeight.SemiBold
        )
    }
}
