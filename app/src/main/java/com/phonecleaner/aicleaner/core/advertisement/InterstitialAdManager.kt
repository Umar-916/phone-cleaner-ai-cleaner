package com.phonecleaner.aicleaner.core.advertisement

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.phonecleaner.aicleaner.core.advertisement.taichi.setInterstitialOnPaidEventListener
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdFailedToLoad
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdImpressionOpportunity
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger.logAdLoaded
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.isValidContext
import com.phonecleaner.aicleaner.core.common.timeDifference
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

object InterstitialAdManager {

    private const val TAG = "interstitial_ad_log"
    private const val INNER_PRELOAD_BEFORE_CAPPING_SECONDS = 10

    var interstitialAd: InterstitialAd? = null

    var isRuntimeAdLoading = false

    private var exitInterstitialAd: InterstitialAd? = null
    private var isExitAdLoading = false
    private var isExitFlowActive = false
    private var isExitDialogVisible = false
    val isExitClickFlowActive: Boolean
        get() = isExitFlowActive || isExitDialogVisible

    var isAdLoading = false
    var isInterstitialAdShowing = false
    var isInterstitialAdFailed = false
    var isActivityPaused = false

    private var interstitialTimeElapsed = 0L
    private val innerPreloadHandler = Handler(Looper.getMainLooper())
    private var innerPreloadRunnable: Runnable? = null
    private var innerPreloadScheduled = false
    private var isClickFlowActive = false
    val isInnerClickFlowActive: Boolean
        get() = isClickFlowActive

    private val _isLoadingDialogVisible = MutableStateFlow(false)
    val isLoadingDialogVisible: StateFlow<Boolean> = _isLoadingDialogVisible.asStateFlow()

    var interstitialAdLoadListener: InterstitialAdLoadListener? = null

    fun isInterstitialCappingComplete(): Boolean {
        return interstitialTimeElapsed.timeDifference() > RemoteConstants.interAdCapping
    }

    fun setExitDialogVisible(visible: Boolean) {
        isExitDialogVisible = visible
        if (!visible) {
            isExitFlowActive = false
        }
    }

    fun showAndLoadInterstitial(
        activity: Activity,
        adId: String,
        canShowLoading: Boolean = true,
        useCapping: Boolean = true,
        shouldPreloadNextAd: Boolean = true,
        onFromRemote: Boolean = RemoteConstants.showInnerInterAd,
        screenName: String,
        interstitialAdShowListener: InterstitialAdShowListener,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {

        if (useCapping) {
            if (!isPremiumUser() && activity.isNetworkAvailable() && onFromRemote && !AppOpenAdManager.isAppOpenAdShowing &&
                isInterstitialCappingComplete() &&
                !isInterstitialAdShowing &&
                !isExitClickFlowActive &&
                !RewardedAdManager.isRewardedAdShowing
            ) {
                runInterstitial(
                    canShowLoading,
                    activity,
                    interstitialAdShowListener,
                    screenName,
                    adId,
                    shouldPreloadNextAd,
                    showLoadingDialog,
                    hideLoadingDialog
                )
            } else {
                interstitialAdShowListener.onInterstitialAdNull()
            }
        } else {
            if (
                !isPremiumUser() && onFromRemote && activity.isNetworkAvailable() && !AppOpenAdManager.isAppOpenAdShowing && !isInterstitialAdShowing && !isExitClickFlowActive && !RewardedAdManager.isRewardedAdShowing
            ) {
                runInterstitial(
                    canShowLoading,
                    activity,
                    interstitialAdShowListener,
                    screenName,
                    adId,
                    shouldPreloadNextAd,
                    showLoadingDialog,
                    hideLoadingDialog
                )
            } else {
                interstitialAdShowListener.onInterstitialAdNull()
            }
        }
    }

    fun loadInterstitialAd(
        activity: Activity,
        adId: String,
        screenName: String,
        isFromSplash: Boolean = false,
        interAdCalled: () -> Unit = {},
        callback: (Boolean) -> Unit = {}
    ) {
        if (!activity.canRequestAds()) {
            return
        }
        if (interstitialAd == null && !isAdLoading) {
            isAdLoading = true

            if (isFromSplash) {
                "Loading Splash Interstitial Ad with ID: $adId".debugRelease(TAG)
            } else {
                "Loading Inner Interstitial Ad with ID: $adId".debugRelease(TAG)
            }
            interAdCalled()


            InterstitialAd.load(
                AdRequest.Builder(adId).build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isAdLoading = false
                        isInterstitialAdFailed = false


                        if (isFromSplash) {
                            "Splash Interstitial AD Loaded".debugRelease(TAG)
                        } else {
                            "Inner Interstitial AD Loaded".debugRelease(TAG)
                        }

                        interstitialAdLoadListener?.onAdLoaded()
                        callback.invoke(true)
                        logAdLoaded(activity, "inter_ad")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                        isAdLoading = false

                        "Interstitial Ad Failed to load with error ${adError.message}".debugRelease(
                            TAG
                        )
                        interstitialAdLoadListener?.onAdLoadFailed()
                        isInterstitialAdFailed = true
                        callback.invoke(false)
                        logAdFailedToLoad(activity, "inter_ad")
                    }

                },
            )
        }
    }

    private fun runInterstitial(
        canShowLoading: Boolean = true,
        activity: Activity,
        interstitialAdShowListener: InterstitialAdShowListener,
        screenName: String,
        adId: String,
        shouldPreloadNextAd: Boolean,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {

        "show_from class value $canShowLoading".debugRelease("inter_ad_log")
        CoroutineScope(Dispatchers.Main).launch {


            if (interstitialAd != null) {

                isInterstitialAdShowing = true

                if (canShowLoading) {
                    "show_triger_from class".debugRelease("inter_ad_log")
                    showLoadingDialog()
                }

                delay(1200.milliseconds)

                if (!AppOpenAdManager.isAppOpenAdShowing &&
                    !isActivityPaused &&
                    !RewardedAdManager.isRewardedAdShowing &&
                    !isExitClickFlowActive
                ) {

                    "Showing Interstitial Ad ".debugRelease(TAG)

                    interstitialAd?.adEventCallback =
                        object : InterstitialAdEventCallback {
                            override fun onAdShowedFullScreenContent() {

                            }

                            override fun onAdDismissedFullScreenContent() {

                                CoroutineScope(Dispatchers.Main).launch {
                                    hideLoadingDialog()
                                    interstitialTimeElapsed = Calendar.getInstance().timeInMillis

                                    interstitialAdShowListener.onInterstitialAdDismissed()

                                    "Interstitial Ad Dismissed".debugRelease(TAG)
                                    isInterstitialAdShowing = false
                                    if (shouldPreloadNextAd) {
                                        cancelInnerPreload()
                                        scheduleInnerPreload(activity)
                                    }
                                }


                            }

                            override fun onAdFailedToShowFullScreenContent(
                                fullScreenContentError: FullScreenContentError
                            ) {

                                CoroutineScope(Dispatchers.Main).launch {
                                    hideLoadingDialog()

                                    isInterstitialAdShowing = false

                                    interstitialAdShowListener.onInterstitialAdNull()

                                    "Interstitial Ad Failed to SHOW full screen with error: ${fullScreenContentError.message} ,${fullScreenContentError.code}".debugRelease(
                                        TAG
                                    )
                                }


                            }

                            override fun onAdImpression() {
                                CoroutineScope(Dispatchers.Main).launch {

                                    hideLoadingDialog()

                                    interstitialAdShowListener.onInterstitialAdImpression()
                                    "Interstitial Ad Impression".debugRelease(TAG)
                                    interstitialAd = null
                                }

                            }

                            override fun onAdClicked() {
                                "Interstitial Ad clicked".debugRelease(TAG)
                                interstitialAdShowListener.onInterstitialAdClicked()
                            }

                            override fun onAdPaid(value: AdValue) {
                                super.onAdPaid(value)
                                setInterstitialOnPaidEventListener(
                                    activity,
                                    value,
                                    interstitialAd?.placementId?.toString().orEmpty()
                                )
                            }
                        }

                    if (!isActivityPaused) {
                        logAdImpressionOpportunity(activity, "inter_ad")
                        interstitialAd?.show(activity)
                    }

                } else {
                    hideLoadingDialog()
                    isInterstitialAdShowing = false
                    interstitialAdShowListener.onInterstitialAdNull()
                }

                hideLoadingDialog()


            } else {
                interstitialAdShowListener.onInterstitialAdNull()

                hideLoadingDialog()
                if (shouldPreloadNextAd) {
                    loadInnerInterstitial(activity)
                }
            }
        }

    }

    fun remainingCappingSeconds(): Int {
        if (interstitialTimeElapsed == 0L) {
            return 0
        }
        return (RemoteConstants.interAdCapping - interstitialTimeElapsed.timeDifference())
            .coerceAtLeast(0)
    }

    fun cancelInnerPreload() {
        innerPreloadRunnable?.let { innerPreloadHandler.removeCallbacks(it) }
        innerPreloadRunnable = null
        innerPreloadScheduled = false
    }

    fun loadInnerInterstitial(activity: Activity?) {
        val currentActivity = activity ?: return
        if (!currentActivity.canRequestAds() || !RemoteConstants.showInnerInterAd) {
            return
        }
        if (interstitialAd != null || isAdLoading) {
            "Inner interstitial call skipped – ad already loaded or loading".debugRelease(TAG)
            return
        }
        val adId = RemoteConstants.INNER_INTERSTITIAL_AD_UNIT_ID
        if (adId.isBlank()) {
            return
        }
        loadInterstitialAd(
            currentActivity,
            adId,
            Constants.currentScreenName.ifBlank { "Home" }
        )
    }

    fun scheduleInnerPreload(activity: Activity?) {
        val currentActivity = activity ?: return
        if (!currentActivity.canRequestAds() || !RemoteConstants.showInnerInterAd) {
            return
        }
        if (interstitialAd != null || isAdLoading) {
            "Inner interstitial already loaded/loading – skip schedule".debugRelease(TAG)
            return
        }
        if (innerPreloadScheduled) {
            "Inner interstitial preload already scheduled".debugRelease(TAG)
            return
        }

        val delayMs = (remainingCappingSeconds() - INNER_PRELOAD_BEFORE_CAPPING_SECONDS)
            .coerceAtLeast(0) * 1000L
        innerPreloadScheduled = true
        "Schedule inner interstitial in ${delayMs}ms (remaining capping=${remainingCappingSeconds()})".debugRelease(
            TAG
        )
        innerPreloadRunnable = Runnable {
            innerPreloadScheduled = false
            loadInnerInterstitial(currentActivity)
        }
        innerPreloadRunnable?.let { runnable ->

            innerPreloadHandler.postDelayed(runnable, delayMs)
        }
    }

    fun onHomeReached(
        activity: Activity?,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {
        val currentActivity = activity ?: return
        if (!currentActivity.canRequestAds()) {
            return
        }

        "onHomeReached: adNull=${interstitialAd == null}, failed=$isInterstitialAdFailed, remaining=${remainingCappingSeconds()}".debugRelease(
            TAG
        )

        loadExitInterstitial(currentActivity)

        if (isInterstitialAdFailed) {
            "Inner interstitial failed previously – retry on Home".debugRelease(TAG)
            cancelInnerPreload()
            loadInnerInterstitial(currentActivity)
            return
        }

        scheduleInnerPreload(currentActivity)
    }

    fun showInnerThen(
        activity: Activity?,
        screenName: String,
        onContinue: () -> Unit
    ) {
        val currentActivity = activity
        if (currentActivity == null || !currentActivity.isValidContext()) {
            onContinue()
            return
        }

        if (
            !currentActivity.canRequestAds() ||
            !RemoteConstants.showInnerInterAd
        ) {
            onContinue()
            return
        }

        if (AppOpenAdManager.isAppOpenAdShowing || RewardedAdManager.isRewardedAdShowing) {
            "showInnerThen: app open/rewarded showing – skip inter, continue".debugRelease(TAG)
            onContinue()
            return
        }

        if (!isInterstitialCappingComplete()) {
            "showInnerThen: capping not complete – continue without ad".debugRelease(TAG)
            onContinue()
            return
        }

        if (isInterstitialAdShowing || isClickFlowActive || isExitClickFlowActive) {
            "showInnerThen: already showing – ignore extra click".debugRelease(TAG)
            return
        }

        isClickFlowActive = true
        showAndLoadInterstitial(
            activity = currentActivity,
            adId = RemoteConstants.INNER_INTERSTITIAL_AD_UNIT_ID,
            canShowLoading = true,
            useCapping = true,
            shouldPreloadNextAd = true,
            onFromRemote = RemoteConstants.showInnerInterAd,
            screenName = screenName,
            interstitialAdShowListener = object : InterstitialAdShowListener {
                override fun onInterstitialAdDismissed() {
                    isClickFlowActive = false
                    onContinue()
                }

                override fun onInterstitialAdNull() {
                    isClickFlowActive = false
                    onContinue()
                }
            },
            showLoadingDialog = { _isLoadingDialogVisible.value = true },
            hideLoadingDialog = { _isLoadingDialogVisible.value = false }
        )
    }

    fun loadExitInterstitial(activity: Activity?) {
        val currentActivity = activity ?: return
        if (!currentActivity.canRequestAds() || !RemoteConstants.showExitInterAd) {
            return
        }
        if (exitInterstitialAd != null || isExitAdLoading) {
            "Exit interstitial call skipped – ad already loaded or loading".debugRelease(TAG)
            return
        }
        val adId = RemoteConstants.EXIT_INTERSTITIAL_AD_UNIT_ID
        if (adId.isBlank()) {
            return
        }

        isExitAdLoading = true
        "Loading Exit Interstitial Ad with ID: $adId".debugRelease(TAG)
        InterstitialAd.load(
            AdRequest.Builder(adId).build(),
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    exitInterstitialAd = ad
                    isExitAdLoading = false
                    "Exit Interstitial AD Loaded".debugRelease(TAG)
                    logAdLoaded(currentActivity, "inter_ad")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    exitInterstitialAd = null
                    isExitAdLoading = false
                    "Exit Interstitial Ad Failed to load: ${adError.message}".debugRelease(TAG)
                    logAdFailedToLoad(currentActivity, "inter_ad")
                }
            }
        )
    }

    fun showExitThen(
        activity: Activity?,
        onContinue: () -> Unit
    ) {
        val currentActivity = activity
        if (currentActivity == null || !currentActivity.isValidContext()) {
            onContinue()
            return
        }

        if (AppOpenAdManager.isAppOpenAdShowing ||
            RewardedAdManager.isRewardedAdShowing ||
            isInterstitialAdShowing ||
            isExitFlowActive ||
            isClickFlowActive ||
            isRuntimeAdLoading
        ) {
            "showExitThen: overlapping ad – ignore extra click".debugRelease(TAG)
            return
        }

        if (
            !currentActivity.canRequestAds() ||
            !RemoteConstants.showExitInterAd
        ) {
            isExitFlowActive = true
            onContinue()
            return
        }

        val ad = exitInterstitialAd
        if (ad == null) {
            "showExitThen: no exit ad – open sheet".debugRelease(TAG)
            isExitFlowActive = true
            onContinue()
            return
        }

        isExitFlowActive = true
        isInterstitialAdShowing = true
        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                CoroutineScope(Dispatchers.Main).launch {
                    isInterstitialAdShowing = false
                    exitInterstitialAd = null
                    "Exit Interstitial Dismissed – skip reload, open sheet".debugRelease(TAG)
                    onContinue()
                }
            }

            override fun onAdFailedToShowFullScreenContent(
                fullScreenContentError: FullScreenContentError
            ) {
                isInterstitialAdShowing = false
                exitInterstitialAd = null
                "Exit Interstitial Failed to SHOW: ${fullScreenContentError.message}".debugRelease(
                    TAG
                )
                onContinue()
            }

            override fun onAdImpression() {
                "Exit Interstitial Impression".debugRelease(TAG)
            }

            override fun onAdClicked() {
                "Exit Interstitial clicked".debugRelease(TAG)
            }

            override fun onAdPaid(value: AdValue) {
                setInterstitialOnPaidEventListener(
                    currentActivity,
                    value,
                    ad.placementId?.toString().orEmpty()
                )
            }
        }

        if (!isActivityPaused) {
            logAdImpressionOpportunity(currentActivity, "inter_ad")
            ad.show(currentActivity)
        } else {
            isInterstitialAdShowing = false
            isExitFlowActive = false
        }
    }


    interface InterstitialAdShowListener {

        fun onInterstitialAdImpression() {
        }

        fun onInterstitialAdDismissed() {
        }

        fun onInterstitialAdNull()

        fun onInterstitialAdClicked() {}
    }

    interface InterstitialAdLoadListener {
        fun onAdLoaded()
        fun onAdLoadFailed()
    }


}
