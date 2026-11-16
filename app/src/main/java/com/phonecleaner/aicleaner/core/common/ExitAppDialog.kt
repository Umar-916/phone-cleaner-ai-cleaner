package com.phonecleaner.aicleaner.core.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.AdLoadListener
import com.phonecleaner.aicleaner.core.advertisement.BannerAdCompose
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.RectBannerAdManager
import com.phonecleaner.aicleaner.core.advertisement.native_ad.ComposeNativeAd
import com.phonecleaner.aicleaner.core.advertisement.native_ad.NativeAdType
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.presentation.ui.theme.colorGrayF5
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

private enum class ExitAdSlot {
    None,
    Banner,
    Native
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitAppDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.navigationBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.secondary,
        dragHandle = { DragHandlerExitDialogue() }
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterHorizontally).navigationBarsPadding(),
        ) {
            var slot by remember { mutableStateOf(ExitAdSlot.None) }
            var exitBannerAd by remember { mutableStateOf<BannerAd?>(null) }
            var isBannerLoading by remember { mutableStateOf(false) }
            var exitNativeAd by remember { mutableStateOf<NativeAd?>(null) }
            var isNativeLoaded by remember { mutableStateOf(false) }

            val nativeAdType = when (RemoteConstants.exitNativeAdSize) {
                1 -> NativeAdType.A_ONE
                2 -> NativeAdType.C_ONE
                else -> NativeAdType.A_ONE
            }

            fun hideAds() {
                slot = ExitAdSlot.None
                exitBannerAd = null
                isBannerLoading = false
                exitNativeAd = null
                isNativeLoaded = false
            }

            fun showNative() {
                if (
                    !context.canRequestAds() ||
                    nativeAdType == NativeAdType.NONE
                ) {
                    hideAds()
                    return
                }

                NativeAdManager.exitAdLoadListener = object : AdLoadListener {
                    override fun onExitAdLoaded(nativeAd: NativeAd?) {
                        if (nativeAd == null) {
                            hideAds()
                            return
                        }
                        exitNativeAd = nativeAd
                        isNativeLoaded = true
                        isBannerLoading = false
                        exitBannerAd = null
                        slot = ExitAdSlot.Native
                    }

                    override fun onExitAdFailedToLoad(error: String) {
                        hideAds()
                    }
                }

                NativeAdManager.getExitNativeAd()?.let { loaded ->
                    exitNativeAd = loaded
                    isNativeLoaded = true
                    isBannerLoading = false
                    exitBannerAd = null
                    slot = ExitAdSlot.Native
                    return
                }

                if (NativeAdManager.isExitNativeAdFailedToLoad()) {
                    hideAds()
                    return
                }

                isBannerLoading = false
                exitBannerAd = null
                isNativeLoaded = false
                exitNativeAd = null
                slot = ExitAdSlot.Native
            }

            fun showBanner(ad: BannerAd) {
                exitBannerAd = ad
                isBannerLoading = false
                exitNativeAd = null
                isNativeLoaded = false
                slot = ExitAdSlot.Banner
            }

            fun showBannerWithNativeFallback() {
                if (!context.canRequestAds()) {
                    hideAds()
                    return
                }

                RectBannerAdManager.rectBannerAdLoadListener =
                    object : RectBannerAdManager.MediumRectBannerAdLoadListener {
                        override fun onRectBannerAdLoaded(bannerAd: BannerAd) {
                            showBanner(bannerAd)
                        }

                        override fun onRectBannerAdFailedToLoad(error: String) {
                            showNative()
                        }

                        override fun onRectBannerAdImpression(message: String) = Unit
                    }

                RectBannerAdManager.getBannerMediumRectangleExitAd()?.let {
                    showBanner(it)
                    return
                }

                if (RectBannerAdManager.isBannerMediumRectangleExitAdLoading()) {
                    isBannerLoading = true
                    slot = ExitAdSlot.Banner
                    return
                }

                showNative()
            }

            val currentSlot by rememberUpdatedState(slot)
            DisposableEffect(Unit) {
                onDispose {
                    NativeAdManager.exitAdLoadListener = null
                    RectBannerAdManager.rectBannerAdLoadListener = null
                    if (currentSlot == ExitAdSlot.Banner) {
                        RectBannerAdManager.releaseShownExitBanner()
                    }
                    if (currentSlot == ExitAdSlot.Native) {
                        NativeAdManager.markExitAdAsShown()
                    }
                }
            }

            LaunchedEffect(Unit) {
                context.postAnalytic(AnalyticsEvents.TAP_TO_EXIT_DIALOG_APPEAR)
                when (RemoteConstants.exitDialougeAd) {
                    1 -> showBannerWithNativeFallback()
                    2 -> showNative()
                    else -> hideAds()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (slot != ExitAdSlot.None) {
                    when (slot) {
                        ExitAdSlot.Banner -> {
                            BannerAdCompose(
                                bannerAd = exitBannerAd,
                                isLoading = isBannerLoading,
                                loadingHeight = 250.sdp
                            )
                        }

                        ExitAdSlot.Native -> {
                            ComposeNativeAd(
                                nativeAd = exitNativeAd,
                                isNativeLoaded = isNativeLoaded,
                                adType = nativeAdType,
                                backgroundColor = MaterialTheme.colorScheme.background,
                                hideNativeAdContainer = { hideAds() }
                            )
                        }

                        ExitAdSlot.None -> Unit
                    }
                    Spacer(Modifier.height(6.sdp))
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dropShadow(
                            RoundedCornerShape(22.sdp),
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                radius = 12.sdp,
                                spread = 0.sdp,
                                offset = DpOffset(0.sdp, 4.sdp)
                            ),
                        )
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        context.postAnalytic(AnalyticsEvents.TAP_TO_EXIT_CLICK)
                        onExit()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.exit),
                        color = Color.White,
                        fontSize = 16.ssp
                    )
                }
            }
        }
    }
}

@Composable
fun DragHandlerExitDialogue() {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .size(width = 32.dp, height = 4.dp)
            .clip(RoundedCornerShape(100))
            .background(colorGrayF5)
    )
}
