package com.phonecleaner.aicleaner.core.advertisement.native_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdImpressionOpportunity
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.delay
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.graphics.toColorInt

private fun String.toNativeCtaColor(): Color? {
    val hex = trim().let { value ->
        if (value.startsWith("#")) value else "#$value"
    }
    return runCatching { Color(hex.toColorInt()) }.getOrNull()
}

@Composable
fun ComposeNativeAd(
    nativeAd: NativeAd?,
    isNativeLoaded: Boolean = false,
    adType: NativeAdType = NativeAdType.NONE,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    ctaColor: Color = RemoteConstants.nativeAdCtaColor.toNativeCtaColor()
        ?: MaterialTheme.colorScheme.primary,
    hideNativeAdContainer: () -> Unit
) {
    var showAd by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val loadingHeight = when (adType) {
        NativeAdType.A_ONE, NativeAdType.A_TWO, NativeAdType.A_THREE -> 118.sdp
        NativeAdType.B_ONE, NativeAdType.B_TWO, NativeAdType.B_THREE -> 72.sdp
        NativeAdType.C_ONE, NativeAdType.D_ONE -> 280.sdp
        else -> 118.sdp
    }
    val corner = when (adType) {
        NativeAdType.A_TWO, NativeAdType.B_TWO -> 0.sdp
        NativeAdType.A_THREE, NativeAdType.B_THREE -> 0.sdp
        else -> 14.sdp
    }
    val shape = RoundedCornerShape(
        topStart = if (adType == NativeAdType.A_THREE || adType == NativeAdType.B_THREE) 0.sdp else corner,
        topEnd = if (adType == NativeAdType.A_THREE || adType == NativeAdType.B_THREE) 0.sdp else corner,
        bottomEnd = if (adType == NativeAdType.A_TWO || adType == NativeAdType.B_TWO) 0.sdp else 14.sdp,
        bottomStart = if (adType == NativeAdType.A_TWO || adType == NativeAdType.B_TWO) 0.sdp else 14.sdp
    )

    LaunchedEffect(isNativeLoaded, nativeAd) {
        if (isNativeLoaded && nativeAd != null) {
            showAd = false
            delay(200.milliseconds)
            showAd = true
        } else {
            showAd = false
        }
    }

    val isShowingAd = isNativeLoaded && showAd && nativeAd != null
    val containerColor = backgroundColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor, shape)
            .padding(10.sdp)
    ) {
        if (isShowingAd) {
            NativeAdView(nativeAd) { ad, adView, contentView, mediaView ->
                ad?.let {
                    LaunchedEffect(Unit) {
                        logAdImpressionOpportunity(context, "native_ad")
                    }
                    when (adType) {
                        NativeAdType.A_ONE, NativeAdType.A_TWO, NativeAdType.A_THREE -> {
                            AFourNativeAdContent(ad, adView, contentView, ctaColor, backgroundColor)
                        }

                        NativeAdType.B_ONE, NativeAdType.B_TWO, NativeAdType.B_THREE -> {
                            BOneNativeAdContent(ad, adView, contentView, ctaColor, backgroundColor)
                        }

                        NativeAdType.C_ONE, NativeAdType.D_ONE -> {
                            COneMediaNativeAdContent(
                                ad,
                                adView,
                                contentView,
                                mediaView,
                                ctaColor,
                                backgroundColor
                            )
                        }

                        else -> {
                            AFourNativeAdContent(ad, adView, contentView, ctaColor, backgroundColor)
                        }
                    }
                } ?: hideNativeAdContainer()
            }
        } else {
            Text(
                text = stringResource(R.string.loading_ad),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(loadingHeight)
                    .wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                fontSize = 12.ssp,
                lineHeight = 14.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
