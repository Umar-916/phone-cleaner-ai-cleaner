package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.domain.LanguageViewModel
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitleStyle
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageAdSlot
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageCard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageCatalog
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageDoneButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageSectionHeader
import com.phonecleaner.aicleaner.domain.viewmodel.LanguageAdsViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguageScreen(
    onBack: () -> Unit = {},
    onDone: () -> Unit = onBack,
    languageViewModel: LanguageViewModel = koinViewModel(),
    languageAdsViewModel: LanguageAdsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val clickGuard = rememberClickGuard()
    val showBackIcon by homeScreenReachedForCurrentSession.collectAsStateWithLifecycle()

    val pendingLanguageCode by languageViewModel.pendingLanguageCode.collectAsStateWithLifecycle()
    val savedLanguageCode by languageViewModel.savedLanguageCode.collectAsStateWithLifecycle()
    val languages = languageViewModel.languages
    val appliedLanguage = LanguageCatalog.findByCode(savedLanguageCode)
    val isAppliedSelected = pendingLanguageCode == savedLanguageCode
    val listLanguages = languages.filter { it.code != savedLanguageCode }

    val adSlot by languageAdsViewModel.adSlot.collectAsStateWithLifecycle()
    val nativeAd by languageAdsViewModel.nativeAd.collectAsStateWithLifecycle()
    val isNativeLoaded by languageAdsViewModel.isNativeLoaded.collectAsStateWithLifecycle()
    val bannerAd by languageAdsViewModel.bannerAd.collectAsStateWithLifecycle()
    val isBannerLoading by languageAdsViewModel.isBannerLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        context.postAnalytic(AnalyticsEvents.LANGUAGE_SCREEN_DISPLAY)
    }

    LaunchedEffect(activity) {
        languageAdsViewModel.setupAds(activity)
    }

    fun leaveWithSystemBack() {
        clickGuard.run {
            if (showBackIcon) {
                onBack()
            } else {
                context.postAnalytic(AnalyticsEvents.LANGUAGE_DONE_CLICK)
                languageViewModel.confirmSelection(activity ?: context) {
                    onDone()
                }
            }
        }
    }

    BackHandler {
        leaveWithSystemBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AppTopBar(
                title = {
                    AppTopBarTitle(
                        text = stringResource(R.string.app_language_new),
                        style = AppTopBarTitleStyle.Detail
                    )
                },
                onBack = if (showBackIcon) onBack else null,
                actions = {
                    LanguageDoneButton(
                        onClick = {
                            clickGuard.run {
                                context.postAnalytic(AnalyticsEvents.LANGUAGE_DONE_CLICK)
                                languageViewModel.confirmSelection(activity ?: context) {
                                    onDone()
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.sdp,
                    end = 16.sdp,
                    top = 10.sdp,
                    bottom = 16.sdp
                ),
                verticalArrangement = Arrangement.spacedBy(12.sdp)
            ) {
            item {
                LanguageSectionHeader(title = stringResource(R.string.selected_languages))
            }

            item(key = "selected_${appliedLanguage.code}") {
                LanguageCard(
                    item = appliedLanguage,
                    selected = isAppliedSelected,
                    highlighted = isAppliedSelected,
                    onClick = {
                        languageViewModel.selectLanguage(savedLanguageCode)
                    }
                )
            }

            item {
                Column {
                    Spacer(modifier = Modifier.height(12.sdp))
                    LanguageSectionHeader(title = stringResource(R.string.all_language))
                }
                Spacer(modifier = Modifier.height(2.sdp))
            }

            items(listLanguages, key = { it.code }) { language ->
                val isListSelected = language.code == pendingLanguageCode && !isAppliedSelected
                LanguageCard(
                    item = language,
                    selected = isListSelected,
                    highlighted = isListSelected,
                    onClick = {
                        context.postAnalytic("${AnalyticsEvents.LANGUAGE_SELECT_PREFIX}${language.code}")
                        languageViewModel.selectLanguage(language.code)
                    }
                )
            }
            }

            LanguageAdSlot(
                slot = adSlot,
                nativeAd = nativeAd,
                isNativeLoaded = isNativeLoaded,
                nativeAdType = languageAdsViewModel.nativeAdType,
                bannerAd = bannerAd,
                isBannerLoading = isBannerLoading,
                activity = activity,
                onHideNative = { languageAdsViewModel.hideNative() }
            )
        }
    }
}
