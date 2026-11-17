package com.phonecleaner.aicleaner.core.advertisement

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.phonecleaner.aicleaner.core.advertisement.taichi.setAppOpenOnPaidEventListener
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdFailedToLoad
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdImpressionOpportunity
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdLoaded
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import java.util.Date

class AppOpenAdManager(myApplication: Application) :
    LifecycleObserver, ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0
    private var isAdLoading = false
    private var isColdStart = true


    val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo()

    companion object {
        private const val TAG = "app_open_ad_log"
        private const val TAG_INTERNAL = "AppOpenAdManager"
        private const val SCREEN_PREMIUM = "SliderPremiumScreen"
        private const val SCREEN_BLACK_PREMIUM = "BlackPremiumScreen"
        private const val SCREEN_ZIPPO = "PremiumZippoScreen"
        private const val SCREEN_HALLOWEEN_PREMIUM = "HalloweenPremiumScreen"
        private const val SCREEN_FEEDBACK = "Feedback"

        var canLoadAppOpen = false

        @SuppressLint("StaticFieldLeak")
        private var instance: AppOpenAdManager? = null

        var isAppOpenAdShowing = false

        fun requestAdIfNeeded() {
            instance?.enableLoadAndFetch()
        }

        private fun isBlockedScreen(): Boolean {
            val screen = Constants.currentScreenName
            return screen == SCREEN_PREMIUM ||
                    screen == SCREEN_BLACK_PREMIUM ||
                    screen == SCREEN_ZIPPO ||
                    screen == SCREEN_HALLOWEEN_PREMIUM ||
                    screen == SCREEN_FEEDBACK
        }
    }

    init {
        instance = this
        Log.i("lifecycleTAG", "init: AppOpenAdManager")
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        InterstitialAdManager.isActivityPaused = false
        "onStart: isPremiumUser=${isPremiumUser()}, showAppOpenAd=${RemoteConstants.showAppOpenAd}".debugRelease(
            TAG_INTERNAL
        )

        if (isColdStart) {
            isColdStart = false
            "onStart: cold start – skip show".debugRelease(TAG)
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            handleForeground()
        }, 400)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        InterstitialAdManager.isActivityPaused = true
        "onStop: app backgrounded".debugRelease(TAG)
    }

    private fun handleForeground() {
        if (currentActivity?.canRequestAds() == false || !RemoteConstants.showAppOpenAd) {
            return
        }

        if (!isAdAvailable) {
            "handleForeground: ad not available – requesting".debugRelease(TAG)
            fetchAd()
            return
        }

        showAdIfAvailable()
    }

    private fun enableLoadAndFetch() {
        canLoadAppOpen = true
        fetchAd()
    }

    fun fetchAd() {
        if (!canLoadAppOpen || !RemoteConstants.showAppOpenAd || isPremiumUser()) {
            return
        }

        if (isAdAvailable || isAdLoading) {
            "fetchAd: ad already available or loading – skipping".debugRelease(TAG)
            return
        }

        val activity = currentActivity
        if (activity != null && !activity.canRequestAds()) {
            "fetchAd: premium or no network – skipping".debugRelease(TAG)
            return
        }

        "fetchAd: starting load for id=${RemoteConstants.APP_OPEN_AD_UNIT_ID}".debugRelease(TAG)
        isAdLoading = true

        try {
            activity?.let { logAdImpressionOpportunity(it, "app_open") }

            AppOpenAd.load(
                AdRequest.Builder(RemoteConstants.APP_OPEN_AD_UNIT_ID).build(),
                object : AdLoadCallback<AppOpenAd> {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        "fetchAd – onAdLoaded".debugRelease(TAG)
                        appOpenAd = ad
                        loadTime = Date().time
                        isAdLoading = false
                        activity?.let { logAdLoaded(it, "app_open") }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        "fetchAd – onAdFailedToLoad: $loadAdError".debugRelease(TAG)
                        appOpenAd = null
                        isAdLoading = false
                        activity?.let { logAdFailedToLoad(it, "app_open") }
                    }
                }
            )
        } catch (e: Exception) {
            "fetchAd – exception: ${e.message}".debugRelease(TAG)
            isAdLoading = false
        }
    }

    fun showAdIfAvailable() {
        if (currentActivity?.canRequestAds() != true) {
            "showAdIfAvailable: premium or no network – skip".debugRelease(TAG)
            return
        }

        if (!RemoteConstants.showAppOpenAd) {
            "showAdIfAvailable: remote flag off – skip".debugRelease(TAG)
            return
        }

        if (BillingUtilsIAP.isPurchaseFlowActive) {
            "showAdIfAvailable: purchase sheet open – skip".debugRelease(TAG)
            return
        }

        if (isBlockedScreen()) {
            "showAdIfAvailable: blocked screen=${Constants.currentScreenName} – skip".debugRelease(
                TAG
            )
            return
        }

        if (isOverlappingFullScreenAd()) {
            "showAdIfAvailable: interstitial/rewarded overlapping – skip".debugRelease(TAG)
            return
        }

        if (!InterstitialAdManager.isInterstitialCappingComplete()) {
            "showAdIfAvailable: interstitial capping not complete – skip".debugRelease(TAG)
            return
        }

        if (isAppOpenAdShowing) {
            "showAdIfAvailable: app open already showing – skip".debugRelease(TAG)
            return
        }

        if (!isAdAvailable) {
            "showAdIfAvailable: ad not ready – requesting".debugRelease(TAG)
            fetchAd()
            return
        }

        val act = currentActivity
        if (act == null) {
            "showAdIfAvailable: ad ready but no activity".debugRelease(TAG)
            return
        }

        "showAdIfAvailable: ad ready – showing on ${act.javaClass.simpleName}".debugRelease(TAG)
        appOpenAd?.adEventCallback = buildFullScreenCallback()
        appOpenAd?.show(act)
    }

    private fun isOverlappingFullScreenAd(): Boolean {
        return InterstitialAdManager.isInterstitialAdShowing ||
                InterstitialAdManager.isRuntimeAdLoading ||
                InterstitialAdManager.isInnerClickFlowActive ||
                InterstitialAdManager.isExitClickFlowActive ||
                RewardedAdManager.isRewardedAdShowing
    }

    private fun buildFullScreenCallback() = object : AppOpenAdEventCallback {

        override fun onAdClicked() {
            super.onAdClicked()
            "onAdClicked".debugRelease(TAG)
        }

        override fun onAdShowedFullScreenContent() {
            "onAdShowedFullScreenContent".debugRelease(TAG)
            isAppOpenAdShowing = true
        }

        override fun onAdImpression() {
            super.onAdImpression()
            "onAdImpression: ${appOpenAd?.placementId}".debugRelease(TAG)
        }

        override fun onAdDismissedFullScreenContent() {
            "onAdDismissedFullScreenContent".debugRelease(TAG)

            appOpenAd = null
            isAppOpenAdShowing = false

            Handler(Looper.getMainLooper()).postDelayed({
                isAppOpenAdShowing = false
            }, 1000)

            fetchAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: FullScreenContentError) {
            "onAdFailedToShowFullScreenContent: ${adError.message}".debugRelease(TAG)
            appOpenAd = null
            isAppOpenAdShowing = false
            fetchAd()
        }

        override fun onAdPaid(value: AdValue) {
            super.onAdPaid(value)
            currentActivity?.let { act ->
                setAppOpenOnPaidEventListener(
                    act,
                    value,
                    appOpenAd?.placementId?.toString().orEmpty()
                )
            }
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        updateCurrentActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        updateCurrentActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        updateCurrentActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (!isAdActivity(activity) && currentActivity == activity) {
            currentActivity = null
        }
    }

    private fun updateCurrentActivity(activity: Activity) {
        if (!isAdActivity(activity)) {
            currentActivity = activity
        }
    }

    private fun isAdActivity(activity: Activity): Boolean {
        return activity.javaClass.name.contains("AdActivity", ignoreCase = true)
    }
}
