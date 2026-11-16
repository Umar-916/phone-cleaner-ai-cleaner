package com.phonecleaner.aicleaner.core.advertisement.native_ad

import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AFourNativeAdContent(
    nativeAd: NativeAd,
    adView: NativeAdView,
    composeView: View,
    ctaColor: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val headlineFont = FontFamily(Font(R.font.google_sans_flex_bold))
    val bodyFont = FontFamily(Font(R.font.google_sans_flex_medium))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            val icon: Drawable? = nativeAd.icon?.drawable
            icon?.let { drawable ->
                AsyncImage(
                    model = drawable,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.sdp)
                        .clip(RoundedCornerShape(8.sdp)),
                )
                Spacer(modifier = Modifier.width(10.sdp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nativeAd.headline.orEmpty(),
                        fontSize = 13.ssp,
                        lineHeight = 16.ssp,
                        fontFamily = headlineFont,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.sdp))
                    AdBadge()
                }
                Spacer(modifier = Modifier.height(4.sdp))
                Text(
                    text = nativeAd.body.orEmpty(),
                    fontFamily = bodyFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.ssp,
                    lineHeight = 13.ssp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.surface,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.sdp))

        adView.registerNativeAd(nativeAd, null)

        nativeAd.callToAction?.let { cta ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.sdp)
                    .clip(RoundedCornerShape(10.sdp))
                    .background(ctaColor)
                    .combinedClickable { composeView.performClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fontSize = 13.ssp,
                    lineHeight = 16.ssp,
                    fontFamily = headlineFont,
                    fontWeight = FontWeight.Bold,
                    text = cta,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AdBadge() {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onTertiary,
                shape = RoundedCornerShape(4.sdp)
            )
            .padding(horizontal = 4.sdp, vertical = 1.sdp)
    ) {
        Text(
            text = stringResource(R.string.ad),
            lineHeight = 10.ssp,
            fontSize = 8.ssp,
            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}
