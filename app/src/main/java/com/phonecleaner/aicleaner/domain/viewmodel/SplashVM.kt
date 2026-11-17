package com.phonecleaner.aicleaner.domain.viewmodel

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.core.advertisement.AdLoadListener
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager.loadInterstitialAd
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.RectBannerAdManager
import com.phonecleaner.aicleaner.core.advertisement.initialization.AppInitializationListener
import com.phonecleaner.aicleaner.core.advertisement.initialization.InitializationHelper
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.core.remote.SplashDesign
import com.phonecleaner.aicleaner.core.remote.SplashDesignProvider
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.OnboardingFlowHelper
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

private const val OFFLINE_PREMIUM_NAVIGATION_DELAY_MS = 2000L

class SplashVM(
    private val app: Application,
    private val splashDesignProvider: SplashDesignProvider
) : ViewModel() {

    private val _progress = MutableStateFlow(0f)
    private val _isLoadingComplete = MutableStateFlow(false)
    val isLoadingComplete: StateFlow<Boolean> = _isLoadingComplete

    private val _isIndeterminateProgress = MutableStateFlow(true)
    private val _shouldExitApp = MutableStateFlow(false)
    val shouldExitApp: StateFlow<Boolean> = _shouldExitApp

    private val _forceUpdateEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceUpdateEvent: SharedFlow<Unit> = _forceUpdateEvent.asSharedFlow()

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val appDataStore = AppDataStore(app)

    private val _splashCount = MutableStateFlow(0)
    val splashCount: StateFlow<Int> = _splashCount

    private val _splashDesign = MutableStateFlow(splashDesignProvider.resolve())
    val splashDesign: StateFlow<SplashDesign> = _splashDesign

    private val _dayAnalyticsEvent = MutableStateFlow<String?>(null)
    val dayAnalyticsEvent: StateFlow<String?> = _dayAnalyticsEvent

    private var runnable: Runnable? = null
    private var offlinePremiumRunnable: Runnable? = null
    private var maxProgress = 1.0f
    private var jump = 0.1f
    private var tickDelay = 1000L
    private var ticks = 0

    private var isSplashActive = true
    private var hasStartedAppInit = false
    private var hasStartedProgress = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _splashCount.value = appDataStore.incrementCountForSplash()
        }
        viewModelScope.launch {
            appDataStore.dayCount { event ->
                _dayAnalyticsEvent.value = event
            }
        }
    }

    fun initializeAppAndSetupProgress(activity: Activity?) {
        val currentActivity = activity ?: return
        if (hasStartedAppInit) {
            return
        }
        hasStartedAppInit = true

        NativeAdManager.activity = currentActivity
        RectBannerAdManager.myActivity = currentActivity
        OnboardingFlowHelper.resetAll()
        Constants.showNotification = true
        InterstitialAdManager.cancelInnerPreload()

        InitializationHelper.setupInitializationListener(object : AppInitializationListener {
            override fun onInitializationCompleted() {
                if (!isSplashActive()) {
                    return
                }

                resolveSplashDesign()
                _isIndeterminateProgress.value = false
                _forceUpdateEvent.tryEmit(Unit)
                preloadLanguageAds(currentActivity)
                loadSplashInterstitial(currentActivity)
                startProgress()
            }

            override fun showIndeterminateProgress() {
                if (isSplashActive()) {
                    _isIndeterminateProgress.value = true
                }
            }

            override fun onRemoteConfigFetched() {
                resolveSplashDesign()
            }
        })

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                BillingUtilsIAP.initializeBilling(currentActivity)
            }
            if (!isSplashActive() || _shouldExitApp.value || _isLoadingComplete.value) {
                return@launch
            }
            if (isPremiumUser() && !currentActivity.isNetworkAvailable()) {
                scheduleOfflinePremiumNavigation(currentActivity)
            }
        }

        InitializationHelper.initializeApp(currentActivity)
    }

    private fun resolveSplashDesign() {
        _splashDesign.value = splashDesignProvider.resolve()
    }

    private fun scheduleOfflinePremiumNavigation(activity: Activity) {
        if (offlinePremiumRunnable != null || _isLoadingComplete.value) {
            return
        }
        offlinePremiumRunnable = Runnable {
            if (!isSplashActive() || _shouldExitApp.value || _isLoadingComplete.value) {
                return@Runnable
            }
            if (isPremiumUser() && !activity.isNetworkAvailable()) {
                runnable?.let { handler.removeCallbacks(it) }
                _isLoadingComplete.value = true
            }
        }
        offlinePremiumRunnable ?.let { runnable ->
            handler.postDelayed(runnable, OFFLINE_PREMIUM_NAVIGATION_DELAY_MS)
        }

    }

    fun exitApp(activity: Activity?) {
        val currentActivity = activity ?: return

        tryCatch {
            currentActivity.finishAffinity()
        }

        tryCatch {
            val activityManager =
                currentActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.appTasks?.forEach {
                it.finishAndRemoveTask()
            }
        }

        tryCatch {
            exitProcess(0)
        }
    }

    fun goToNextScreen(navigate: () -> Unit) {
        if (isSplashActive()) {
            navigate()
        }
    }

    private fun isSplashActive(): Boolean = isSplashActive

    private fun preloadLanguageAds(activity: Activity) {
        if (!activity.isNetworkAvailable() || isPremiumUser()) {
            return
        }

        Constants.myAppCoroutineScope.launch(Dispatchers.Main + Constants.commonCoroutineExceptionHandler) {
            ensureActive()

            val isLanguageScreenOn = OnboardingFlowHelper.isScreenActive(
                activity,
                OnboardingScreen.LANGUAGE
            ) && OnboardingFlowHelper.canPreloadOnboardingAdDueToPremiumPlacement(
                activity,
                OnboardingScreen.LANGUAGE
            )

            if (isLanguageScreenOn) {
                loadLanguageAds(activity)
            }
        }
    }

    private fun loadLanguageAds(activity: Activity) {
        if (!activity.canRequestAds()) {
            return
        }

        val languageScreenAdType = RemoteConstants.languageScreenAdType

        if (languageScreenAdType == 1 || languageScreenAdType == 2) {
            val nativeAdId = RemoteConstants.LANGUAGE_NATIVE_AD_UNIT_ID
            if (nativeAdId.isNotBlank()) {
                NativeAdManager.languageAdLoadListener = object : AdLoadListener {
                    override fun onLanguageNativeAdLoaded(nativeAd: NativeAd?) {
                        activity.postAnalytic("ob_language_native_loaded")
                    }

                    override fun onLanguageAdFailedToLoad(error: String) {
                        activity.postAnalytic("ob_language_native_failed")
                    }
                }
                activity.postAnalytic("ob_language_native_call")
                NativeAdManager.loadLanguageNativeAd(activity, nativeAdId)
            }
        }

        if (
            languageScreenAdType == 1 &&
            !RectBannerAdManager.rectLanguageBannerPreloadAttempted &&
            !OnboardingFlowHelper.isHomeReachedForCurrentRun()
        ) {
            val rectAdId = RemoteConstants.LANGUAGE_RECT_BANNER_AD_UNIT_ID
            if (rectAdId.isNotBlank()) {
                RectBannerAdManager.rectLanguageBannerPreloadAttempted = true
                activity.postAnalytic("banner_preload_language")
                RectBannerAdManager.loadMediumRectangleNewLanguageBannerAd(
                    context = activity,
                    adUnitId = rectAdId,
                    screenName = "PreloadScreen",
                    tag = "banner_preload_language"
                )
            }
        }
    }

    private fun loadSplashInterstitial(activity: Activity) {

        if (!activity.isNetworkAvailable() || isPremiumUser()) {
            return
        }

        Constants.myAppCoroutineScope.launch(Dispatchers.Main + Constants.commonCoroutineExceptionHandler) {
            ensureActive()

            val isSplashAdOn = OnboardingFlowHelper.isScreenActive(
                activity,
                OnboardingScreen.SPLASH_INTERSTITIAL
            ) && OnboardingFlowHelper.canPreloadOnboardingAdDueToPremiumPlacement(
                activity,
                OnboardingScreen.SPLASH_INTERSTITIAL
            )

            if (isSplashAdOn && activity.canRequestAds() && RemoteConstants.SPLASH_INTERSTITIAL_AD_UNIT_ID.isNotBlank()) {
                loadInterstitialAd(
                    activity,
                    RemoteConstants.SPLASH_INTERSTITIAL_AD_UNIT_ID,
                    "SplashScreen",
                    isFromSplash = true,
                    interAdCalled = {
                        activity.postAnalytic("splash_interstitial_call")
                    }
                ) { loaded ->
                    if (loaded) {
                        activity.postAnalytic("splash_interstitial_load")
                    }
                }
            }

        }
    }

    private fun startProgress() {
        if (hasStartedProgress || _isLoadingComplete.value) {
            return
        }
        hasStartedProgress = true
        configureProgress()
        startHandlerTimer()
    }

    private fun configureProgress() {
        val adEnabled = !isPremiumUser() && app.isNetworkAvailable()
        maxProgress = 1.0f
        val steps = if (adEnabled) 10 else 2
        jump = maxProgress / steps
        tickDelay = 1000L
        ticks = 0
    }

    private fun startHandlerTimer() {
        tryCatch {
            runnable = object : Runnable {
                override fun run() {
                    ticks++

                    val shouldSkip = isPremiumUser() ||
                            InterstitialAdManager.interstitialAd != null ||
                            InterstitialAdManager.isInterstitialAdFailed

                    _progress.value = if (shouldSkip) {
                        maxProgress
                    } else {
                        (ticks * jump).coerceAtMost(maxProgress)
                    }

                    if (_progress.value < maxProgress) {
                        handler.postDelayed(this, tickDelay)
                    } else {
                        offlinePremiumRunnable?.let { handler.removeCallbacks(it) }
                        _isLoadingComplete.value = true
                    }
                }
            }
            runnable?.let { handler.postDelayed(it, tickDelay) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isSplashActive = false
        InitializationHelper.setupInitializationListener(null)
        tryCatch {
            runnable?.let { handler.removeCallbacks(it) }
            offlinePremiumRunnable?.let { handler.removeCallbacks(it) }
        }
    }
}
