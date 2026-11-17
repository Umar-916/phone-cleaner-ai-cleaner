package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapClickable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette
import org.koin.compose.viewmodel.koinViewModel

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val Plum: Color @Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary

private class Feature(@StringRes val label: Int, @DrawableRes val icon: Int, val inBasic: Boolean)

private val features = listOf(
    Feature(R.string.pro_f_junk, R.drawable.pro_ic_junk, true),
    Feature(R.string.pro_f_files, R.drawable.pro_ic_files, false),
    Feature(R.string.pro_f_ai, R.drawable.pro_ic_ai, false),
    Feature(R.string.pro_f_noads, R.drawable.pro_ic_noads, false),
    Feature(R.string.pro_f_vip, R.drawable.pro_ic_vip, false)
)

@Composable
fun PremiumScreen(
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
    val price = billedPrice.ifBlank { "null" }.let { if (it == "null") "$4.99" else it }
    val trialOn = canShowTrial && isTrialEnabled

    PostAnalytic(AnalyticsEvents.PREMIUM_SCREEN_DISPLAY)

    LaunchedEffect(activity) { premiumViewModel.onScreenReady(activity) }

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

    BackHandler { if (isBackPressEnabled) closePremium(isRight = showCloseRight) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingPalette.top, OnboardingPalette.bottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 44.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.files_mascot),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .aspectRatio(340f / 310f)
                        
                )
                Text(
                    stringResource(R.string.pro_title),
                    textAlign = TextAlign.Center,
                    color = Plum,
                    style = TextStyle(fontSize = 28.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(16.dp))
                ComparisonTable()
                Spacer(Modifier.height(18.dp))
                Text(
                    if (trialOn) stringResource(R.string.pro_trial_line, price) else stringResource(R.string.pro_plain_line, price),
                    textAlign = TextAlign.Center,
                    color = Plum,
                    style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 6.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(OnboardingPalette.purple)
                        .tapClickable("close_premium_click") { clickGuard.run { premiumViewModel.onPurchaseClicked(activity) } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(if (trialOn) R.string.premium_start_trial else R.string.premium_continue_button),
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    stringResource(R.string.pro_renews),
                    modifier = Modifier.padding(top = 10.dp),
                    color = Plum,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 11.sp, fontFamily = medium)
                )
            }
        }

        if (showCloseLeft) CloseX(Modifier.align(Alignment.TopStart)) { closePremium(isRight = false) }
        if (showCloseRight) CloseX(Modifier.align(Alignment.TopEnd)) { closePremium(isRight = true) }
    }
}

@Composable
private fun CloseX(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.statusBarsPadding().padding(10.dp).size(40.dp).clip(CircleShape).tapClickable("close_x_click", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Close, null, tint = Plum, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ComparisonTable() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
    ) {
        Row(Modifier.matchParentSize().padding(horizontal = 14.dp)) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(Modifier.width(56.dp))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.pro_premium), modifier = Modifier.width(64.dp), textAlign = TextAlign.Center, color = Plum, style = TextStyle(fontSize = 13.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                Text(stringResource(R.string.pro_basic), modifier = Modifier.width(56.dp), textAlign = TextAlign.Center, color = Plum, style = TextStyle(fontSize = 13.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            }
            features.forEach { f ->
                Box(Modifier.fillMaxWidth().height(1.dp).background(OnboardingPalette.purple.copy(alpha = 0.12f)))
                Row(Modifier.fillMaxWidth().height(50.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(f.icon), null, modifier = Modifier.size(32.dp), contentScale = ContentScale.Fit)
                    Text(
                        stringResource(f.label),
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                        color = Plum,
                        style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                    Box(Modifier.width(64.dp), contentAlignment = Alignment.Center) { Tick() }
                    Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) { if (f.inBasic) Tick() else Dash() }
                }
            }
        }
    }
}

@Composable
private fun Tick() {
    Icon(Icons.Rounded.Check, null, tint = OnboardingPalette.purple, modifier = Modifier.size(24.dp))
}

@Composable
private fun Dash() {
    Box(Modifier.width(16.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFB9A6D6)))
}
