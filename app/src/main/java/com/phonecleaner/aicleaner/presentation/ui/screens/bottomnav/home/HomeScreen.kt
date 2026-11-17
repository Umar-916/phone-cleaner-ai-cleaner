package com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.text.format.Formatter
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.RectBannerAdManager
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.common.ExitAppDialog
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.utils.PostAnalytic
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.OnboardingFlowHelper
import com.phonecleaner.aicleaner.domain.HomeViewModel
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.navigation.getTotalBottomHeight
import com.phonecleaner.aicleaner.presentation.ui.components.HeroOrb
import com.phonecleaner.aicleaner.presentation.ui.components.PremiumTopBarAction
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel
import kotlin.system.exitProcess

private val boldFont = FontFamily(Font(R.font.google_sans_flex_bold))
private val semiFont = FontFamily(Font(R.font.google_sans_flex_semi))
private val mediumFont = FontFamily(Font(R.font.google_sans_flex_medium))

private val HeroBackdropHeight = 330.dp

private class FeatureTile(
    val label: Int,
    val hint: Int,
    val icon: ImageVector,
    val tint: Color,
    val analytics: String,
    val action: () -> Unit
)

/** The Cleaner tab: a compact hero for the junk scan and one soft-colored card per feature. */
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = koinViewModel(),
    showExitDialog: Boolean,
    changeExitDialogStatus: (Boolean) -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToJunk: () -> Unit = {},
    onNavigateToApps: () -> Unit = {},
    onNavigateToProcess: () -> Unit = {},
    onNavigateToDuplicates: () -> Unit = {},
    onNavigateToWhatsApp: () -> Unit = {},
    onNavigateToPhotoCompressor: () -> Unit = {},
    onNavigateToAiCleanup: () -> Unit = {},
    onNavigateToBattery: () -> Unit = {},
    onNavigateToDeviceInfo: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as ComponentActivity

    val storage by homeViewModel.storage.collectAsState()
    val homeLandCount by homeViewModel.homeLandCount.collectAsState()

    PostAnalytic(AnalyticsEvents.HOME_SCREEN_DISPLAY)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            context.postAnalytic("permission_notification_result_granted")
            homeViewModel.onPermissionGranted()
        } else {
            context.postAnalytic("permission_notification_result_denied")
            homeViewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                homeViewModel.onPermissionGranted()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            homeViewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(homeLandCount) {
        if (homeLandCount > 0) context.postAnalytic("home_land_$homeLandCount")
    }

    LaunchedEffect(Unit) {
        OnboardingFlowHelper.onHomeScreenReached(context)
        RectBannerAdManager.preloadExitBannerIfNeeded(context)
        NativeAdManager.preloadExitNativeIfNeeded(context)
        InterstitialAdManager.loadExitInterstitial(activity)
        homeViewModel.refreshStorage()
    }

    fun gated(tag: String, screen: String, action: () -> Unit): () -> Unit = {
        context.postAnalytic(tag)
        InterstitialAdManager.showInnerThen(activity, screen) { action() }
    }

    val tiles = listOf(
        FeatureTile(R.string.feature_process_manager, R.string.hint_process, Icons.Rounded.RocketLaunch, Brand.Process, "home_process_click", gated("home_process_click", "HomeProcess", onNavigateToProcess)),
        FeatureTile(R.string.feature_duplicates, R.string.hint_duplicates, Icons.Rounded.ContentCopy, Brand.Duplicates, "home_duplicates_click", gated("home_duplicates_click", "HomeDuplicates", onNavigateToDuplicates)),
        FeatureTile(R.string.feature_ai_cleanup, R.string.hint_ai, Icons.Rounded.AutoAwesome, Brand.Ai, "home_ai_click", gated("home_ai_click", "HomeAi", onNavigateToAiCleanup)),
        FeatureTile(R.string.feature_whatsapp, R.string.hint_whatsapp, Icons.Rounded.Forum, Brand.WhatsApp, "home_whatsapp_click", gated("home_whatsapp_click", "HomeWhatsApp", onNavigateToWhatsApp)),
        FeatureTile(R.string.feature_apps_manager, R.string.hint_apps, Icons.Rounded.Android, Brand.Apps, "home_apps_click", gated("home_apps_click", "HomeApps", onNavigateToApps)),
        FeatureTile(R.string.feature_photo_compressor, R.string.hint_photo, Icons.Rounded.PhotoSizeSelectLarge, Brand.Photo, "home_photo_click", gated("home_photo_click", "HomePhoto", onNavigateToPhotoCompressor)),
        FeatureTile(R.string.feature_battery, R.string.hint_battery, Icons.Rounded.BatteryChargingFull, Brand.Battery, "home_battery_click", gated("home_battery_click", "HomeBattery", onNavigateToBattery))
    )
    // Device info opens directly: no interstitial in front of it.
    val openDeviceInfo = { context.postAnalytic("home_device_info_click"); onNavigateToDeviceInfo() }
    val openJunk = gated("home_junk_click", "HomeJunk", onNavigateToJunk)
    val homeScroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(HeroBackdropHeight).background(Brush.verticalGradient(listOf(Color(0xFFC79BF4), Color(0xFFE6CFFB)))))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.app_name_short),
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF250B3B),
                            style = TextStyle(fontSize = 22.sp, fontFamily = boldFont, fontWeight = FontWeight.Bold)
                        )
                        if (!com.phonecleaner.aicleaner.core.common.isPremiumUser()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Brush.verticalGradient(listOf(Brand.Gold, Brand.GoldDeep)))
                                    .clickable { onNavigateToPremium() },
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.home_pro_crown),
                                    contentDescription = stringResource(R.string.prem_description),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFF8D3EDD), Color(0xFF6A1FC0))))
                                .clickable(onClick = openDeviceInfo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.SmartToy, contentDescription = stringResource(R.string.device_info_title), tint = Color.White, modifier = Modifier.size(22.dp))
                        }
            }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(homeScroll)) {
            run {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .graphicsLayer {
                            translationY = homeScroll.value * 0.5f
                            alpha = (1f - homeScroll.value / 380f).coerceIn(0f, 1f)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeroOrb(usedFraction = storage.usedFraction, onClean = openJunk)
                    Text(
                        stringResource(
                            R.string.home_storage_summary,
                            Formatter.formatFileSize(context, storage.freeBytes),
                            Formatter.formatFileSize(context, storage.totalBytes)
                        ),
                        color = Color(0xFF4A2A6F),
                        style = TextStyle(fontSize = 13.sp, fontFamily = mediumFont)
                    )
                }
            }

            run {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), clip = false, ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 16.dp, end = 16.dp, top = 22.dp)
                ) {
                    tiles.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { tile -> FeatureCard(tile, Modifier.weight(1f)) }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(getTotalBottomHeight() + getBannerOverlayHeight() + 8.dp))
                }
            }
        }
        }
    }

    if (showExitDialog) {
        ExitAppDialog(
            onDismiss = {
                changeExitDialogStatus(false)
                RectBannerAdManager.preloadExitBannerIfNeeded(context)
                NativeAdManager.preloadExitNativeIfNeeded(context)
                InterstitialAdManager.loadExitInterstitial(activity)
            },
            onExit = {
                changeExitDialogStatus(false)
                activity.finishAffinity()
                exitProcess(0)
            }
        )
    }
}

@Composable
private fun FeatureCard(tile: FeatureTile, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
                        .clickable(onClick = tile.action)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(58.dp).clip(CircleShape).background(tile.tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(tile.icon, null, tint = tile.tint, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(tile.label),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = TextStyle(fontSize = 14.sp, fontFamily = semiFont, fontWeight = FontWeight.SemiBold)
        )
    }
}
