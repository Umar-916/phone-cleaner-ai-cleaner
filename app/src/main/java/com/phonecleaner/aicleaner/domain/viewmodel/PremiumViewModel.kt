package com.phonecleaner.aicleaner.domain.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.OnboardingFlowHelper
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.isTrialPremiumScreenType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PremiumViewModel(
    private val app: Application
) : ViewModel() {

    private val _isTrialEnabled = MutableStateFlow(true)
    val isTrialEnabled: StateFlow<Boolean> = _isTrialEnabled

    private val _canShowTrial = MutableStateFlow(false)
    val canShowTrial: StateFlow<Boolean> = _canShowTrial

    private val _weeklyPrice = MutableStateFlow("")
    val weeklyPrice: StateFlow<String> = _weeklyPrice

    private val _showCloseLeft = MutableStateFlow(false)
    val showCloseLeft: StateFlow<Boolean> = _showCloseLeft

    private val _showCloseRight = MutableStateFlow(false)
    val showCloseRight: StateFlow<Boolean> = _showCloseRight

    private val _isBackPressEnabled = MutableStateFlow(false)
    val isBackPressEnabled: StateFlow<Boolean> = _isBackPressEnabled

    private val _showNoInternet = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val showNoInternet: SharedFlow<Unit> = _showNoInternet.asSharedFlow()

    private var closeButtonJob: Job? = null

    init {
        setupCloseButton()
    }

    fun onScreenReady(activity: Activity?) {
        setupCloseButton()
        val trialAvailable = canShowTrial(activity)
        _canShowTrial.value = trialAvailable
        if (!trialAvailable) {
            _isTrialEnabled.value = false
        }
        refreshWeeklyPrice()
    }

    fun onTrialToggled() {
        val enabled = !_isTrialEnabled.value
        _isTrialEnabled.value = enabled
        app.postAnalytic(
            if (enabled) AnalyticsEvents.PREMIUM_SWITCH_ENABLED
            else AnalyticsEvents.PREMIUM_SWITCH_DISABLED
        )
        refreshWeeklyPrice()
    }

    private fun refreshWeeklyPrice() {
        val price = BillingUtilsIAP.getWeeklyPrice()
        _weeklyPrice.value = price.ifBlank { "null" }
    }

    fun onPurchaseClicked(activity: Activity?) {
        val currentActivity = activity ?: return
        app.postAnalytic(AnalyticsEvents.WEEKLY_SLIDER_PURCHASE_BTN)
        app.postAnalytic(AnalyticsEvents.PREMIUM_PURCHASE_BTN)

        if (!currentActivity.isNetworkAvailable()) {
            _showNoInternet.tryEmit(Unit)
            return
        }

        BillingUtilsIAP.isPurchaseFlowActive = true

        val started = if (_canShowTrial.value && _isTrialEnabled.value) {
            app.postAnalytic(AnalyticsEvents.PREMIUM_CONTINUE_FOR_FREE_CLICK)
            BillingUtilsIAP.subscribeWeeklyTrial(currentActivity)
        } else {
            app.postAnalytic(AnalyticsEvents.PREMIUM_CONTINUE_CLICK)
            BillingUtilsIAP.subscribeWeekly(currentActivity)
        }

        if (!started) {
            BillingUtilsIAP.isPurchaseFlowActive = false
        }
    }

    fun onCloseClicked(isRight: Boolean) {
        app.postAnalytic(AnalyticsEvents.PREMIUM_CLOSE_CLICK)
        app.postAnalytic(AnalyticsEvents.WEEK_SLIDER_CLOSE)
        app.postAnalytic(
            if (isRight) AnalyticsEvents.WEEK_SLIDER_CLOSE_RIGHT
            else AnalyticsEvents.WEEK_SLIDER_CLOSE_LEFT
        )
    }

    fun onPurchased() {
        BillingUtilsIAP.isPurchaseFlowActive = false
    }

    private fun canShowTrial(activity: Activity?): Boolean {
        val selectedType = if (homeScreenReachedForCurrentSession.value) {
            RemoteConstants.inner_premium_screen
        } else {
            OnboardingFlowHelper.selectedPremiumScreenType
        }
        val isTrialActiveFromRemote = isTrialPremiumScreenType(selectedType)
        return isTrialActiveFromRemote && BillingUtilsIAP.isWeeklyTrialActive(activity)
    }

    private fun setupCloseButton() {
        if (homeScreenReachedForCurrentSession.value) {
            _isBackPressEnabled.value = true
            _showCloseLeft.value = true
            _showCloseRight.value = false
            return
        }

        _showCloseLeft.value = false
        _showCloseRight.value = false
        _isBackPressEnabled.value = false

        closeButtonJob?.cancel()
        closeButtonJob = viewModelScope.launch {
            when (RemoteConstants.backPressOnPremiumScreen) {
                0 -> {
                    app.postAnalytic(AnalyticsEvents.PREM_SLIDER_HARD_PAYWALL)
                    _isBackPressEnabled.value = false
                    _showCloseLeft.value = false
                    _showCloseRight.value = false
                }

                1 -> showRightClose()

                2 -> {
                    delay(3000L.milliseconds)
                    showRightClose()
                }

                3 -> {
                    delay(3000L.milliseconds)
                    showLeftClose()
                }

                else -> {
                    delay(3000L.milliseconds)
                    showLeftClose()
                }
            }
        }
    }

    private fun showRightClose() {
        _isBackPressEnabled.value = true
        _showCloseLeft.value = false
        _showCloseRight.value = true
    }

    private fun showLeftClose() {
        _isBackPressEnabled.value = true
        _showCloseLeft.value = true
        _showCloseRight.value = false
    }
}
