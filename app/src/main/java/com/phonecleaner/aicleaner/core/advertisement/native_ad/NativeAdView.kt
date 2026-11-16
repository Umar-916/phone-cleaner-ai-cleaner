package com.phonecleaner.aicleaner.core.advertisement.native_ad

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

@Composable
fun NativeAdView(
    nativeAd: NativeAd?,
    adContent: @Composable (ad: NativeAd?, nativeAdView: NativeAdView, contentView: View, mediaView: MediaView?) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val mediaViewState = remember { mutableStateOf<MediaView?>(null) }

    AndroidView(
        factory = { context ->
            val contentView = ComposeView(context).apply {
                id = contentViewId
            }

            val mediaView = MediaView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            mediaViewState.value = mediaView

            NativeAdView(context).apply {
                id = adViewId
                addView(contentView)
            }
        },
        update = { view ->
            val adView = view.findViewById<NativeAdView>(adViewId)
            val contentView = view.findViewById<ComposeView>(contentViewId)

            nativeAd?.let { ad ->
                adView.registerNativeAd(ad, mediaViewState.value)
            }

            adView.callToActionView = contentView
            contentView.setContent {
                adContent(
                    nativeAd,
                    adView,
                    contentView,
                    mediaViewState.value
                )
            }
        }
    )
}
