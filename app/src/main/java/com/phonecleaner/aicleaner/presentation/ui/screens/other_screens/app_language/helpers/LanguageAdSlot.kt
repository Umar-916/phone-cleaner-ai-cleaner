package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.native_ad.ComposeNativeAd
import com.phonecleaner.aicleaner.core.advertisement.native_ad.NativeAdType
import com.phonecleaner.aicleaner.domain.viewmodel.LanguageAdSlot
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun LanguageAdSlot(
    slot: LanguageAdSlot,
    nativeAd: NativeAd?,
    isNativeLoaded: Boolean,
    nativeAdType: NativeAdType,
    bannerAd: BannerAd?,
    isBannerLoading: Boolean,
    activity: Activity?,
    onHideNative: () -> Unit
) {
    when (slot) {
        LanguageAdSlot.None -> Unit

        LanguageAdSlot.Banner -> {
            LanguageRectBanner(
                bannerAd = bannerAd,
                isLoading = isBannerLoading,
                activity = activity
            )
        }

        LanguageAdSlot.Native -> {
            ComposeNativeAd(
                nativeAd = nativeAd,
                isNativeLoaded = isNativeLoaded,
                adType = nativeAdType,
                hideNativeAdContainer = onHideNative
            )
        }
    }
}

@Composable
private fun LanguageRectBanner(
    bannerAd: BannerAd?,
    isLoading: Boolean,
    activity: Activity?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.sdp)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        if (bannerAd != null && activity != null) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    FrameLayout(context).apply {
                        val adView = bannerAd.getView(activity)
                        (adView.parent as? ViewGroup)?.removeView(adView)
                        addView(adView)
                    }
                }
            )
        } else if (isLoading) {
            Text(
                text = stringResource(R.string.loading_ad),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                fontSize = 12.ssp,
                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
