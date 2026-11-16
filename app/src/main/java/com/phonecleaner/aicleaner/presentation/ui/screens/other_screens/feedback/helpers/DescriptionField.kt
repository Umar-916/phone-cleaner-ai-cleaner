package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedTab: Int
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.sdp),
        shape = RoundedCornerShape(15.sdp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.sdp)
                .padding(12.sdp),
            contentAlignment = Alignment.TopStart
        ) {

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.ssp,
                    fontFamily = FontFamily(
                        Font(R.font.google_sans_flex_regular)
                    ),
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.surface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.surface),
                decorationBox = { innerTextField ->

                    if (value.isEmpty()) {
                        Text(
                            text = if (selectedTab == 0) stringResource(
                                R.string.describe_the_issue_you_ve_encountered_here
                            ) else stringResource(
                                R.string.tell_us_how_can_we_improve_our_app
                            ),
                            fontSize = 12.ssp,
                            color = MaterialTheme.colorScheme.surface,
                            fontFamily = FontFamily(
                                Font(R.font.google_sans_flex_regular)
                            )
                        )
                    }

                    innerTextField()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DescriptionFieldPreview() {
    MaterialTheme {
        DescriptionField(
            value = "",
            onValueChange = {}, selectedTab = 0
        )
    }
}