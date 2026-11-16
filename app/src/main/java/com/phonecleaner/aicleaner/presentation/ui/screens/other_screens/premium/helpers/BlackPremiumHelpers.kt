package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.utils.ComponentHelper.debounceClickable
import com.phonecleaner.aicleaner.presentation.ui.theme.blackPremiumOrange
import com.phonecleaner.aicleaner.presentation.ui.theme.colorNine
import com.phonecleaner.aicleaner.presentation.ui.theme.colorWhite
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun BlackPremiumFeatureRow(
    iconRes: Int,
    iconBackground: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.sdp)
    ) {
        Box(
            modifier = Modifier
                .size(36.sdp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.sdp),
                contentScale = ContentScale.Fit
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.sdp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 19.ssp,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)),
                    fontWeight = FontWeight.SemiBold,
                    color = colorWhite
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 13.ssp,
                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                    fontWeight = FontWeight.Medium,
                    color = colorNine
                )
            )
        }
    }
}

@Composable
fun BlackPremiumGoProButton(
    isTrialEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(57.sdp)
            .clip(RoundedCornerShape(16.sdp))
            .background(blackPremiumOrange)
            .debounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                if (isTrialEnabled) R.string.zippo_cta_trial
                else R.string.black_premium_go_pro
            ),
            style = TextStyle(
                fontSize = 19.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun BlackPremiumCloseButton(
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.black_premium_close,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(20.sdp)
            .size(23.sdp)
            .debounceClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = stringResource(R.string.close),
            modifier = Modifier.size(24.sdp),
            contentScale = ContentScale.Fit
        )
    }
}
