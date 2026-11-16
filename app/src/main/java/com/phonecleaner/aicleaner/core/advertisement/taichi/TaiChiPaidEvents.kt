package com.phonecleaner.aicleaner.core.advertisement.taichi

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appsflyer.share.MediationNetwork
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
import com.phonecleaner.aicleaner.core.advertisement.app_flyer.AppFlyer
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.tryCatch
import kotlin.math.floor

/*** Preference Key ***/

private const val taiChiRevenueCacheOnePreferenceKey = "taiChiRevenueCacheOnePreferenceKey"
private const val taiChiRevenueCacheTwoPreferenceKey = "taiChiRevenueCacheTwoPreferenceKey"
private const val taiChiAdCountPreferenceKey = "taiChiAdCountPreferenceKey"

private const val taiChiRevenueThresholdOnePreferenceKey = "taiChiRevenueThresholdOnePreferenceKey"
private const val taiChiRevenueThresholdTwoPreferenceKey = "taiChiRevenueThresholdTwoPreferenceKey"
private const val taiChiRevenueThresholdThreePreferenceKey =
    "taiChiRevenueThresholdThreePreferenceKey"
private const val taiChiRevenueThresholdFourPreferenceKey =
    "taiChiRevenueThresholdFourPreferenceKey"

/*** TaiChi ***/

fun setInterstitialOnPaidEventListener(
    context: Activity,
    adValue: AdValue,
    adId: String = context.packageName
) {
    handlePaidEvent(
        context = context,
        adValue = adValue,
        adType = "Interstitial",
        adId = adId,
        logTag = "InterstitialAd"
    )
}

fun setAppOpenOnPaidEventListener(
    context: Activity,
    adValue: AdValue,
    adId: String = context.packageName
) {
    handlePaidEvent(
        context = context,
        adValue = adValue,
        adType = "AppOpen",
        adId = adId,
        logTag = "AppOpenAd"
    )
}

fun setNativeOnPaidEventListener(
    context: Activity,
    adValue: AdValue,
    adId: String = context.packageName
) {
    handlePaidEvent(
        context = context,
        adValue = adValue,
        adType = "Native",
        adId = adId,
        logTag = "NativeAd"
    )
}

fun setBannerOnPaidEventListener(
    context: Activity,
    adValue: AdValue,
    adId: String = context.packageName
) {
    handlePaidEvent(
        context = context,
        adValue = adValue,
        adType = "Banner",
        adId = adId,
        logTag = "BannerAd"
    )
}

fun setRewardedOnPaidEventListener(
    context: Activity,
    adValue: AdValue,
    adId: String = context.packageName
) {
    handlePaidEvent(
        context = context,
        adValue = adValue,
        adType = "Rewarded",
        adId = adId,
        logTag = "RewardedAd"
    )
}

private fun handlePaidEvent(
    context: Activity,
    adValue: AdValue,
    adType: String,
    adId: String,
    logTag: String
) {
    tryCatch {
        val revenue = adValue.valueMicros / 1_000_000.0
        val currency = adValue.currencyCode

        Log.i(
            "OnPaidEventListenerTag",
            "event set $logTag revenue = $revenue || currency = $currency"
        )

        if (revenue > 0 && currency.isNotEmpty()) {
            Log.i("OnPaidEventListenerTag", "event set $logTag")
            context.taiChi(revenue, currency)
        }
    }

    tryCatch {
        val revenue = adValue.valueMicros / 1_000_000.0

        if (revenue > 0 && adValue.currencyCode.isNotEmpty()) {
            AppFlyer.logAdRevenue(
                adType = adType,
                currency = adValue.currencyCode,
                revenue = revenue,
                networkName = "AdMob",
                mediationNetwork = MediationNetwork.GOOGLE_ADMOB,
                adID = adId.ifEmpty { context.packageName }
            )
        }
    }
}

/**
 * Called on every ad paid event.
 *
 * Converts incoming revenue to USD, then runs two independent checks:
 *
 *   TaiChi 1.0 — fires "taichi_one" when cumulative USD revenue meets
 *                Constants.currencyThreshold. Resets revenue cache after firing.
 *
 *   TaiChi 2.0 — fires "taichi_two" when total ad impression count meets
 *                Constants.adsCountThreshold. Resets ad count after firing.
 *
 * Both checks run on every call — neither blocks the other.
 */

private fun Activity.postTaiChiThresholdEvent(
    amount: Double,
    preferenceKey: String,
    eventKey: String,
    thresholdValue: Double
) {

    tryCatch {

        if (thresholdValue > 0.0 && amount.isFinite() && thresholdValue.isFinite()) {

            var totalAmount =
                this.getFloatFromSharedPreferences(preferenceKey, 0f).toDouble()
                    .takeIf { it.isFinite() } ?: 0.0

            totalAmount += amount

            val remainder = totalAmount % thresholdValue

            val noOfTimeEventShouldTrigger =
                (floor(totalAmount / thresholdValue)).toInt().coerceIn(0, Int.MAX_VALUE)

            tryCatch {
                this.saveFloatToSharedPreferences(
                    preferenceKey,
                    remainder.toFloat().coerceAtLeast(0.0f)
                )
            }

            tryCatch {
                applicationContext?.let { appContext ->

                    val bundle = Bundle().apply {
                        putDouble(
                            FirebaseAnalytics.Param.VALUE,
                            thresholdValue
                        )
                        putString(
                            FirebaseAnalytics.Param.CURRENCY,
                            "USD"
                        )
                    }

                    repeat(noOfTimeEventShouldTrigger) {

                        postTaiChaiEvent(
                            eventKey,
                            bundle,
                            appContext
                        )
                    }
                }
            }
        }
    }
}

private val taiChiLock = Any()
fun Activity.taiChi(revenue: Double, currencyCode: String) {

    tryCatch {

        val appContext = applicationContext

        CurrencyConverter.convertToUSD(revenue, currencyCode) { amount ->

            if (!amount.isFinite() || amount <= 0.0) return@convertToUSD

            synchronized(taiChiLock) {

                tryCatch {

                    val updatedCache2 = this.getFloatFromSharedPreferences(
                        taiChiRevenueCacheTwoPreferenceKey,
                        0f
                    ) + amount

                    val updatedAdCount =
                        this.getIntFromSharedPreferences(taiChiAdCountPreferenceKey, 0) + 1

                    this.saveFloatToSharedPreferences(
                        taiChiRevenueCacheTwoPreferenceKey,
                        updatedCache2.toFloat()
                    )

                    // TaiChi 1.0 — revenue threshold

                    postTaiChiThresholdEvent(
                        amount = amount,
                        preferenceKey = taiChiRevenueCacheOnePreferenceKey,
                        eventKey = "taichi_one",
                        thresholdValue = Constants.currencyThreshold
                    )

                    postTaiChiThresholdEvent(
                        amount = amount,
                        preferenceKey = taiChiRevenueThresholdOnePreferenceKey,
                        eventKey = "total_ads_revenue_new_01",
                        thresholdValue = Constants.currencyThreshold1
                    )

                    postTaiChiThresholdEvent(
                        amount = amount,
                        preferenceKey = taiChiRevenueThresholdTwoPreferenceKey,
                        eventKey = "total_ads_revenue_new_02",
                        thresholdValue = Constants.currencyThreshold2
                    )

                    postTaiChiThresholdEvent(
                        amount = amount,
                        preferenceKey = taiChiRevenueThresholdThreePreferenceKey,
                        eventKey = "total_ads_revenue_new_03",
                        thresholdValue = Constants.currencyThreshold3
                    )

                    postTaiChiThresholdEvent(
                        amount = amount,
                        preferenceKey = taiChiRevenueThresholdFourPreferenceKey,
                        eventKey = "total_ads_revenue_new_04",
                        thresholdValue = Constants.currencyThreshold4
                    )

                    // TaiChi 2.0 — ad count threshold
                    if (updatedAdCount >= Constants.adsCountThreshold) {

                        val bundle = Bundle().apply {
                            putDouble(FirebaseAnalytics.Param.VALUE, updatedCache2)
                            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                        }

                        postTaiChaiEvent("taichi_two", bundle, appContext)

                        this.saveIntToSharedPreferences(taiChiAdCountPreferenceKey, 0)
                        this.saveFloatToSharedPreferences(taiChiRevenueCacheTwoPreferenceKey, 0.0f)
                    } else {
                        this.saveIntToSharedPreferences(taiChiAdCountPreferenceKey, updatedAdCount)
                    }
                }
            }
        }
    }
}


fun postTaiChaiEvent(key: String, bundle: Bundle, context: Context) {
    try {
        AnalyticsLogger.logEventWithBundle(context, key, bundle)
    } catch (_: Exception) {
    }
}
