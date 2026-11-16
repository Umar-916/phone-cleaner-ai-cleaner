package com.phonecleaner.aicleaner.core.advertisement.native_ad

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.phonecleaner.aicleaner.R
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun COneMediaNativeAdContent(
    nativeAd: NativeAd,
    adView: NativeAdView,
    composeView: View,
    mediaView: MediaView?,
    ctaColor: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val headlineFont = FontFamily(Font(R.font.google_sans_flex_bold))
    val bodyFont = FontFamily(Font(R.font.google_sans_flex_medium))

    LaunchedEffect(nativeAd, mediaView) {
        mediaView?.let {
            try {
                adView.registerNativeAd(nativeAd, it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
        }

        Spacer(modifier = Modifier.height(10.sdp))

        mediaView?.let { mv ->
            AndroidView(
                factory = { ctx ->
                    (mv.parent as? ViewGroup)?.removeView(mv)
                    mv.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (160 * ctx.resources.displayMetrics.density).toInt()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.sdp)
                    .clip(RoundedCornerShape(10.sdp))
            )
        }

        nativeAd.body?.let { body ->
            Spacer(modifier = Modifier.height(10.sdp))
            Text(
                text = body,
                fontFamily = bodyFont,
                fontWeight = FontWeight.Medium,
                fontSize = 11.ssp,
                lineHeight = 15.ssp,
                maxLines = 3,
                color = MaterialTheme.colorScheme.surface,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        nativeAd.callToAction?.let { cta ->
            Spacer(modifier = Modifier.height(10.sdp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.sdp)
                    .clip(RoundedCornerShape(10.sdp))
                    .background(ctaColor)
                    .combinedClickable { composeView.performClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    fontSize = 14.ssp,
                    lineHeight = 18.ssp,
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
