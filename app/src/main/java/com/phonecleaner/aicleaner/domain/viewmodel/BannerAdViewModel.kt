package com.phonecleaner.aicleaner.domain.viewmodel

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadResult
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.phonecleaner.aicleaner.core.advertisement.taichi.setBannerOnPaidEventListener
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdFailedToLoad
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdImpressionOpportunity
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdLoaded
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BannerAdViewModel : ViewModel() {

    private val _bannerAd = MutableStateFlow<BannerAd?>(null)
    val bannerAd: StateFlow<BannerAd?> = _bannerAd

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var loadAttempted = false

    fun loadIfNeeded(activity: Activity?) {
        val currentActivity = activity ?: return
        if (
            loadAttempted ||
            _isLoading.value ||
            _bannerAd.value != null ||
            !homeScreenReachedForCurrentSession.value ||
            !currentActivity.canRequestAds() ||
            RemoteConstants.innerBannerAdType == 0
        ) {
            return
        }

        val adUnitId = RemoteConstants.INNER_BANNER_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            return
        }

        loadAttempted = true
        _isLoading.value = true
        viewModelScope.launch {
            "Banner ad loading started type=${RemoteConstants.innerBannerAdType}".debugRelease("banner_ad_log")
            logAdImpressionOpportunity(currentActivity, "banner_ad")

            val adWidth = (currentActivity.resources.displayMetrics.widthPixels /
                currentActivity.resources.displayMetrics.density).toInt()
            @Suppress("DEPRECATION")
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                currentActivity,
                adWidth
            )
            val requestBuilder = BannerAdRequest.Builder(adUnitId = adUnitId, adSize = adSize)
            if (RemoteConstants.innerBannerAdType == 2) {
                requestBuilder.setGoogleExtrasBundle(
                    Bundle().apply { putString("collapsible", "bottom") }
                )
            }
            val request = requestBuilder.build()

            when (val result = BannerAd.load(request)) {
                is AdLoadResult.Success -> {
                    result.ad.adEventCallback = object : BannerAdEventCallback {
                        override fun onAdImpression() {
                            "banner ad Impression".debugRelease("banner_ad_log")
                        }

                        override fun onAdClicked() {
                            "banner ad Clicked".debugRelease("banner_ad_log")
                        }

                        override fun onAdPaid(value: AdValue) {
                            super.onAdPaid(value)
                            setBannerOnPaidEventListener(currentActivity, value, adUnitId)
                        }
                    }
                    _bannerAd.value = result.ad
                    _isLoading.value = false
                    "banner loaded with id $adUnitId collapsible=${result.ad.isCollapsible()}".debugRelease("banner_ad_log")
                    logAdLoaded(currentActivity, "banner_ad")
                }

                is AdLoadResult.Failure -> {
                    _bannerAd.value = null
                    _isLoading.value = false
                    "banner failed".debugRelease("banner_ad_log")
                    logAdFailedToLoad(currentActivity, "banner_ad")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _bannerAd.value?.destroy()
        _bannerAd.value = null
    }
}
