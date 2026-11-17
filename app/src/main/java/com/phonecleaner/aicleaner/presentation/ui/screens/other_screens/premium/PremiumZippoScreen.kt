package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium

import com.phonecleaner.aicleaner.core.common.tapEvent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.openWebLinkExternally
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.PremiumCloseButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.ZippoCtaButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.ZippoFeatureRow
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.ZippoLegalText
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PremiumZippoScreen(
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

    fun openPrivacyPolicy() {
        clickGuard.run {
            context.postAnalytic(AnalyticsEvents.PRIVACY_POLICY_CLICK)
            context.openWebLinkExternally(Constants.PRIVACY_POLICY_URL)
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
                        .padding(top = 60.sdp, bottom = 20.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(42.sdp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.sdp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.sdp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 16.sdp)
                                        .size(50.sdp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.zippo_crown),
                                        contentDescription = null,
                                        modifier = Modifier.size(width = 33.sdp, height = 44.sdp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.zippo_title),
                                    style = TextStyle(
                                        fontSize = 25.ssp,
                                        fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            Text(
                                text = stringResource(R.string.zippo_subtitle),
                                style = TextStyle(
                                    fontSize = 15.ssp,
                                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.surface,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.zippo_premium),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                style = TextStyle(
                                    fontSize = 11.ssp,
                                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.surface,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Box(modifier = Modifier.width(6.sdp))
                            Text(
                                text = stringResource(R.string.zippo_basic),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                style = TextStyle(
                                    fontSize = 11.ssp,
                                    fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.surface,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.sdp)
                            .padding(vertical = 12.sdp),
                        verticalArrangement = Arrangement.spacedBy(23.sdp)
                    ) {
                        ZippoFeatureRow(
                            iconRes = R.drawable.new_lock_icon,
                            title = stringResource(R.string.premium_feature_unlock_all_prank_sounds)
                        )
                        ZippoFeatureRow(
                            iconRes = R.drawable.new_ad_icon,
                            title = stringResource(R.string.premium_feature_exclusive_pro_sounds)
                        )
                        ZippoFeatureRow(
                            iconRes = R.drawable.new_play_sound_icon,
                            title = stringResource(R.string.premium_feature_vibration_effects)
                        )
                        ZippoFeatureRow(
                            iconRes = R.drawable.new_control_icon,
                            title = stringResource(R.string.premium_feature_timer_loop)
                        )
                        ZippoFeatureRow(
                            iconRes = R.drawable.zippo_sound,
                            title = stringResource(R.string.zippo_feature_ad_free)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.sdp)
                        .padding(bottom = 20.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.sdp)
                ) {
                    Text(
                        text = if (weeklyPrice == "null") "…" else stringResource(
                            if (trialOn) R.string.zippo_price_trial
                            else R.string.zippo_price_no_trial,
                            weeklyPrice
                        ),
                        maxLines = 1,
                        style = TextStyle(
                            fontSize = 11.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.Center
                        )
                    )
                    ZippoCtaButton(
                        isTrialEnabled = trialOn,
                        onClick = { tapEvent("open_privacy_policy_tap");
                            clickGuard.run {
                                premiumViewModel.onPurchaseClicked(activity)
                            }
                        }
                    )
                    ZippoLegalText(
                        isTrialEnabled = trialOn,
                        onPolicyClick = { openPrivacyPolicy() }
                    )
                }
            }

            if (showCloseLeft) {
                PremiumCloseButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 5.sdp),
                    onClick = { tapEvent("open_privacy_policy_tap_2"); closePremium(isRight = false) }
                )
            }

            if (showCloseRight) {
                PremiumCloseButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.sdp),
                    onClick = { tapEvent("open_privacy_policy_tap_3"); closePremium(isRight = true) }
                )
            }
        }
    }
}
