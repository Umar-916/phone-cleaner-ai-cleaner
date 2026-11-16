package com.phonecleaner.aicleaner.domain.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.core.advertisement.AdLoadListener
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.RectBannerAdManager
import com.phonecleaner.aicleaner.core.advertisement.native_ad.NativeAdType
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class LanguageAdSlot {
    None,
    Banner,
    Native
}

class LanguageAdsViewModel : ViewModel() {

    private val _adSlot = MutableStateFlow(LanguageAdSlot.None)
    val adSlot: StateFlow<LanguageAdSlot> = _adSlot

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd

    private val _isNativeLoaded = MutableStateFlow(false)
    val isNativeLoaded: StateFlow<Boolean> = _isNativeLoaded

    private val _bannerAd = MutableStateFlow<BannerAd?>(null)
    val bannerAd: StateFlow<BannerAd?> = _bannerAd

    private val _isBannerLoading = MutableStateFlow(false)
    val isBannerLoading: StateFlow<Boolean> = _isBannerLoading

    private var useInnerNative = false

    val nativeAdType: NativeAdType
        get() {
            val size = if (useInnerNative) {
                RemoteConstants.homeNativeAdSize
            } else {
                RemoteConstants.languageNativeAdSize
            }
            return when (size) {
                1 -> NativeAdType.A_ONE
                2 -> NativeAdType.C_ONE
                else -> if (useInnerNative) NativeAdType.NONE else NativeAdType.C_ONE
            }
        }

    private var hasStartedSetup = false
    private var nativeAdFailed = false
    private var nativeAdPopulated = false
    private var languageListener: AdLoadListener? = null
    private var innerListener: AdLoadListener? = null

    fun setupAds(activity: Activity?) {
        val currentActivity = activity ?: return
        if (hasStartedSetup) {
            return
        }
        hasStartedSetup = true

        if (!currentActivity.canRequestAds()) {
            hideAds()
            return
        }

        if (homeScreenReachedForCurrentSession.value) {
            useInnerNative = true
            hideBanner()
            showInnerNative(currentActivity)
            return
        }

        val adType = RemoteConstants.languageScreenAdType
        if (adType != 1 && adType != 2) {
            hideAds()
            return
        }

        if (adType == 1) {
            showBannerWithNativeFallback(currentActivity)
        } else {
            hideBanner()
            showNative(currentActivity)
        }
    }

    fun hideNative() {
        _nativeAd.value = null
        _isNativeLoaded.value = false
        if (_adSlot.value == LanguageAdSlot.Native) {
            _adSlot.value = LanguageAdSlot.None
        }
    }

    private fun showBannerWithNativeFallback(activity: Activity) {
        if (!activity.canRequestAds()) {
            hideBanner()
            return
        }

        hideNative()

        if (RectBannerAdManager.isLanguageBannerAdMediumRectangleLanguageLoaded()) {
            val loaded = RectBannerAdManager.getLanguageBannerMediumRectangleLanguageAd()
            if (loaded != null) {
                showBanner(loaded)
                return
            }
        }

        if (RectBannerAdManager.isLanguageBannerMediumRectangleLanguageAdLoading()) {
            waitForBanner(activity)
            return
        }

        hideBanner()
        showNative(activity)
    }

    private fun waitForBanner(activity: Activity) {
        _isBannerLoading.value = true
        _adSlot.value = LanguageAdSlot.Banner
        RectBannerAdManager.languageRectBannerAdLoadListener =
            object : RectBannerAdManager.MediumLanguageRectBannerAdLoadListener {
                override fun onLanguageRectBannerAdLoaded(bannerAd: BannerAd) {
                    showBanner(bannerAd)
                }

                override fun onLanguageRectBannerAdFailedToLoad(error: String) {
                    hideBanner()
                    showNative(activity)
                }

                override fun onLanguageRectBannerAdImpression(message: String) = Unit
            }
    }

    private fun showBanner(bannerAd: BannerAd) {
        _bannerAd.value = bannerAd
        _isBannerLoading.value = false
        _adSlot.value = LanguageAdSlot.Banner
    }

    private fun hideBanner() {
        _bannerAd.value = null
        _isBannerLoading.value = false
        if (_adSlot.value == LanguageAdSlot.Banner) {
            _adSlot.value = LanguageAdSlot.None
        }
    }

    private fun showNative(activity: Activity) {
        if (
            !activity.canRequestAds() ||
            nativeAdFailed ||
            nativeAdType == NativeAdType.NONE
        ) {
            hideNative()
            return
        }

        if (nativeAdPopulated && _nativeAd.value != null) {
            _adSlot.value = LanguageAdSlot.Native
            _isNativeLoaded.value = true
            return
        }

        attachNativeListener()

        val preloaded = NativeAdManager.getLanguageNativeAd()
        if (preloaded != null) {
            populateNative(preloaded)
            return
        }

        if (NativeAdManager.isLanguageScreenNativeAdLoading()) {
            _adSlot.value = LanguageAdSlot.Native
            return
        }

        hideNative()
    }

    private fun showInnerNative(activity: Activity) {
        if (!activity.canRequestAds() || nativeAdType == NativeAdType.NONE) {
            hideNative()
            return
        }

        innerListener = object : AdLoadListener {
            override fun onGenericNativeAdLoaded(nativeAd: NativeAd?) {
                populateNative(nativeAd)
            }

            override fun onGenericAdFailedToLoad(error: String) {
                hideNative()
            }
        }
        NativeAdManager.innerAdLoadListener = innerListener

        val ready = NativeAdManager.getInnerNativeAd()
        if (ready != null) {
            populateNative(ready)
            return
        }

        if (NativeAdManager.isInnerNativeAdLoading()) {
            _adSlot.value = LanguageAdSlot.Native
            return
        }

        val adUnitId = RemoteConstants.INNER_NATIVE_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            hideNative()
            return
        }

        _adSlot.value = LanguageAdSlot.Native
        activity.postAnalytic("inner_native_call")
        NativeAdManager.loadInnerNativeAd(activity, adUnitId)
    }

    private fun attachNativeListener() {
        languageListener = object : AdLoadListener {
            override fun onLanguageNativeAdLoaded(nativeAd: NativeAd?) {
                populateNative(nativeAd)
            }

            override fun onLanguageAdFailedToLoad(error: String) {
                nativeAdFailed = false
                hideNative()
            }
        }
        NativeAdManager.languageAdLoadListener = languageListener
    }

    private fun populateNative(nativeAd: NativeAd?) {
        if (nativeAd == null) {
            nativeAdFailed = true
            hideNative()
            return
        }
        nativeAdPopulated = true
        nativeAdFailed = false
        _nativeAd.value = nativeAd
        _isNativeLoaded.value = true
        _adSlot.value = LanguageAdSlot.Native
    }

    private fun hideAds() {
        hideBanner()
        hideNative()
        _adSlot.value = LanguageAdSlot.None
    }

    override fun onCleared() {
        super.onCleared()
        if (NativeAdManager.languageAdLoadListener === languageListener) {
            NativeAdManager.languageAdLoadListener = null
        }
        if (NativeAdManager.innerAdLoadListener === innerListener) {
            NativeAdManager.innerAdLoadListener = null
        }
        RectBannerAdManager.languageRectBannerAdLoadListener = null
    }
}
