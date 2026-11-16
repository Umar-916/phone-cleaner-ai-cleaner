package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.isAppInDarkTheme
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.core.utils.ComponentHelper.debounceClickable
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumCloseButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumContinueButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumFeatureRow
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumNoPaymentFooter
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumOutlinedCard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumPlanCard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumTrialSwitch
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SliderPremiumScreen(
    onClose: () -> Unit,
    premiumViewModel: PremiumViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val clickGuard = rememberClickGuard()
    val isPurchased by BillingUtilsIAP.isPurchased.collectAsStateWithLifecycle()
    val isTrialEnabled by premiumViewModel.isTrialEnabled.collectAsStateWithLifecycle()
    val canShowTrial by premiumViewModel.canShowTrial.collectAsStateWithLifecycle()
    val billedPrice by premiumViewModel.weeklyPrice.collectAsStateWithLifecycle()
    val showCloseLeft by premiumViewModel.showCloseLeft.collectAsStateWithLifecycle()
    val showCloseRight by premiumViewModel.showCloseRight.collectAsStateWithLifecycle()
    val isBackPressEnabled by premiumViewModel.isBackPressEnabled.collectAsStateWithLifecycle()
    val weeklyPrice = billedPrice.ifBlank { "null" }
    val trialOn = canShowTrial && isTrialEnabled
    val isDarkTheme = isAppInDarkTheme()

    PostAnalytic(AnalyticsEvents.PREMIUM_SCREEN_DISPLAY)

    LaunchedEffect(activity) {
        premiumViewModel.onScreenReady(activity)
    }

    LaunchedEffect(isPurchased) {
        if (isPurchased || isPremiumUser()) {
            premiumViewModel.onPurchased()
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        premiumViewModel.showNoInternet.collect {
            context.showToast(context.getSafeString(R.string.internet_not_available))
        }
    }

    fun closePremium(isRight: Boolean) {
        clickGuard.run {
            premiumViewModel.onCloseClicked(isRight)
            onClose()
        }
    }

    BackHandler {
        if (isBackPressEnabled) {
            closePremium(isRight = showCloseRight)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.sdp)
                        .padding(top = 54.sdp, bottom = 16.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.sdp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(25.sdp)
                    ) {
                        Image(
                            painter = painterResource(
                                if (isDarkTheme) R.drawable.premium_hero_dark
                                else R.drawable.premium_hero
                            ),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 28.sdp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = stringResource(R.string.premium_unlock_all_features),
                            modifier = Modifier.padding(top = 4.sdp),
                            style = TextStyle(
                                fontSize = 26.ssp,
                                lineHeight = 35.ssp,
                                fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Text(
                        text = if (weeklyPrice == "null") "…" else stringResource(
                            if (trialOn) R.string.premium_opportunity_trial
                            else R.string.premium_opportunity_no_trial,
                            weeklyPrice
                        ),
                        modifier = Modifier.padding(top = 2.sdp),
                        style = TextStyle(
                            fontSize = 15.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.ssp
                        )
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.sdp)
                    ) {
                        Text(
                            text = stringResource(R.string.premium_whats_included),
                            style = TextStyle(
                                fontSize = 16.ssp,
                                fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.sdp)
                        ) {
                            PremiumOutlinedCard {
                                Column(
                                    modifier = Modifier.padding(top = 3.sdp, bottom = 3.sdp),
                                    verticalArrangement = Arrangement.spacedBy(20.sdp)
                                ) {
                                    PremiumFeatureRow(stringResource(R.string.premium_feature_unlock_all_prank_sounds))
                                    PremiumFeatureRow(stringResource(R.string.premium_feature_exclusive_pro_sounds))
                                    PremiumFeatureRow(stringResource(R.string.premium_feature_vibration_effects))
                                    PremiumFeatureRow(stringResource(R.string.premium_feature_timer_loop))
                                    PremiumFeatureRow(stringResource(R.string.premium_feature_ad_free_experience))
                                }
                            }

                            if (canShowTrial) {
                                PremiumOutlinedCard {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .debounceClickable {
                                                premiumViewModel.onTrialToggled()
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.premium_free_trial_enabled),
                                                style = TextStyle(
                                                    fontSize = 14.ssp,
                                                    fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                            Text(
                                                text = stringResource(R.string.premium_free_trial_subtitle),
                                                style = TextStyle(
                                                    fontSize = 10.ssp,
                                                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        }
                                        PremiumTrialSwitch(checked = isTrialEnabled)
                                    }
                                }
                            }

                            PremiumPlanCard(
                                isTrialEnabled = trialOn,
                                weeklyPrice = weeklyPrice
                            )
                        }
                    }

                    Text(
                        text = if (weeklyPrice == "null") "…" else stringResource(
                            if (trialOn) R.string.premium_trial_terms
                            else R.string.premium_payment_terms,
                            weeklyPrice
                        ),
                        style = TextStyle(
                            fontSize = 10.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.ssp
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.sdp)
                        .padding(top = 16.sdp, bottom = 20.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(13.sdp)
                ) {
                    PremiumContinueButton(
                        isTrialEnabled = trialOn,
                        onClick = {
                            clickGuard.run {
                                premiumViewModel.onPurchaseClicked(activity)
                            }
                        }
                    )
                    if (trialOn) {
                        PremiumNoPaymentFooter()
                    } else {
                        Text(
                            text = stringResource(R.string.premium_cancel_anytime),
                            style = TextStyle(
                                fontSize = 10.ssp,
                                fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }

            if (showCloseLeft) {
                PremiumCloseButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 4.sdp),
                    onClick = { closePremium(isRight = false) }
                )
            }

            if (showCloseRight) {
                PremiumCloseButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.sdp),
                    onClick = { closePremium(isRight = true) }
                )
            }
        }
    }
}
