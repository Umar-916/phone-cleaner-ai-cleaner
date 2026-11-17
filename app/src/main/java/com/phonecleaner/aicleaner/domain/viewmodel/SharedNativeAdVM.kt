package com.phonecleaner.aicleaner.domain.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.phonecleaner.aicleaner.core.advertisement.AdLoadListener
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.native_ad.NativeAdType
import com.phonecleaner.aicleaner.core.common.canRequestAds
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedNativeAdVM : ViewModel() {
    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd

    private val _isNativeLoaded = MutableStateFlow(false)
    val isNativeLoaded: StateFlow<Boolean> = _isNativeLoaded

    private val _showAd = MutableStateFlow(false)
    val showAd: StateFlow<Boolean> = _showAd

    private var waitingOnLanguageNative = false
    private var languageHandoffDone = false
    private var showHomeNativeWhenLoaded = true
    private var languageBridge: AdLoadListener? = null

    val nativeAdType: NativeAdType
        get() = when (RemoteConstants.homeNativeAdSize) {
            1 -> NativeAdType.A_ONE
            2 -> NativeAdType.C_ONE
            else -> NativeAdType.NONE
        }

    fun loadHomeNative(context: Context) {
        if (
            RemoteConstants.homeNativeAdSize == 0 ||
            nativeAdType == NativeAdType.NONE ||
            !context.canRequestAds()
        ) {
            hideAd()
            return
        }

        if (!languageHandoffDone) {
            val leftoverLanguage = NativeAdManager.getLanguageNativeAd()?.takeIf {
                !NativeAdManager.hasLanguageAdRecordedImpression()
            }
            if (NativeAdManager.isLanguageScreenNativeAdLoading() || leftoverLanguage != null) {
                holdForLanguageNative(context, leftoverLanguage)
                return
            }
            languageHandoffDone = true
        }

        val readyHomeNative = NativeAdManager.getInnerNativeAd()
        if (readyHomeNative != null) {
            populate(readyHomeNative)
            return
        }

        requestHomeNative(context, showWhenLoaded = true)
    }

    private fun holdForLanguageNative(context: Context, alreadyLoaded: NativeAd?) {
        waitingOnLanguageNative = true
        languageBridge = object : AdLoadListener {
            override fun onLanguageNativeAdLoaded(nativeAd: NativeAd?) {
                if (!waitingOnLanguageNative) return
                if (nativeAd == null) {
                    waitingOnLanguageNative = false
                    languageHandoffDone = true
                    requestHomeNative(context, showWhenLoaded = true)
                } else {
                    populate(nativeAd)
                }
            }

            override fun onLanguageAdFailedToLoad(error: String) {
                if (!waitingOnLanguageNative) return
                waitingOnLanguageNative = false
                languageHandoffDone = true
                requestHomeNative(context, showWhenLoaded = true)
            }

            override fun onLanguageAdImpression() {
                if (!waitingOnLanguageNative) return
                waitingOnLanguageNative = false
                languageHandoffDone = true
                requestHomeNative(context, showWhenLoaded = false)
            }
        }
        NativeAdManager.languageAdLoadListener = languageBridge

        if (alreadyLoaded != null) {
            populate(alreadyLoaded)
        } else {
            _showAd.value = true
            _isNativeLoaded.value = false
        }
    }

    private fun requestHomeNative(context: Context, showWhenLoaded: Boolean) {
        showHomeNativeWhenLoaded = showWhenLoaded
        NativeAdManager.innerAdLoadListener = object : AdLoadListener {
            override fun onGenericNativeAdLoaded(nativeAd: NativeAd?) {
                if (showHomeNativeWhenLoaded) {
                    populate(nativeAd)
                }
            }

            override fun onGenericAdFailedToLoad(error: String) {
                if (showHomeNativeWhenLoaded) {
                    hideAd()
                }
            }
        }

        val ready = NativeAdManager.getInnerNativeAd()
        if (ready != null) {
            if (showWhenLoaded) {
                populate(ready)
            }
            return
        }

        if (NativeAdManager.isInnerNativeAdLoading()) {
            return
        }

        val adUnitId = RemoteConstants.INNER_NATIVE_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            if (showWhenLoaded) hideAd()
            return
        }

        if (showWhenLoaded) {
            _showAd.value = true
            _isNativeLoaded.value = false
        }
        NativeAdManager.loadInnerNativeAd(context, adUnitId)
    }

    fun clearAd() = Unit

    fun hideAd() {
        _showAd.value = false
        _isNativeLoaded.value = false
        _nativeAd.value = null
    }

    private fun populate(nativeAd: NativeAd?) {
        if (nativeAd == null) {
            hideAd()
            return
        }
        _nativeAd.value = nativeAd
        _isNativeLoaded.value = true
        _showAd.value = true
    }

    override fun onCleared() {
        super.onCleared()
        NativeAdManager.innerAdLoadListener = null
        if (NativeAdManager.languageAdLoadListener === languageBridge) {
            NativeAdManager.languageAdLoadListener = null
        }
    }
}
