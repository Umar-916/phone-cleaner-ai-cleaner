package com.phonecleaner.aicleaner.core.advertisement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.phonecleaner.aicleaner.core.advertisement.taichi.setNativeOnPaidEventListener
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdFailedToLoad
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdLoaded
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.remote.RemoteConstants

@SuppressLint("StaticFieldLeak")
object NativeAdManager {

    const val TAG = "native_ad_log"

    @SuppressLint("StaticFieldLeak")
    var activity: Activity? = null

    /** Inner Native */

    private var innerNativeAd: NativeAd? = null
    var isGenericNativeAdLoading = false

    private var innerAdImpressionRecorded = false
    var innerAdLoadListener: AdLoadListener? = null

    fun loadInnerNativeAd(context: Context, adUnitId: String) {
        if (!context.canRequestAds()) {
            return
        }

        if (innerNativeAd != null || isGenericNativeAdLoading) {
            "PreLoadNative: Inner Native ad is already loaded or loading".debugRelease(TAG)
            return
        }

        if (isPremiumUser()) {
            innerAdLoadListener?.onGenericAdFailedToLoad("PREMIUM")
            return
        }

        "LoadNative: Inner Native Loading Native Ad with ID: $adUnitId".debugRelease(TAG)

        isGenericNativeAdLoading = true
        innerAdImpressionRecorded = false

        val adRequest =
            NativeAdRequest.Builder(
                adUnitId = adUnitId,
                listOf(NativeAd.NativeAdType.NATIVE)
            )
                .build()

        val adCallback =
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    "Inner Native onAdLoaded:".debugRelease(TAG)
                    setInnerEventCallback(nativeAd)
                    innerNativeAd = nativeAd
                    isGenericNativeAdLoading = false
                    innerAdImpressionRecorded = false
                    innerAdLoadListener?.onGenericNativeAdLoaded(innerNativeAd)
                    logAdLoaded(context, "inner_native_ad")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    "Inner Native onAdFailedToLoad: $adError".debugRelease(TAG)
                    isGenericNativeAdLoading = false
                    innerNativeAd = null
                    innerAdImpressionRecorded = false
                    innerAdLoadListener?.onGenericAdFailedToLoad(adError.message)
                    logAdFailedToLoad(context, "inner_native_ad")
                }
            }
        NativeAdLoader.load(adRequest, adCallback)
    }

    fun getInnerNativeAd(): NativeAd? {
        return innerNativeAd
    }

    fun isInnerNativeAdLoading(): Boolean {
        return isGenericNativeAdLoading
    }


    private fun setInnerEventCallback(nativeAd: NativeAd) {
        nativeAd.adEventCallback =
            object : NativeAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Inner Native ad showed full screen content.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Inner Native ad dismissed full screen content.")
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    Log.d(
                        TAG,
                        "Inner Native ad failed to show full screen content with error: $fullScreenContentError",
                    )
                }

                override fun onAdImpression() {

                    Log.d(TAG, "Inner Native ad recorded an impression.")
                    innerNativeAd = null
                    innerAdImpressionRecorded = true
                    innerAdLoadListener?.onGenericAdImpression()
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Inner Native ad recorded a click.")
                    innerAdLoadListener?.onGenericAdClicked()
                }

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    activity?.let {
                        setNativeOnPaidEventListener(it, value, nativeAd.placementId.toString())
                    }
                }
            }
    }

    /** Feature Native */

    private var featureNativeAd: NativeAd? = null
    private var isFeatureNativeAdLoading = false
    private var featureAdImpressionRecorded = false
    var featureAdLoadListener: AdLoadListener? = null

    fun loadFeatureNativeAd(context: Context, adUnitId: String) {
        if (!context.canRequestAds()) {
            return
        }
        if (featureNativeAd != null || isFeatureNativeAdLoading) {
            "PreLoadNative: Feature Native ad is already loaded or loading".debugRelease(TAG)
            return
        }

        if (isPremiumUser()) {
            featureAdLoadListener?.onFeatureAdFailedToLoad("PREMIUM")
            return
        }

        "LoadNative: Feature Native Loading Native Ad with ID: $adUnitId".debugRelease(TAG)

        isFeatureNativeAdLoading = true
        featureAdImpressionRecorded = false

        val adRequest =
            NativeAdRequest.Builder(
                adUnitId = adUnitId,
                listOf(NativeAd.NativeAdType.NATIVE)
            )
                .build()

        val adCallback =
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    "Feature native onAdLoaded:".debugRelease(TAG)
                    setFeatureEventCallback(nativeAd)
                    featureNativeAd = nativeAd
                    isFeatureNativeAdLoading = false
                    featureAdImpressionRecorded = false
                    featureAdLoadListener?.onFeatureNativeAdLoaded(featureNativeAd)
                    logAdLoaded(context, "feature_native_ad")
                    context.postAnalytic("feature_native_loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    "Feature Native onAdFailedToLoad: $adError".debugRelease(TAG)
                    isFeatureNativeAdLoading = false
                    featureNativeAd = null
                    featureAdImpressionRecorded = false
                    featureAdLoadListener?.onFeatureAdFailedToLoad(adError.message)
                    logAdFailedToLoad(context, "feature_native_ad")
                    context.postAnalytic("feature_native_failed")
                }
            }
        NativeAdLoader.load(adRequest, adCallback)
    }

    fun getFeatureNativeAd(): NativeAd? {
        return featureNativeAd
    }

    fun isFeatureNativeAdLoading(): Boolean {
        return isFeatureNativeAdLoading
    }

    fun clearFeatureNativeAd() {
        featureNativeAd = null
        isFeatureNativeAdLoading = false
        featureAdImpressionRecorded = false
        featureAdLoadListener = null
    }

    fun markFeatureAdAsShown() {
        "markFeatureAdAsShown: Marking feature ad as shown".debugRelease(TAG)
        featureNativeAd = null
        isFeatureNativeAdLoading = false
        featureAdImpressionRecorded = false
    }

    private fun setFeatureEventCallback(nativeAd: NativeAd) {
        nativeAd.adEventCallback =
            object : NativeAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Feature Native ad showed full screen content.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Feature Native ad dismissed full screen content.")
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    Log.d(
                        TAG,
                        "Feature Native ad failed to show full screen content with error: $fullScreenContentError",
                    )
                }

                override fun onAdImpression() {
                    if (!featureAdImpressionRecorded) {
                        Log.d(TAG, "Feature Native ad recorded an impression.")
                        featureAdImpressionRecorded = true
                        featureAdLoadListener?.onFeatureAdImpression()
                    }
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Feature Native ad recorded a click.")
                    featureAdLoadListener?.onFeatureAdClicked()
                }

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    activity?.let {
                        setNativeOnPaidEventListener(it, value, nativeAd.placementId.toString())
                    }
                }
            }
    }

    /** Language Two Native */

    private var languageNativeAd: NativeAd? = null
    private var isLanguageNativeAdLoading = false
    private var languageAdImpressionRecorded = false
    var languageAdLoadListener: AdLoadListener? = null

    fun loadLanguageNativeAd(context: Context, adUnitId: String) {
        if (!context.canRequestAds()) {
            return
        }
        if (languageNativeAd != null || isLanguageNativeAdLoading) {
            "PreLoadNative: language Native ad is already loaded or loading".debugRelease(TAG)
            return
        }

        if (isPremiumUser()) {
            languageAdLoadListener?.onGenericAdFailedToLoad("PREMIUM")
            return
        }

        "LoadNative: language Native Loading Native Ad with ID: $adUnitId".debugRelease(TAG)
        isLanguageNativeAdLoading = true
        languageAdImpressionRecorded = false

        val adRequest =
            NativeAdRequest.Builder(
                adUnitId = adUnitId,
                listOf(NativeAd.NativeAdType.NATIVE)
            )
                .build()

        val adCallback =
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    "Language native onAdLoaded:".debugRelease(TAG)
                    setLanguageEventCallback(nativeAd)
                    languageNativeAd = nativeAd
                    isLanguageNativeAdLoading = false
                    languageAdImpressionRecorded = false
                    languageAdLoadListener?.onLanguageNativeAdLoaded(languageNativeAd)
                    logAdLoaded(context, "language_native_ad")
                    context.postAnalytic("ob_language_native_loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    "language Native onAdFailedToLoad: $adError".debugRelease(TAG)
                    isLanguageNativeAdLoading = false
                    languageNativeAd = null
                    languageAdImpressionRecorded = false
                    languageAdLoadListener?.onLanguageAdFailedToLoad(adError.message)
                    logAdFailedToLoad(context, "language_native_ad")
                    context.postAnalytic("ob_language_native_failed")
                }
            }
        NativeAdLoader.load(adRequest, adCallback)
    }

    fun hasLanguageAdRecordedImpression(): Boolean {
        return languageAdImpressionRecorded
    }

    fun getLanguageNativeAd(): NativeAd? {
        return languageNativeAd
    }

    fun isLanguageScreenNativeAdLoading(): Boolean {
        return isLanguageNativeAdLoading
    }

    fun clearLanguageNativeAd() {
        languageNativeAd = null
        isLanguageNativeAdLoading = false
        languageAdImpressionRecorded = false
        languageAdLoadListener = null
    }

    fun markLanguageAdAsShown() {
        "markLanguageAdAsShown: Marking language two ad as shown".debugRelease("check_lang_shown")
        languageNativeAd = null
        isLanguageNativeAdLoading = false
        languageAdImpressionRecorded = false
    }

    private fun setLanguageEventCallback(nativeAd: NativeAd) {
        nativeAd.adEventCallback =
            object : NativeAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Language Native ad showed full screen content.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Language Native ad dismissed full screen content.")
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    Log.d(
                        TAG,
                        "Language ad failed to show full screen content with error: $fullScreenContentError",
                    )
                }

                override fun onAdImpression() {
                    if (!languageAdImpressionRecorded) {
                        Log.d(TAG, "Language Native ad recorded an impression.")
                        languageAdImpressionRecorded = true
                    }
                    languageAdLoadListener?.onLanguageAdImpression()
                    languageNativeAd = null
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Language Native ad recorded a click.")
                    languageAdLoadListener?.onGenericAdClicked()
                }

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    activity?.let {
                        setNativeOnPaidEventListener(it, value, nativeAd.placementId.toString())
                    }
                }
            }
    }

    /** Exit Native */
    private var exitNativeAd: NativeAd? = null
    private var isExitNativeAdLoading = false
    private var isExitNativeFailed = false
    private var exitAdImpressionRecorded = false
    var exitAdLoadListener: AdLoadListener? = null

    fun loadExitNativeAd(context: Context, adUnitId: String) {
        if (!context.canRequestAds()) {
            return
        }
        if (exitNativeAd != null || isExitNativeAdLoading) {
            "PreLoadNative: Exit Native ad is already loaded or loading".debugRelease(TAG)
            return
        }

        if (isPremiumUser()) {
            exitAdLoadListener?.onExitAdFailedToLoad("PREMIUM")
            return
        }

        "LoadNative: Exit Native Loading Native Ad with ID: $adUnitId".debugRelease(TAG)
        isExitNativeAdLoading = true
        isExitNativeFailed = false
        exitAdImpressionRecorded = false

        val adRequest =
            NativeAdRequest.Builder(
                adUnitId = adUnitId,
                listOf(NativeAd.NativeAdType.NATIVE)
            )
                .build()

        val adCallback =
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    isExitNativeFailed = false
                    "Exit native onAdLoaded:".debugRelease(TAG)
                    setExitEventCallback(nativeAd)
                    exitNativeAd = nativeAd
                    isExitNativeAdLoading = false
                    exitAdImpressionRecorded = false
                    exitAdLoadListener?.onExitAdLoaded(exitNativeAd)
                    logAdLoaded(context, "exit_native_ad")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    "Exit Native onAdFailedToLoad: $adError".debugRelease(TAG)
                    isExitNativeAdLoading = false
                    isExitNativeFailed = true
                    exitNativeAd = null
                    exitAdImpressionRecorded = false
                    exitAdLoadListener?.onExitAdFailedToLoad(adError.message)
                    logAdFailedToLoad(context, "exit_native_ad")
                }
            }
        NativeAdLoader.load(adRequest, adCallback)
    }

    fun getExitNativeAd(): NativeAd? {
        return exitNativeAd
    }

    fun isExitNativeAdFailedToLoad(): Boolean {
        return isExitNativeFailed
    }

    fun isExitNativeAdLoading(): Boolean {
        return isExitNativeAdLoading
    }

    fun preloadExitNativeIfNeeded(context: Context) {
        if (
            RemoteConstants.exitDialougeAd == 0 ||
            !context.canRequestAds()
        ) {
            return
        }
        val adUnitId = RemoteConstants.EXIT_NATIVE_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            return
        }
        loadExitNativeAd(context, adUnitId)
    }

    fun clearExitNativeAd() {
        exitNativeAd = null
        isExitNativeAdLoading = false
        isExitNativeFailed = false
        exitAdImpressionRecorded = false
        exitAdLoadListener = null
    }

    fun markExitAdAsShown() {
        if (isExitNativeAdLoading) {
            return
        }
        "markExitAdAsShown: Marking exit ad as shown".debugRelease(TAG)
        exitNativeAd = null
        exitAdImpressionRecorded = false
    }

    private fun setExitEventCallback(nativeAd: NativeAd) {
        nativeAd.adEventCallback =
            object : NativeAdEventCallback {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Exit Native ad showed full screen content.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Exit Native ad dismissed full screen content.")
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    Log.d(
                        TAG,
                        "Exit Native ad failed to show full screen content with error: $fullScreenContentError",
                    )
                }

                override fun onAdImpression() {
                    if (!exitAdImpressionRecorded) {
                        exitAdImpressionRecorded = true
                        Log.d(TAG, "Exit Native ad recorded an impression.")
                    }
                    exitAdLoadListener?.onExitAdImpression()
                    exitNativeAd = null
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Exit Native ad recorded a click.")
                    exitAdLoadListener?.onExitAdClicked()
                }

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    activity?.let {
                        setNativeOnPaidEventListener(it, value, nativeAd.placementId.toString())
                    }
                }
            }
    }
}

interface AdLoadListener {
    fun onGenericAdCalled() {}
    fun onGenericNativeAdLoaded(nativeAd: NativeAd?) {}
    fun onGenericAdFailedToLoad(error: String) {}

    fun onExitAdLoaded(nativeAd: NativeAd?) {
        Log.i("TAG", "onAdLoaded: Native Ad Loaded")
    }

    fun onExitAdFailedToLoad(error: String) {
        Log.i("TAG", "onAdFailedToLoad: $error")
    }

    fun onLanguageNativeAdLoaded(nativeAd: NativeAd?) {
        Log.i("TAG", "onLanguageNativeAdLoaded: ")
    }

    fun onLanguageAdFailedToLoad(error: String) {
        Log.i("TAG", "onLanguageAdFailedToLoad: ")
    }

    fun onFeatureNativeAdLoaded(nativeAd: NativeAd?) {
        Log.i("TAG", "onFeatureNativeAdLoaded: ")
    }

    fun onFeatureAdFailedToLoad(error: String) {
        Log.i("TAG", "onFeatureAdFailedToLoad: ")
    }

    fun onGenericAdImpression() {
        Log.i("TAG", "onGenericAdImpression: ")
    }

    fun onLanguageAdImpression() {
        Log.i("TAG", "onGenericAdImpression: ")
    }

    fun onGenericAdClicked() {
        Log.i("TAG", "onGenericAdClicked: ")
    }


    fun onFeatureAdImpression() {
        Log.i("TAG", "onFeatureAdImpression: ")
    }

    fun onFeatureAdClicked() {
        Log.i("TAG", "onFeatureAdClicked: ")
    }

    fun onExitAdImpression() {
        Log.i("TAG", "onExitAdImpression: ")
    }

    fun onExitAdClicked() {
        Log.i("TAG", "onExitAdClicked: ")
    }
}