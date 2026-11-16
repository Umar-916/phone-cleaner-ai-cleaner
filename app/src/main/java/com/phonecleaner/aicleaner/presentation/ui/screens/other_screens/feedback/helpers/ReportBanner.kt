package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun ReportBanner(
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.sdp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.sdp, end = 10.sdp, top = 11.sdp, bottom = 11.sdp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(
                    R.string.report_bugs_every_time_you_encounter_problems_to_help_us_solve_them_faster
                ),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.ssp,
                lineHeight = 16.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(2.sdp))

            Image(
                painter = painterResource(R.drawable.feed_back_close),
                contentDescription = stringResource(R.string.close),
                modifier = Modifier
                    .padding(4.sdp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.postAnalytic("feedback_report_banner_cancel")
                        onClose()
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportBannerPreview() {
    MaterialTheme {
        ReportBanner()
    }
}