package com.phonecleaner.aicleaner.core.common

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

object Constants {
    val commonCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        "Unhandled exception in coroutine $throwable".debug("CoroutineException")
    }
    // Basically used to avoid launching a new parent scope each time.
    val myAppCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + commonCoroutineExceptionHandler)

    /**
     * Master ad switch for this build. false: no ad SDK initialisation, consent form, ad request or ad UI anywhere
     * (banner, native, interstitial, app-open, rect banner, language and exit-dialog ads all go through
     * canRequestAds()). Set to true to turn every ad back on with the configured remote-config behaviour.
     */
    const val ADS_ENABLED = false

    /** in-App Purchases */
    const val WEEKLY_PRODUCT_ID = "premium"
    const val WEEKLY_SUBSCRIPTION = "weekly"
    const val WEEKLY_TRIAL_SUBSCRIPTION = "weekly-trial-3"
    const val USER_PREMIUM = "user_premium"
    const val PREF_KEY = "phone_cleaner_prefs"
    const val APP_FLYER_DEV_KEY = "APP_FLYER_DEV_KEY"

    var currentScreenName = ""

    var showNotification = true

    var currencyThreshold: Double = 0.1
    var currencyThreshold1: Double = 0.05
    var currencyThreshold2: Double = 0.07
    var currencyThreshold3: Double = 0.1
    var currencyThreshold4: Double = 0.12
    var adsCountThreshold: Int = 2

    const val SUPPORT_EMAIL = "support@example.com"

    const val TERMS_URL = "https://example.com/phone-cleaner-pro/terms"

    const val PRIVACY_POLICY_URL =
        "https://example.com/phone-cleaner-pro/privacy"

    var homeScreenReachedForCurrentSession = MutableStateFlow(false)
}