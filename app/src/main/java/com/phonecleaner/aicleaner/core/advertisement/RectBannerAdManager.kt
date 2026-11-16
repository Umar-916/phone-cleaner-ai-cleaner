package com.phonecleaner.aicleaner.core.advertisement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
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
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object RectBannerAdManager {

    // EXIT BANNER - For Exit Dialog
    var rectBannerAd: BannerAd? = null
    private var rectBannerAdView: View? = null
    private var isMediumRectangleBannerAdLoading = false
    private var isMediumRectangleBannerAdLoaded = false
    private var isMediumRectBannerFailedToLoad = false
    var rectBannerPreloadAttempted = false

    // LANGUAGE BANNER - For Language Screen
    var languageRectBannerAd: BannerAd? = null
    private var languageRectBannerAdView: View? = null
    private var isLanguageMediumRectangleBannerAdLoading = false
    private var isLanguageMediumRectangleBannerAdLoaded = false
    private var isLanguageMediumRectBannerFailedToLoad = false
    var rectLanguageBannerPreloadAttempted = false

    var myActivity = NativeAdManager.activity

    var rectBannerAdLoadListener: MediumRectBannerAdLoadListener? = null
    var languageRectBannerAdLoadListener: MediumLanguageRectBannerAdLoadListener? = null

    // ==================== EXIT BANNER METHODS ====================

    fun loadMediumRectangleExitBannerAd(
        context: Context,
        adUnitId: String,
        screenName: String,
        tag: String = "exit_rect_banner_ad_log"
    ) {
        if (isMediumRectangleBannerAdLoaded || isMediumRectangleBannerAdLoading) {
            Log.i(tag, "Exit Rect BannerAd: Ad is already loaded or loading")
            return
        }

        if (!context.canRequestAds()) {
            rectBannerAdLoadListener?.onRectBannerAdFailedToLoad("NO_ADS")
            isMediumRectBannerFailedToLoad = true
            return
        }

        if (rectBannerAd == null) {
            // Build ad request with MEDIUM_RECTANGLE size
            val adRequest = BannerAdRequest.Builder(
                adUnitId = adUnitId,
                adSize = AdSize.MEDIUM_RECTANGLE
            ).build()

            isMediumRectangleBannerAdLoading = true
            isMediumRectangleBannerAdLoaded = false
            isMediumRectBannerFailedToLoad = false

            "Load Exit Rect Banner with id : $adUnitId".debugRelease(tag)

            // Load ad asynchronously using coroutine
            CoroutineScope(Dispatchers.Main).launch {
                logAdImpressionOpportunity(context, "exit_rect_banner")
                when (val result = BannerAd.load(adRequest)) {
                    is AdLoadResult.Success -> {
                        rectBannerAd = result.ad

                        // Set up event callback
                        rectBannerAd?.adEventCallback = object : BannerAdEventCallback {

                            @SuppressLint("StaticFieldLeak")
                            override fun onAdImpression() {
                                rectBannerAdLoadListener?.onRectBannerAdImpression("IMPRESSION")
                                "Exit Rect Banner onAdImpression".debugRelease(tag)
                            }

                            override fun onAdClicked() {
                                Log.i(tag, "Exit Rect Banner onAdClicked")
                            }

                            override fun onAdPaid(value: AdValue) {
                                super.onAdPaid(value)
                                myActivity?.let {
                                    setBannerOnPaidEventListener(it, value, adUnitId)
                                }
                            }
                        }

                        isMediumRectangleBannerAdLoading = false
                        isMediumRectangleBannerAdLoaded = true
                        isMediumRectBannerFailedToLoad = false

                        rectBannerAdLoadListener?.onRectBannerAdLoaded(rectBannerAd!!)

                        "Exit Rect Banner onAdLoaded :: $adUnitId".debugRelease(tag)
                        logAdLoaded(context, "exit_rect_banner")
                    }

                    is AdLoadResult.Failure -> {
                        rectBannerAd = null
                        rectBannerAdView = null
                        isMediumRectBannerFailedToLoad = true
                        isMediumRectangleBannerAdLoaded = false
                        isMediumRectangleBannerAdLoading = false

                        val errorMessage = "${result.error.message} ${result.error.code}"
                        rectBannerAdLoadListener?.onRectBannerAdFailedToLoad(errorMessage)

                        Log.i(tag, "Exit Rect Banner onAdFailedToLoad: $errorMessage")
                        logAdFailedToLoad(context, "exit_rect_banner")
                    }
                }
            }
        }
    }

    fun isBannerMediumRectangleExitAdLoading(): Boolean {
        return isMediumRectangleBannerAdLoading
    }

    // Exit Banner - Check if loaded
    fun isBannerAdMediumRectangleExitLoaded(): Boolean {
        return isMediumRectangleBannerAdLoaded
    }

    // Exit Banner - Check if failed
    fun isExitRectBannerFailedToLoad(): Boolean {
        return isMediumRectBannerFailedToLoad
    }

    // Exit Banner - Get ad instance
    fun getBannerMediumRectangleExitAd(): BannerAd? {
        return rectBannerAd
    }

    fun releaseShownExitBanner() {
        rectBannerAd?.destroy()
        rectBannerAd = null
        rectBannerAdView = null
        isMediumRectangleBannerAdLoaded = false
        isMediumRectangleBannerAdLoading = false
        isMediumRectBannerFailedToLoad = false
        rectBannerPreloadAttempted = false
    }

    // Exit Banner - Get ad view
    fun getBannerMediumRectangleExitAdView(activity: Activity): View? {
        Log.d("ExitRectBanner", "getBannerMediumRectangleExitAdView: rectBannerAd=$rectBannerAd")
        return rectBannerAd?.getView(activity)
    }

    fun preloadExitBannerIfNeeded(context: Context) {
        if (RemoteConstants.exitDialougeAd != 1 || !context.canRequestAds()) {
            return
        }
        val adUnitId = RemoteConstants.EXIT_BANNER_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            return
        }
        loadMediumRectangleExitBannerAd(
            context = context,
            adUnitId = adUnitId,
            screenName = "Home",
            tag = "exit_rect_banner_ad_log"
        )
    }

    // ==================== LANGUAGE BANNER METHODS ====================

    fun loadMediumRectangleNewLanguageBannerAd(
        context: Context,
        adUnitId: String,
        screenName: String,
        tag: String = "rect_banner_ad_log"
    ) {
        if (isLanguageMediumRectangleBannerAdLoaded || isLanguageMediumRectangleBannerAdLoading) {
            "Language Rect Banner: Already loading or loaded".debugRelease(tag)
            return
        }

        if (!context.canRequestAds()) {
            languageRectBannerAdLoadListener?.onLanguageRectBannerAdFailedToLoad("NO_ADS")
            isLanguageMediumRectBannerFailedToLoad = true
            return
        }

        if (languageRectBannerAd == null) {
            // Build ad request with MEDIUM_RECTANGLE size
            val adRequest = BannerAdRequest.Builder(
                adUnitId = adUnitId,
                adSize = AdSize.MEDIUM_RECTANGLE
            ).build()

            isLanguageMediumRectangleBannerAdLoading = true
            isLanguageMediumRectangleBannerAdLoaded = false
            isLanguageMediumRectBannerFailedToLoad = false

            "Load Language Rect Banner with id : $adUnitId".debugRelease(tag)

            // Load ad asynchronously using coroutine
            CoroutineScope(Dispatchers.Main).launch {
                logAdImpressionOpportunity(context, "language_rect_banner")
                when (val result = BannerAd.load(adRequest)) {
                    is AdLoadResult.Success -> {
                        languageRectBannerAd = result.ad

                        // Set up event callback
                        languageRectBannerAd?.adEventCallback = object : BannerAdEventCallback {

                            @SuppressLint("StaticFieldLeak")
                            override fun onAdImpression() {
                                languageRectBannerAdLoadListener?.onLanguageRectBannerAdImpression("IMPRESSION")

                                // Don't destroy language banner on impression
                                // Uncomment below if you want to destroy it
                                /*
                                languageRectBannerAd?.destroy()
                                languageRectBannerAd = null
                                languageRectBannerAdView = null
                                isLanguageMediumRectangleBannerAdLoaded = false
                                isLanguageMediumRectBannerFailedToLoad = false
                                rectLanguageBannerPreloadAttempted = false
                                */

                                "Language Rect Banner onAdImpression".debugRelease(tag)
                            }

                            override fun onAdClicked() {
                                Log.i(tag, "Language Rect Banner onAdClicked")
                            }

                            override fun onAdPaid(value: AdValue) {
                                super.onAdPaid(value)
                                myActivity?.let {
                                    setBannerOnPaidEventListener(it, value, adUnitId)
                                }
                            }
                        }

                        isLanguageMediumRectangleBannerAdLoading = false
                        isLanguageMediumRectangleBannerAdLoaded = true
                        isLanguageMediumRectBannerFailedToLoad = false

                        languageRectBannerAdLoadListener?.onLanguageRectBannerAdLoaded(
                            languageRectBannerAd!!
                        )

                        "Language Rect Banner onAdLoaded :: $adUnitId".debugRelease(tag)
                        logAdLoaded(context, "language_rect_banner")
                        context.postAnalytic("ob_language_banner_loaded")
                    }

                    is AdLoadResult.Failure -> {
                        languageRectBannerAd = null
                        languageRectBannerAdView = null
                        isLanguageMediumRectBannerFailedToLoad = true
                        isLanguageMediumRectangleBannerAdLoading = false
                        isLanguageMediumRectangleBannerAdLoaded = false

                        val errorMessage = "${result.error.message} ${result.error.code}"
                        languageRectBannerAdLoadListener?.onLanguageRectBannerAdFailedToLoad(
                            errorMessage
                        )

                        Log.i(tag, "Language Rect Banner onAdFailedToLoad: $errorMessage")
                        logAdFailedToLoad(context, "language_rect_banner")
                        context.postAnalytic("ob_language_banner_failed")
                    }
                }
            }
        } else {
            Log.i(tag, "Language Rect Banner: languageRectBannerAd is not null")
        }
    }

    // Language Banner - Check if loading
    fun isLanguageBannerMediumRectangleLanguageAdLoading(): Boolean {
        return isLanguageMediumRectangleBannerAdLoading
    }

    // Language Banner - Check if loaded
    fun isLanguageBannerAdMediumRectangleLanguageLoaded(): Boolean {
        return isLanguageMediumRectangleBannerAdLoaded
    }

    // Language Banner - Check if failed
    fun isLanguageRectBannerFailedToLoad(): Boolean {
        return isLanguageMediumRectBannerFailedToLoad
    }

    // Language Banner - Get ad instance
    fun getLanguageBannerMediumRectangleLanguageAd(): BannerAd? {
        return languageRectBannerAd
    }

    // Language Banner - Get ad view
    fun getLanguageBannerMediumRectangleLanguageAdView(activity: Activity): View? {
        Log.d(
            "LanguageRectBanner",
            "getLanguageBannerMediumRectangleLanguageAdView: languageRectBannerAd=$languageRectBannerAd"
        )
        return languageRectBannerAd?.getView(activity)
    }

    // ==================== LEGACY/DEPRECATED METHODS ====================
    // These are kept for backward compatibility but should be avoided

    @Deprecated(
        "Use isBannerMediumRectangleExitAdLoading() instead",
        ReplaceWith("isBannerMediumRectangleExitAdLoading()")
    )
    fun isBannerMediumRectangleLanguageAdLoading(): Boolean {
        return isMediumRectangleBannerAdLoading
    }

    @Deprecated(
        "Use isBannerAdMediumRectangleExitLoaded() instead",
        ReplaceWith("isBannerAdMediumRectangleExitLoaded()")
    )
    fun isBannerAdMediumRectangleLanguageLoaded(): Boolean {
        return isMediumRectangleBannerAdLoaded
    }

    @Deprecated(
        "Use isExitRectBannerFailedToLoad() instead",
        ReplaceWith("isExitRectBannerFailedToLoad()")
    )

    fun isRectBannerFailedToLoad(): Boolean {
        return isMediumRectBannerFailedToLoad
    }

    @Deprecated(
        "Use getBannerMediumRectangleExitAd() instead",
        ReplaceWith("getBannerMediumRectangleExitAd()")
    )

    fun getBannerMediumRectangleLanguageAd(): BannerAd? {
        return rectBannerAd
    }

    @Deprecated(
        "Use getBannerMediumRectangleExitAdView() instead",
        ReplaceWith("getBannerMediumRectangleExitAdView(activity)")
    )
    fun getBannerMediumRectangleLanguageAdView(activity: Activity): View? {
        Log.d(
            "DeprecatedMethod",
            "getBannerMediumRectangleLanguageAdView called - use getBannerMediumRectangleExitAdView instead"
        )
        return rectBannerAd?.getView(activity)
    }

    // ==================== INTERFACES ====================

    interface MediumRectBannerAdLoadListener {
        fun onRectBannerAdLoaded(bannerAd: BannerAd)
        fun onRectBannerAdFailedToLoad(error: String)
        fun onRectBannerAdImpression(message: String)
    }

    interface MediumLanguageRectBannerAdLoadListener {
        fun onLanguageRectBannerAdLoaded(bannerAd: BannerAd)
        fun onLanguageRectBannerAdFailedToLoad(error: String)
        fun onLanguageRectBannerAdImpression(message: String)
    }
}