package com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 14.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondary
        )
    )

}