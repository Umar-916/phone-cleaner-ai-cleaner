package com.phonecleaner.aicleaner.core.remote

import com.phonecleaner.aicleaner.BuildConfig

object RemoteConstants {

    /** Show ads */
    var showInnerInterAd: Boolean = true
    var showExitInterAd: Boolean = false
    var showAppOpenAd: Boolean = true

    /**
     * Inner/main banner
     * 0 = off
     * 1 = adaptive
     * 2 = Collapsible (Default)
     */
    var innerBannerAdType: Int = 2

    /**
     * Language screen ad type
     * 0 = off
     * 1 = Rect Banner Backfill with Native with media (Default)
     * 2 = Native
     */
    var languageScreenAdType: Int = 1

    /**
     * Language native size (used on Language screen, including Settings on-demand)
     * 1 = without media
     * 2 = with media (Default)
     */
    var languageNativeAdSize: Int = 2

    /**
     * Home native size
     * 0 = off
     * 1 = without media
     * 2 = with media (Default)
     */
    var homeNativeAdSize: Int = 1

    /**
     * Exit dialog ad
     * 0 = off
     * 1 = Rect Banner Backfill with Native with Media (Default)
     * 2 = Native
     */
    var exitDialougeAd: Int = 2

    /**
     * Exit native size
     * 1 = without media
     * 2 = with media (Default)
     * else = without media
     */
    var exitNativeAdSize: Int = 2

    var nativeAdCtaColor: String = "#0265DC"
    var nativeAdBgColor: String = "#FFFFFF"

    var interAdCapping = 30

    var inner_premium_screen = 7

    /**
     * Splash screen design
     * 1 = classic splash (Default)
     * 2 = new splash (Prank Hub artwork with animated logo for hallo ween)
     */
    var splashScreenType = 1

    /**
     * HOT badge on the home category cards
     * 0 = off (Default)
     * 1 = badge on the first card only
     */

    var hotBadgeDisplayState = 1

    /** Label shown on the HOT badge; the remote value replaces it when not blank. */

    var hotBadgeText = "HOT"

    // 0 = hard paywall (no close / no back; ads after premium are not preloaded)
    // 1 = right close immediately
    // 2 = right close after 3s
    // 3 / else = left close after 3s
    var backPressOnPremiumScreen = 1


    /** App Open — Throughout the App */
    var APP_OPEN_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/9257395921"

    /** Banner — Throughout the App */
    var INNER_BANNER_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/9214589741"

    /** Banner — Language Screen */
    var LANGUAGE_RECT_BANNER_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/6300978111"

    /** Banner — Exit Screen */
    var EXIT_BANNER_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/6300978111"

    /** Interstitial — Splash Screen */
    var SPLASH_INTERSTITIAL_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/1033173712"

    /** Interstitial — Inner Inter */
    var INNER_INTERSTITIAL_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/1033173712"

    /** Interstitial — Exit Inter */
    var EXIT_INTERSTITIAL_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/1033173712"

    /** Native — Language Screen */
    var LANGUAGE_NATIVE_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/2247696110"

    /** Native — Home Native */
    var INNER_NATIVE_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/2247696110"

    /** Native — Exit Screen */
    var EXIT_NATIVE_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/2247696110"

    /** Rewarded Ad — Use Feature */
    var REWARDED_AD_UNIT_ID: String =
        "ca-app-pub-3940256099942544/5224354917"
}
