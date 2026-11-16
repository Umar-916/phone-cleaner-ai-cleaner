package com.phonecleaner.aicleaner.core.advertisement

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.phonecleaner.aicleaner.core.advertisement.AppOpenAdManager.Companion.isAppOpenAdShowing
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager.isInterstitialAdShowing
import com.phonecleaner.aicleaner.core.advertisement.taichi.setRewardedOnPaidEventListener
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object RewardedAdManager {

    private const val TAG = "rewarded_ad_log"

    var rewardedAd: RewardedAd? = null

    var isAdLoading = false

    var isRewardedAdShowing = false

    var rewardedAdLoadListener: RewardedAdLoadListener? = null

    fun loadAndShowRewardedAd(
        activity: Activity,
        rewardedAdId: String,
        interstitialAdId: String,
        canShowLoading: Boolean = true,
        onFromRemote: Boolean,
        screenName: String,
        shouldPreloadNextAd: Boolean = true,
        nextPreloadDelayDuration: Long? = null,
        rewardedAdShowListener: RewardedAdShowListener,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {
        if (!isPremiumUser() && onFromRemote && activity.isNetworkAvailable() &&
            !isAppOpenAdShowing && !isInterstitialAdShowing && !isRewardedAdShowing &&
            !InterstitialAdManager.isExitClickFlowActive &&
            !InterstitialAdManager.isInnerClickFlowActive
        ) {
            if (canShowLoading) {
                showLoadingDialog()
            }

            if (rewardedAd != null && !isAdLoading) {
                "loadAndShowRewardedAd: Ad already loaded, showing instantly".debugRelease(TAG)
                runRewardedAd(
                    canShowLoading = canShowLoading,
                    activity = activity,
                    rewardedAdShowListener = rewardedAdShowListener,
                    screenName = screenName,
                    adId = rewardedAdId,
                    shouldPreloadNextAd = shouldPreloadNextAd,
                    nextPreloadDelayDuration = nextPreloadDelayDuration,
                    showLoadingDialog = showLoadingDialog,
                    hideLoadingDialog = hideLoadingDialog
                )
            } else if (isAdLoading) {
                "loadAndShowRewardedAd: Ad is loading, waiting...".debugRelease(TAG)
                val originalListener = rewardedAdLoadListener
                rewardedAdLoadListener = object : RewardedAdLoadListener {
                    override fun onAdLoaded() {
                        originalListener?.onAdLoaded()
                        "loadAndShowRewardedAd: Ad loaded, now showing".debugRelease(TAG)
                        runRewardedAd(
                            canShowLoading = false,
                            activity = activity,
                            rewardedAdShowListener = rewardedAdShowListener,
                            screenName = screenName,
                            adId = rewardedAdId,
                            shouldPreloadNextAd = shouldPreloadNextAd,
                            nextPreloadDelayDuration = nextPreloadDelayDuration,
                            showLoadingDialog = showLoadingDialog,
                            hideLoadingDialog = hideLoadingDialog
                        )
                        rewardedAdLoadListener = originalListener
                    }

                    override fun onAdLoadFailed() {
                        originalListener?.onAdLoadFailed()
                        "loadAndShowRewardedAd: Ad load failed, showing interstitial as fallback".debugRelease(
                            TAG
                        )
                        hideLoadingDialog()
                        showInterstitialFallback(
                            activity = activity,
                            interstitialAdId = interstitialAdId,
                            screenName = screenName,
                            rewardedAdShowListener = rewardedAdShowListener,
                            showLoadingDialog = showLoadingDialog,
                            hideLoadingDialog = hideLoadingDialog
                        )
                        rewardedAdLoadListener = originalListener
                    }
                }
            }
            // Case 3: Ad is not loaded - load it first
            else {
                "loadAndShowRewardedAd: Ad not loaded, loading now...".debugRelease(TAG)
                isAdLoading = true

                RewardedAd.load(
                    AdRequest.Builder(rewardedAdId).build(),
                    object : AdLoadCallback<RewardedAd> {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            isAdLoading = false

                            "loadAndShowRewardedAd: Ad loaded successfully, showing now".debugRelease(
                                TAG
                            )
                            rewardedAdLoadListener?.onAdLoaded()

                            runRewardedAd(
                                canShowLoading = false,
                                activity = activity,
                                rewardedAdShowListener = rewardedAdShowListener,
                                screenName = screenName,
                                adId = rewardedAdId,
                                shouldPreloadNextAd = shouldPreloadNextAd,
                                nextPreloadDelayDuration = nextPreloadDelayDuration,
                                showLoadingDialog = showLoadingDialog,
                                hideLoadingDialog = hideLoadingDialog
                            )
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            rewardedAd = null
                            isAdLoading = false

                            "loadAndShowRewardedAd: Failed to load with error ${adError.message}, showing interstitial as fallback".debugRelease(
                                TAG
                            )
                            rewardedAdLoadListener?.onAdLoadFailed()

                            hideLoadingDialog()

                            showInterstitialFallback(
                                activity = activity,
                                interstitialAdId = interstitialAdId,
                                screenName = screenName,
                                rewardedAdShowListener = rewardedAdShowListener,
                                showLoadingDialog = showLoadingDialog,
                                hideLoadingDialog = hideLoadingDialog
                            )
                        }
                    }
                )
            }
        } else {
            "loadAndShowRewardedAd: Conditions not met".debugRelease(TAG)
            hideLoadingDialog()

            if (!isPremiumUser() && onFromRemote && activity.isNetworkAvailable() &&
                !isAppOpenAdShowing && !isInterstitialAdShowing
            ) {
                rewardedAdShowListener.onRewardedAdDismissed()
            } else {
                rewardedAdShowListener.onRewardedAdNull(false)
            }
        }
    }

    private fun showInterstitialFallback(
        activity: Activity,
        interstitialAdId: String,
        screenName: String,
        rewardedAdShowListener: RewardedAdShowListener,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {
        "showInterstitialFallback: Attempting to show interstitial ad".debugRelease(TAG)

        InterstitialAdManager.showAndLoadInterstitial(
            activity = activity,
            adId = interstitialAdId,
            canShowLoading = true,
            useCapping = false, // Don't use capping for fallback
            shouldPreloadNextAd = true,
            onFromRemote = true,
            screenName = screenName,
            interstitialAdShowListener = object : InterstitialAdManager.InterstitialAdShowListener {
                override fun onInterstitialAdImpression() {
                    "showInterstitialFallback: Interstitial ad shown as fallback".debugRelease(TAG)
                    rewardedAdShowListener.onRewardedAdFailedWithInterstitialShown()
                }

                override fun onInterstitialAdDismissed() {
                    rewardedAdShowListener.onRewardedAdDismissed()
                }

                override fun onInterstitialAdNull() {
                    "showInterstitialFallback: Interstitial ad also failed".debugRelease(TAG)
                    rewardedAdShowListener.onRewardedAdNull(false)
                }

                override fun onInterstitialAdClicked() {
                    rewardedAdShowListener.onRewardedAdClicked()
                }
            },
            showLoadingDialog = showLoadingDialog,
            hideLoadingDialog = hideLoadingDialog
        )
    }

    fun showAndLoadRewardedAd(
        activity: Activity,
        adId: String,
        canShowLoading: Boolean = true,
        onFromRemote: Boolean,
        screenName: String,
        shouldPreloadNextAd: Boolean,
        nextPreloadDelayDuration: Long?,
        rewardedAdShowListener: RewardedAdShowListener,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {
        if (!isPremiumUser() && onFromRemote && activity.isNetworkAvailable() &&
            !isAppOpenAdShowing && !isInterstitialAdShowing && !isRewardedAdShowing &&
            !InterstitialAdManager.isExitClickFlowActive &&
            !InterstitialAdManager.isInnerClickFlowActive
        ) {
            "showRewardedAd: Show ".debugRelease(TAG)
            runRewardedAd(
                canShowLoading = canShowLoading,
                activity = activity,
                rewardedAdShowListener = rewardedAdShowListener,
                screenName = screenName,
                adId = adId,
                shouldPreloadNextAd = shouldPreloadNextAd,
                nextPreloadDelayDuration = nextPreloadDelayDuration,
                showLoadingDialog = showLoadingDialog,
                hideLoadingDialog = hideLoadingDialog
            )
        } else {
            "showRewardedAd: else ".debugRelease(TAG)

            if (!isPremiumUser() && onFromRemote && activity.isNetworkAvailable() && !isAppOpenAdShowing && !isInterstitialAdShowing) {
                rewardedAdShowListener.onRewardedAdDismissed()
            } else {
                rewardedAdShowListener.onRewardedAdNull(false)
            }
        }

    }

    fun loadRewardedAd(
        activity: Activity,
        adId: String,
        rewardedAdCalled: () -> Unit = {},
        callback: (Boolean) -> Unit = {}
    ) {
        if (rewardedAd == null && !isAdLoading && activity.canRequestAds()) {
            isAdLoading = true

            "loadRewardedAd: Loading Rewarded Ad with ID: $adId".debugRelease(TAG)
            rewardedAdCalled()


            RewardedAd.load(
                AdRequest.Builder(adId).build(),
                object : AdLoadCallback<RewardedAd> {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isAdLoading = false
                        "onAdLoaded: Rewarded AD Loaded".debugRelease(TAG)
                        rewardedAdLoadListener?.onAdLoaded()
                        callback.invoke(true)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                        isAdLoading = false
                        "onAdFailedToLoad: Rewarded Ad Failed to load with error ${adError.message}".debugRelease(
                            TAG
                        )
                        rewardedAdLoadListener?.onAdLoadFailed()

                        callback.invoke(false)
                    }
                },
            )

        }
    }

    private fun runRewardedAd(
        canShowLoading: Boolean,
        activity: Activity,
        rewardedAdShowListener: RewardedAdShowListener,
        screenName: String,
        adId: String,
        shouldPreloadNextAd: Boolean,
        nextPreloadDelayDuration: Long?,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            if (rewardedAd == null) {
                rewardedAdShowListener.onRewardedAdNull(true)
                hideLoadingDialog()
                "Unable to show Rewarded AD : Ad is null".debugRelease(TAG)
                return@launch
            }

            isRewardedAdShowing = true

            if (canShowLoading) {
                showLoadingDialog()
                delay(1000.milliseconds)
            }

            if (!isAppOpenAdShowing && !isInterstitialAdShowing &&
                !InterstitialAdManager.isExitClickFlowActive
            ) {

                "Showing Rewarded Ad with ID: ${rewardedAd?.placementId}".debugRelease(TAG)
                // Listen for ad events.
                rewardedAd?.adEventCallback =
                    object : RewardedAdEventCallback {
                        override fun onAdShowedFullScreenContent() {
                            " Rewarded Ad is shown".debugRelease(TAG)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            CoroutineScope(Dispatchers.Main).launch {
                                hideLoadingDialog()
                                isRewardedAdShowing = false

                                if (shouldPreloadNextAd) {
                                    loadRewardedAd(activity, adId)
                                }
                                "onAdDismissedFullScreenContent: Rewarded Ad is Dismissed".debugRelease(
                                    TAG
                                )
                                rewardedAdShowListener.onRewardedAdDismissed()
                            }

                        }

                        override fun onAdFailedToShowFullScreenContent(
                            fullScreenContentError: FullScreenContentError
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                hideLoadingDialog()

                                isRewardedAdShowing = false

                                rewardedAdShowListener.onRewardedAdNull(false)
                                "onAdFailedToShowFullScreenContent: Rewarded Ad Failed to SHOW with error: ${fullScreenContentError.message} ,${fullScreenContentError.code}".debugRelease(
                                    TAG
                                )
                            }

                        }

                        override fun onAdImpression() {
                            CoroutineScope(Dispatchers.Main).launch {
                                rewardedAd = null
                                hideLoadingDialog()

                                rewardedAdShowListener.onRewardedAdImpression()
                                "onAdImpression: Rewarded Ad Impression Received".debugRelease(TAG)
                            }

                        }

                        override fun onAdClicked() {
                            "onAdClicked: Rewarded Ad clicked".debugRelease(TAG)
                            rewardedAdShowListener.onRewardedAdClicked()
                        }

                        override fun onAdPaid(value: AdValue) {
                            super.onAdPaid(value)
                            CoroutineScope(Dispatchers.Main).launch {

                                setRewardedOnPaidEventListener(
                                    activity,
                                    value,
                                    rewardedAd?.placementId?.toString().orEmpty()
                                )
                            }
                        }
                    }
                // Show the ad.
                rewardedAd?.show(
                    activity,
                ) { reward ->
//                    addCoins(reward.amount)
//                    showToast("Reward earned: ${reward.amount} ${reward.type}")
//                    Log.d(Constant.TAG, "User earned a reward: ${reward.amount} ${reward.type}")
                }

            } else {
                isRewardedAdShowing = false
                rewardedAdShowListener.onRewardedAdNull(false)
            }
            hideLoadingDialog()
        }


    }

    interface RewardedAdShowListener {

        fun onRewardedAdImpression() {
            "onRewardedAdImpression: Rewarded Ad Impression received".debugRelease("AD_LISTENER_TAG")
        }

        fun onRewardedAdDismissed() {
            "onRewardedAdDismissed: Rewarded Ad Dismissed".debugRelease("AD_LISTENER_TAG")
        }

        fun onRewardedAdNull(loadInterAd: Boolean)

        fun onRewardedAdClicked() {}

        /**
         * Called when rewarded ad fails to load but interstitial ad is shown as fallback
         */
        fun onRewardedAdFailedWithInterstitialShown() {
            "onRewardedAdFailedWithInterstitialShown: Interstitial shown as fallback".debugRelease("AD_LISTENER_TAG")
        }

    }

    interface RewardedAdLoadListener {
        fun onAdLoaded()
        fun onAdLoadFailed()
    }

}