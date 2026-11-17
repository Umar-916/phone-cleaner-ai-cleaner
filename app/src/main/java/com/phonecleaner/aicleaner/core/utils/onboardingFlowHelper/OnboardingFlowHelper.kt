package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper

import android.app.Activity
import android.content.Context
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.isValidContext
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingFlowConfig
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingScreen
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.RemoteScreenConfig
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.normalizedPremiumScreenType
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.resolvePremiumDestination
import com.phonecleaner.aicleaner.presentation.navigation.AppDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object OnboardingFlowHelper {

    const val TAG = "OnboardingFlowHelper"
    private var currentIndex = -1
    private var splashInterstitialIndex: Int? = null
    var selectedPremiumScreenType: Int? = null
    private var currentOnboardingFlowConfig: OnboardingFlowConfig? = null

    private var onboardingPreferenceHelper: OnboardingPreferenceHelper? = null
    private var onboardingBackupFlow: OnboardingBackupFlow? = null
    private var previousDestinationId: NavKey? = null

    fun onboardingNavigation(
        context: Context,
        activity: Activity?,
        scope: CoroutineScope,
        backStack: NavBackStack<NavKey>,
        showLoadingDialog: () -> Unit = {},
        hideLoadingDialog: () -> Unit = {}
    ) {
        "onboardingNavigation: Start".debug(TAG)

        scope.launch(Dispatchers.Main) {
            incrementCurrentIndex(backStack.lastOrNull())
            initializeCurrentOnboardingFlowConfig(context)
            checkForInterstitialAd()

            showSplashInterstitialAd(
                activity = activity,
                showLoadingDialog = showLoadingDialog,
                hideLoadingDialog = hideLoadingDialog
            ) {
                scope.launch(Dispatchers.Main) {
                    val targetDestination = getTargetScreenDestination(backStack.lastOrNull())
                    targetDestination.debug(TAG, "targetDestination")
                    replaceTop(backStack, targetDestination)
                }
            }
        }
    }

    suspend fun canPreloadOnboardingAdDueToPremiumPlacement(
        context: Context,
        onboardingScreen: OnboardingScreen
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (RemoteConstants.backPressOnPremiumScreen != 0) {
                    true
                } else {
                    initializeCurrentOnboardingFlowConfig(context)

                    val premiumIndex = currentOnboardingFlowConfig?.screens?.indexOfFirst {
                        it.id.normalizedOnboardingId() == OnboardingScreen.PREMIUM.name
                    } ?: -1

                    if (premiumIndex == -1) {
                        true
                    } else {
                        val targetScreenIndex = currentOnboardingFlowConfig?.screens
                            ?.indexOfFirst {
                                it.id.normalizedOnboardingId() == onboardingScreen.name
                            } ?: -1

                        if (targetScreenIndex == -1) {
                            false
                        } else {
                            targetScreenIndex < premiumIndex
                        }
                    }
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun isScreenActive(context: Context, onboardingScreen: OnboardingScreen): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                initializeCurrentOnboardingFlowConfig(context)
                val result = currentOnboardingFlowConfig?.screens?.any {
                    it.id.normalizedOnboardingId() == onboardingScreen.name
                } ?: false
                result.debug(TAG, "isScreenActive $onboardingScreen")
                result
            } catch (_: Exception) {
                false
            }
        }
    }

    fun resetAll() {
        homeScreenReachedForCurrentSession.value = false
        currentIndex = -1
        splashInterstitialIndex = null
        onboardingBackupFlow?.clear()
        onboardingBackupFlow = null
        onboardingPreferenceHelper?.clear()
        onboardingPreferenceHelper = null
        currentOnboardingFlowConfig = null
        previousDestinationId = null
        selectedPremiumScreenType = null
    }

    fun onHomeScreenReached(context: Context) {

        if (!homeScreenReachedForCurrentSession.value) {

            homeScreenReachedForCurrentSession.value = true

            if (onboardingPreferenceHelper == null) {
                onboardingPreferenceHelper = OnboardingPreferenceHelper()
            }

            onboardingPreferenceHelper?.incrementRunCount(context)
            onboardingBackupFlow?.clear()
            onboardingBackupFlow = null
            onboardingPreferenceHelper?.clear()
            onboardingPreferenceHelper = null
            currentOnboardingFlowConfig = null
            previousDestinationId = null
            splashInterstitialIndex = null
            selectedPremiumScreenType = null
            OnboardingFlowRemoteValues.clear()
        }
    }

    fun isHomeReachedForCurrentRun(): Boolean = homeScreenReachedForCurrentSession.value

    private suspend fun getTargetScreenDestination(currentDestination: NavKey?): NavKey {
        return withContext(Dispatchers.IO) {
            var targetDestination: NavKey = AppDestination.Home

            while (true) {
                val screenConfig = currentOnboardingFlowConfig
                    ?.screens
                    ?.getOrNull(currentIndex)

                if (screenConfig == null) {
                    targetDestination = AppDestination.Home
                    break
                }

                when (OnboardingScreen.fromId(screenConfig.id)) {
                    null -> currentIndex++

                    OnboardingScreen.SPLASH_INTERSTITIAL -> {
                        splashInterstitialIndex = currentIndex
                        currentIndex++
                    }

                    OnboardingScreen.PREMIUM -> {
                        val destination = getPremiumDestination(screenConfig)
                        if (destination == null ||
                            currentDestination is AppDestination.PremiumScreen
                        ) {
                            currentIndex++
                        } else {
                            selectedPremiumScreenType =
                                normalizedPremiumScreenType(screenConfig.screenType)
                            targetDestination = destination
                            break
                        }
                    }

                    OnboardingScreen.ONBOARDING -> {
                        if (currentDestination is AppDestination.Onboarding) {
                            currentIndex++
                        } else {
                            targetDestination = AppDestination.Onboarding
                            break
                        }
                    }

                    OnboardingScreen.LANGUAGE -> {
                        if (currentDestination is AppDestination.AppLanguage) {
                            currentIndex++
                        } else {
                            targetDestination = AppDestination.AppLanguage(isFromSplash = true)
                            break
                        }
                    }
                }
            }

            targetDestination
        }
    }

    private fun incrementCurrentIndex(currentDestination: NavKey?) {
        if (previousDestinationId != currentDestination) {
            previousDestinationId = currentDestination
            currentIndex++
        }
    }

    private fun getPremiumDestination(screenConfig: RemoteScreenConfig): NavKey? {
        if (isPremiumUser()) {
            "getPremiumDestination: User is premium, skipping premium screen".debug(TAG)
            return null
        }
        val destination = resolvePremiumDestination(screenConfig.screenType)
        "getPremiumDestination: screenType=${screenConfig.screenType}, target destination=$destination".debug(
            TAG
        )
        return destination
    }

    private fun showSplashInterstitialAd(
        activity: Activity?,
        showLoadingDialog: () -> Unit,
        hideLoadingDialog: () -> Unit,
        callback: () -> Unit
    ) {

        val currentActivity = activity
        if (currentActivity == null || !currentActivity.isValidContext()) {
            callback()
            return
        }

        val onFromRemote =
            !isPremiumUser() &&
                    currentActivity.isNetworkAvailable() && (currentIndex >= (splashInterstitialIndex
                ?: Int.MAX_VALUE))

        InterstitialAdManager.showAndLoadInterstitial(
            activity = currentActivity,
            adId = RemoteConstants.SPLASH_INTERSTITIAL_AD_UNIT_ID,
            canShowLoading = true,
            useCapping = false,
            shouldPreloadNextAd = false,
            onFromRemote = onFromRemote,
            screenName = "Splash",
            interstitialAdShowListener = object : InterstitialAdManager.InterstitialAdShowListener {
                override fun onInterstitialAdImpression() {
                    "showSplashInterstitialAd: onInterstitialAdImpression".debug(TAG)
                }

                override fun onInterstitialAdDismissed() {
                    "showSplashInterstitialAd: onInterstitialAdDismissed".debug(TAG)
                    if (currentActivity.isValidContext()) {
                        callback()
                    }
                }

                override fun onInterstitialAdNull() {
                    "showSplashInterstitialAd: onInterstitialAdNull".debug(TAG)
                    if (currentActivity.isValidContext()) {
                        callback()
                    }
                }
            },
            showLoadingDialog = showLoadingDialog,
            hideLoadingDialog = hideLoadingDialog
        )
    }

    private suspend fun filterScreens(config: OnboardingFlowConfig?): OnboardingFlowConfig? =
        withContext(Dispatchers.IO) {
            config?.copy(
                screens = config.screens
                    .filter(RemoteScreenConfig::enabled)
                    .filter { OnboardingScreen.fromId(it.id) != null }
                    .filter { screenConfig ->
                        when (screenConfig.id.normalizedOnboardingId()) {
                            OnboardingScreen.PREMIUM.name -> getPremiumDestination(screenConfig) != null
                            else -> true
                        }
                    }
                    .distinctBy { OnboardingScreen.fromId(it.id) }
            )
        }

    private fun String?.normalizedOnboardingId(): String =
        this?.trim()?.uppercase().orEmpty()

    private suspend fun initializeCurrentOnboardingFlowConfig(context: Context) {
        withContext(Dispatchers.IO) {
            if (currentOnboardingFlowConfig == null) {
                if (onboardingPreferenceHelper == null) {
                    onboardingPreferenceHelper = OnboardingPreferenceHelper()
                }
                if (onboardingBackupFlow == null) {
                    onboardingBackupFlow = OnboardingBackupFlow()
                }

                val currentRunCount = onboardingPreferenceHelper?.getCurrentRunCount(context) ?: 1
                val selectedRemoteFlowJson =
                    OnboardingFlowRemoteValues.getSelectedFlowJson(currentRunCount)
                val backFlowConfiguration = onboardingBackupFlow?.getBackupFlow(currentRunCount)
                    ?: OnboardingBackupFlow.defaultBackUp

                currentOnboardingFlowConfig = filterScreens(
                    OnboardingConfigParser.parse(
                        json = selectedRemoteFlowJson,
                        fallback = backFlowConfiguration
                    )
                )
            }
        }
    }

    private suspend fun checkForInterstitialAd() {
        withContext(Dispatchers.IO) {
            if (splashInterstitialIndex == null) {
                val currentRemoteScreenConfig =
                    currentOnboardingFlowConfig?.screens?.getOrNull(currentIndex)
                if (currentRemoteScreenConfig?.id.normalizedOnboardingId() == OnboardingScreen.SPLASH_INTERSTITIAL.name) {
                    splashInterstitialIndex = currentIndex
                }
            }
        }
    }

    private fun replaceTop(backStack: NavBackStack<NavKey>, destination: NavKey) {
        if (backStack.lastOrNull() == destination) {
            return
        }
        if (backStack.isNotEmpty()) {
            backStack.removeLastOrNull()
        }
        backStack.add(destination)
    }
}
