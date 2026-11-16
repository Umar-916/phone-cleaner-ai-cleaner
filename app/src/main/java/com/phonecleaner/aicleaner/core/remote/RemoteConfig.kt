package com.phonecleaner.aicleaner.core.remote

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.phonecleaner.aicleaner.BuildConfig
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.taichi.CurrencyConverter
import com.phonecleaner.aicleaner.core.advertisement.taichi.CurrencyRate
import com.phonecleaner.aicleaner.core.common.Constants.adsCountThreshold
import com.phonecleaner.aicleaner.core.common.Constants.currencyThreshold
import com.phonecleaner.aicleaner.core.common.Constants.currencyThreshold1
import com.phonecleaner.aicleaner.core.common.Constants.currencyThreshold2
import com.phonecleaner.aicleaner.core.common.Constants.currencyThreshold3
import com.phonecleaner.aicleaner.core.common.Constants.currencyThreshold4
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.remote.RemoteConstants.backPressOnPremiumScreen
import com.phonecleaner.aicleaner.core.remote.RemoteConstants.inner_premium_screen
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.OnboardingFlowRemoteValues

object RemoteConfig {

    fun fetchValues(onResponse: (Boolean) -> Unit) {

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3).build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config)
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val updated = task.result
                Log.d("REMOTE_TAG", "Config params updated: $updated")

                currencyThreshold = mFirebaseRemoteConfig.getDouble("currencyThreshold")
                currencyThreshold1 = mFirebaseRemoteConfig.getDouble("currencyThreshold1")
                currencyThreshold2 = mFirebaseRemoteConfig.getDouble("currencyThreshold2")
                currencyThreshold3 = mFirebaseRemoteConfig.getDouble("currencyThreshold3")
                currencyThreshold4 = mFirebaseRemoteConfig.getDouble("currencyThreshold4")
                adsCountThreshold =
                    mFirebaseRemoteConfig.getLong("adsCountThreshold").toInt()

                tryCatch {
                    val currencyRatesString = mFirebaseRemoteConfig.getString("currency_rates_json")
                    if (currencyRatesString.isNotBlank()) {
                        CurrencyConverter.currencyRatesJson =
                            Gson().fromJson(currencyRatesString, CurrencyRate::class.java)
                    }
                }

                RemoteConstants.splashScreenType = if (BuildConfig.DEBUG) 2 else
                    mFirebaseRemoteConfig.getLong("splashScreenType").toInt()

                inner_premium_screen = if (BuildConfig.DEBUG) 1 else mFirebaseRemoteConfig.getLong("inner_premium_screen").toInt()

                backPressOnPremiumScreen = mFirebaseRemoteConfig.getLong("backPressOnPremiumScreen").toInt()

                RemoteConstants.hotBadgeDisplayState =
                   if (BuildConfig.DEBUG) 1 else mFirebaseRemoteConfig.getLong("hotBadgeDisplayState").toInt()
                RemoteConstants.hotBadgeText =
                    mFirebaseRemoteConfig.getString("hotBadgeText").trim().ifBlank { "HOT" }

                OnboardingFlowRemoteValues.flowFirstRun =
                    mFirebaseRemoteConfig.getString("flowFirstRun")
                OnboardingFlowRemoteValues.flowSecondRunAndOnwards =
                    mFirebaseRemoteConfig.getString("flowSecondRunAndOnwards")

                RemoteConstants.showInnerInterAd =
                    mFirebaseRemoteConfig.getBoolean("show_inner_inter_ad")
                RemoteConstants.showExitInterAd =
                    mFirebaseRemoteConfig.getBoolean("show_exit_inter_ad")
                RemoteConstants.showAppOpenAd =
                    mFirebaseRemoteConfig.getBoolean("show_app_open_ad")
                RemoteConstants.innerBannerAdType =
                    mFirebaseRemoteConfig.getLong("inner_banner_ad_type").toInt()

                RemoteConstants.languageScreenAdType =
                    mFirebaseRemoteConfig.getLong("language_screen_ad_type").toInt()
                RemoteConstants.languageNativeAdSize =
                    mFirebaseRemoteConfig.getLong("language_native_ad_size").toInt()
                RemoteConstants.homeNativeAdSize =
                    mFirebaseRemoteConfig.getLong("home_native_ad_size").toInt()
                RemoteConstants.exitDialougeAd = if (BuildConfig.DEBUG) 2 else
                    mFirebaseRemoteConfig.getLong("exit_dialouge_ad").toInt()
                RemoteConstants.exitNativeAdSize =
                    mFirebaseRemoteConfig.getLong("exit_native_ad_size").toInt()

                RemoteConstants.nativeAdCtaColor =
                    mFirebaseRemoteConfig.getString("native_ad_cta_color")
                RemoteConstants.nativeAdBgColor =
                    mFirebaseRemoteConfig.getString("native_ad_bg_color")

                RemoteConstants.interAdCapping =
                    mFirebaseRemoteConfig.getLong("inter_ad_capping").toInt()

                if (!BuildConfig.DEBUG) {
                    RemoteConstants.SPLASH_INTERSTITIAL_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("splash_interstitial_ad_unit_id")
                    RemoteConstants.LANGUAGE_RECT_BANNER_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("language_rect_banner_ad_unit_id")
                    RemoteConstants.LANGUAGE_NATIVE_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("language_native_ad_unit_id")
                    RemoteConstants.INNER_BANNER_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("inner_banner_ad_unit_id")
                    RemoteConstants.INNER_NATIVE_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("inner_native_ad_unit_id")
                    RemoteConstants.REWARDED_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("rewarded_ad_unit_id")
                    RemoteConstants.INNER_INTERSTITIAL_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("inner_interstitial_ad_unit_id")
                    RemoteConstants.APP_OPEN_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("app_open_ad_unit_id")
                    RemoteConstants.EXIT_INTERSTITIAL_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("exit_interstitial_ad_unit_id")
                    RemoteConstants.EXIT_BANNER_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("exit_banner_ad_unit_id")
                    RemoteConstants.EXIT_NATIVE_AD_UNIT_ID =
                        mFirebaseRemoteConfig.getString("exit_native_ad_unit_id")
                }

                onResponse(true)

            } else {
                onResponse(false)
            }
        }.addOnFailureListener {
            onResponse(false)
        }.addOnFailureListener {
            onResponse(false)
        }

    }
}
