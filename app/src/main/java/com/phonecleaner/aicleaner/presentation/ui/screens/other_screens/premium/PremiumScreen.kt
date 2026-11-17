package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium

import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.getSafeString
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.openWebLinkExternally
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.showToast
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.domain.viewmodel.PremiumViewModel
import com.phonecleaner.aicleaner.presentation.ui.components.AppSwitch
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val Coral = Color(0xFFF2605F)
private val TrialGreen = Color(0xFF3CCB7F)
private const val TRIAL_DAYS = 3

/**
 * The single premium screen: a storage-themed pitch, the free-trial timeline, the trial switch and the
 * price summary. Billing, trial and close behaviour all come from [PremiumViewModel], unchanged.
 */
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
    val price = billedPrice.ifBlank { "null" }.let { if (it == "null") "…" else it }
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

    val storage = remember {
        val stat = StatFs(Environment.getDataDirectory().path)
        stat.totalBytes - stat.availableBytes to stat.totalBytes
    }
    val activationDate = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, TRIAL_DAYS) }
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(cal.time as Date)
    }

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
                    .padding(horizontal = 20.dp)
                    .padding(top = 40.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.premium_title),
                    textAlign = TextAlign.Center,
                    color = OnboardingPalette.ink,
                    style = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.premium_subtitle),
                    textAlign = TextAlign.Center,
                    color = OnboardingPalette.muted,
                    style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = medium)
                )

                Image(
                    painter = painterResource(R.drawable.onboarding_junk),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(0.82f)
                        .aspectRatio(844f / 578f)
                        .featheredEdges(0.12f)
                )

                StorageUsage(used = storage.first, total = storage.second)

                Spacer(Modifier.height(16.dp))
                if (trialOn) {
                    TrialTimeline(activationDate)
                } else {
                    Features()
                }

                if (canShowTrial) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(OnboardingPalette.purple.copy(alpha = 0.16f), OnboardingPalette.purple.copy(alpha = 0.07f))))
                            .border(1.dp, OnboardingPalette.purple.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .clickable { premiumViewModel.onTrialToggled() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.premium_trial_enabled),
                            modifier = Modifier.weight(1f),
                            color = OnboardingPalette.ink,
                            style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                        )
                        AppSwitch(checked = isTrialEnabled, onCheckedChange = { premiumViewModel.onTrialToggled() })
                    }
                }

                Spacer(Modifier.height(16.dp))
                PriceSummary(trialOn = trialOn, price = price, activationDate = activationDate)
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple)))
                        .clickable { clickGuard.run { premiumViewModel.onPurchaseClicked(activity) } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(if (trialOn) R.string.premium_start_trial else R.string.premium_continue_button),
                        color = Color.White,
                        style = TextStyle(fontSize = 19.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FooterLink(stringResource(R.string.premium_terms)) { context.openWebLinkExternally(Constants.TERMS_URL) }
                    Text(
                        stringResource(R.string.premium_cancel_anytime),
                        color = OnboardingPalette.muted,
                        style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                    )
                    FooterLink(stringResource(R.string.premium_privacy)) {
                        context.postAnalytic(AnalyticsEvents.PRIVACY_POLICY_CLICK)
                        context.openWebLinkExternally(Constants.PRIVACY_POLICY_URL)
                    }
                }
            }
        }

        if (showCloseLeft) CloseButton(Modifier.align(Alignment.TopStart)) { closePremium(isRight = false) }
        if (showCloseRight) CloseButton(Modifier.align(Alignment.TopEnd)) { closePremium(isRight = true) }
    }
}

@Composable
private fun FooterLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        color = OnboardingPalette.purple,
        style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun CloseButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(10.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun StorageUsage(used: Long, total: Long) {
    val context = LocalContext.current
    val fraction = if (total <= 0) 0f else (used.toFloat() / total).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .background(Coral.copy(alpha = 0.18f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF8A7A), Coral)))
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(
                    R.string.premium_storage_of,
                    Formatter.formatFileSize(context, used),
                    Formatter.formatFileSize(context, total)
                ),
                color = OnboardingPalette.ink,
                style = TextStyle(fontSize = 17.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            Text(
                " " + stringResource(R.string.premium_storage_used),
                color = Coral,
                style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TrialTimeline(activationDate: String) {
    val steps = listOf(
        Triple(Icons.Rounded.Lock, R.string.premium_step_unlock to null, true),
        Triple(Icons.Rounded.Notifications, R.string.premium_step_reminder to R.string.premium_step_reminder_sub, false),
        Triple(Icons.Rounded.AutoAwesome, R.string.premium_step_start to R.string.premium_step_start_sub, false)
    )
    Column(Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, (icon, texts, highlighted) ->
            Row(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (highlighted) Color.White else OnboardingPalette.dot.copy(alpha = 0.7f))
                            .border(if (highlighted) 2.dp else 0.dp, OnboardingPalette.purple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = OnboardingPalette.purple, modifier = Modifier.size(20.dp))
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(26.dp)
                                .clip(CircleShape)
                                .background(OnboardingPalette.purple.copy(alpha = 0.55f))
                        )
                    }
                }
                Column(Modifier.padding(start = 6.dp, top = 2.dp)) {
                    Text(
                        stringResource(texts.first),
                        color = if (highlighted) OnboardingPalette.purple else OnboardingPalette.ink,
                        style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    texts.second?.let {
                        Text(
                            if (it == R.string.premium_step_start_sub) stringResource(it, activationDate) else stringResource(it),
                            color = OnboardingPalette.muted,
                            style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Features() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(OnboardingPalette.purple.copy(alpha = 0.16f), OnboardingPalette.purple.copy(alpha = 0.07f))))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            R.string.premium_feature_unlock_all_prank_sounds,
            R.string.premium_feature_exclusive_pro_sounds,
            R.string.premium_feature_vibration_effects,
            R.string.premium_feature_timer_loop,
            R.string.premium_feature_ad_free_experience
        ).forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(OnboardingPalette.purple), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(it),
                    color = OnboardingPalette.ink,
                    style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun PriceSummary(trialOn: Boolean, price: String, activationDate: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            if (trialOn) {
                Text(
                    stringResource(R.string.premium_due_today),
                    color = OnboardingPalette.ink,
                    style = TextStyle(fontSize = 14.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.premium_due_on, activationDate),
                    color = OnboardingPalette.muted,
                    style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                )
            } else {
                Text(
                    stringResource(R.string.premium_weekly_access),
                    color = OnboardingPalette.ink,
                    style = TextStyle(fontSize = 14.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (trialOn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.premium_days_free),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TrialGreen.copy(alpha = 0.25f)).padding(horizontal = 8.dp, vertical = 3.dp),
                        color = Color(0xFF138A52),
                        style = TextStyle(fontSize = 12.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.premium_free),
                        color = OnboardingPalette.ink,
                        style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(
                stringResource(R.string.premium_price_per_week_format, price),
                color = OnboardingPalette.ink,
                style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
        }
    }
}
