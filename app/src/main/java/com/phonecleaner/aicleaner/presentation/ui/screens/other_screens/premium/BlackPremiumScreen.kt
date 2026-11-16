package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.phonecleaner.aicleaner.core.common.HideSystemBars
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.BlackPremiumCloseButton
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.BlackPremiumFeatureRow
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.helpers.BlackPremiumGoProButton
import com.phonecleaner.aicleaner.presentation.ui.theme.blackPremiumGreen
import com.phonecleaner.aicleaner.presentation.ui.theme.blackPremiumOrange
import com.phonecleaner.aicleaner.presentation.ui.theme.blackPremiumPink
import com.phonecleaner.aicleaner.presentation.ui.theme.colorNine
import com.phonecleaner.aicleaner.presentation.ui.theme.colorPrimaryBoth
import com.phonecleaner.aicleaner.presentation.ui.theme.colorWhite
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BlackPremiumScreen(
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

    BackHandler {
        if (isBackPressEnabled) {
            closePremium(isRight = showCloseRight)
        }
    }

    HideSystemBars()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.black_premium_upgrade_to),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.sdp)
                            .padding(top = 78.sdp),
                        style = TextStyle(
                            fontSize = 24.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.Bold,
                            // "Make Every Prank" is white and "Your Own" orange in the reference design
                            color = colorWhite,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = stringResource(R.string.black_premium_prankhub_pro),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.sdp)
                            .padding(top = 6.sdp, bottom = 20.sdp),
                        style = TextStyle(
                            fontSize = 27.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.ExtraBold,
                            color = blackPremiumOrange,
                            textAlign = TextAlign.Center
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.sdp)
                            .padding(top = 60.sdp, bottom = 30.sdp),
                        verticalArrangement = Arrangement.spacedBy(32.sdp)
                    ) {
                        BlackPremiumFeatureRow(
                            iconRes = R.drawable.black_premium_unlock,
                            iconBackground = colorPrimaryBoth,
                            title = stringResource(R.string.black_premium_feature_sounds_title),
                            subtitle = stringResource(R.string.black_premium_feature_sounds_body)
                        )
                        BlackPremiumFeatureRow(
                            iconRes = R.drawable.black_premium_ad,
                            iconBackground = blackPremiumPink,
                            title = stringResource(R.string.black_premium_feature_pro_title),
                            subtitle = stringResource(R.string.black_premium_feature_pro_body)
                        )
                        BlackPremiumFeatureRow(
                            iconRes = R.drawable.black_premium_download,
                            iconBackground = blackPremiumGreen,
                            title = stringResource(R.string.black_premium_feature_vibration_title),
                            subtitle = stringResource(R.string.black_premium_feature_vibration_body)
                        )
                    }

                    Text(
                        text = stringResource(R.string.black_premium_unlock_cta),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 17.sdp)
                            .padding(top = 44.sdp, bottom = 0.sdp),
                        style = TextStyle(
                            fontSize = 18.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)),
                            fontWeight = FontWeight.Bold,
                            color = colorWhite,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.sdp)
                        .padding(vertical = 13.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.sdp)
                ) {
                    Text(
                        text = if (weeklyPrice == "null") "…" else stringResource(
                            if (trialOn) R.string.zippo_price_trial
                            else R.string.black_premium_price_week,
                            weeklyPrice
                        ),
                        maxLines = 2,
                        style = TextStyle(
                            fontSize = 13.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = colorNine,
                            textAlign = TextAlign.Center
                        )
                    )
                    BlackPremiumGoProButton(
                        isTrialEnabled = trialOn,
                        onClick = {
                            clickGuard.run {
                                premiumViewModel.onPurchaseClicked(activity)
                            }
                        }
                    )
                    Text(
                        text = stringResource(R.string.black_premium_cancel_playstore),
                        style = TextStyle(
                            fontSize = 11.ssp,
                            fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)),
                            fontWeight = FontWeight.Medium,
                            color = colorNine,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            if (showCloseLeft) {
                BlackPremiumCloseButton(
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(top = 5.sdp),
                    onClick = { closePremium(isRight = false) }
                )
            }

            if (showCloseRight) {
                BlackPremiumCloseButton(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 5.sdp),
                    onClick = { closePremium(isRight = true) }
                )
            }
        }
    }
}
