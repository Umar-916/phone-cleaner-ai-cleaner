package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers

import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.presentation.ui.theme.colorWhite
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun FeedbackToggle(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current

    val tabs = listOf(
        stringResource(R.string.issues),
        stringResource(R.string.suggestions)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(14.sdp)
            )
            .padding(vertical = 6.sdp)
    ) {

        tabs.forEachIndexed { index, title ->

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTab == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.sdp)
                    )
                    .tapPlainClickable("feedback_toggle_tap_1") {
                        context.postAnalytic("feedback_tab_click_${if (index == 0) "issues" else "suggestions"}")
                        onTabSelected(index)
                    }
                    .padding(horizontal = 12.sdp, vertical = 10.sdp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    Image(
                        modifier = Modifier.padding(
                            start = if (index == 0) 8.sdp else 0.sdp
                        ),
                        painter = painterResource(
                            if (selectedTab == index)
                                R.drawable.radio_button_selected
                            else
                                R.drawable.radio_button_unselected
                        ),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.sdp))

                    Text(
                        text = title,
                        modifier = Modifier.padding(
                            start = if (index == 0) 16.sdp else 0.sdp
                        ),
                        fontSize = 14.ssp,
                        fontFamily = FontFamily(
                            Font(R.font.google_sans_flex_medium)
                        ),
                        fontWeight = FontWeight.Medium,
                        color = if (selectedTab == index)
                            colorWhite
                        else
                            MaterialTheme.colorScheme.surface
                    )
                }
            }

            if (index == 0) {
                Spacer(modifier = Modifier.width(8.sdp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TogglePreview() {
    MaterialTheme {
        FeedbackToggle(
            selectedTab = 0,
            onTabSelected = {}
        )
    }
}