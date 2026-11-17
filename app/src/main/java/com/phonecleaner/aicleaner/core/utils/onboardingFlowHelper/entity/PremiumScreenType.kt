package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity

import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.presentation.navigation.AppDestination

/**
 * Remote premium screen types:
 * 1, 2 = Slider (1 = with trial, 2 = without)
 * 3, 4 = Zippo (3 = with trial, 4 = without)
 * 5, 6 = Black (5 = without trial, 6 = with trial)
 * 7, 8 = Black design (7 = with trial, 8 = without trial)
 */
private const val MAX_PREMIUM_SCREEN_TYPE = 8
private val TRIAL_PREMIUM_SCREEN_TYPES = setOf(1, 3, 6, 7)

fun isTrialPremiumScreenType(screenType: Int?): Boolean = screenType in TRIAL_PREMIUM_SCREEN_TYPES

private fun premiumDestinationFor(screenType: Int, isFromSplash: Boolean): AppDestination =
    when (screenType) {
        1, 2 -> AppDestination.SliderPremiumScreen(isFromSplash = isFromSplash)
        3, 4 -> AppDestination.PremiumZippoScreen(isFromSplash = isFromSplash)
        5, 6 -> AppDestination.BlackPremiumScreen(isFromSplash = isFromSplash)
        7, 8 -> AppDestination.BlackPremiumScreen(isFromSplash = isFromSplash)
        else -> AppDestination.SliderPremiumScreen(isFromSplash = isFromSplash)
    }

fun resolvePremiumDestination(screenType: Int?): AppDestination? {
    if (screenType == null || screenType == 0) {
        return null
    }
    return premiumDestinationFor(screenType, isFromSplash = true)
}

fun normalizedPremiumScreenType(screenType: Int?): Int? {
    return when (screenType) {
        null, 0 -> null
        in 1..MAX_PREMIUM_SCREEN_TYPE -> screenType
        else -> 1
    }
}

fun getPremiumDestination(): AppDestination {
    if (RemoteConstants.inner_premium_screen !in 1..MAX_PREMIUM_SCREEN_TYPE) {
        RemoteConstants.inner_premium_screen = 1
    }

    return premiumDestinationFor(RemoteConstants.inner_premium_screen, isFromSplash = false)
}
