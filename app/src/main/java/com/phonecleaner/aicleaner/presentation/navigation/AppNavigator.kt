package com.phonecleaner.aicleaner.presentation.navigation

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.phonecleaner.aicleaner.BuildConfig
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.application.MyApplication
import com.phonecleaner.aicleaner.core.advertisement.AppOpenAdManager
import com.phonecleaner.aicleaner.core.advertisement.BannerAdCompose
import com.phonecleaner.aicleaner.core.advertisement.InterstitialAdManager
import com.phonecleaner.aicleaner.core.advertisement.LoadingDialog
import com.phonecleaner.aicleaner.core.advertisement.NativeAdManager
import com.phonecleaner.aicleaner.core.advertisement.initialization.InitializationHelper
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.common.HideSystemBars
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.remote.RemoteConstants
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.OnboardingFlowHelper
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.getPremiumDestination
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.domain.viewmodel.BannerAdViewModel
import com.phonecleaner.aicleaner.domain.viewmodel.SharedNativeAdVM
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.home.HomeScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.SettingScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.apps.AppsManagerScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.duplicates.DuplicatesScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.files.FileCategoryScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai.AiCleanupScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai.AiCategoryScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.completed.TaskCompletedScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.completed.OtherFeature
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery.BatteryOptimizeScreen
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.TaskTracker
import com.phonecleaner.aicleaner.domain.ExtraTask
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.junk.JunkCleanerScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.files.StorageBrowserScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.files.FilesScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.device.DeviceInfoScreen
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.photo.PhotoCompressorScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process.ProcessManagerScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.whatsapp.WhatsAppCleanerScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.AppLanguageScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.feedback.FeedbackScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.onboarding.OnboardingScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.PremiumScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.PremiumZippoScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.premium.SliderPremiumScreen
import com.phonecleaner.aicleaner.presentation.ui.screens.splash.StartScreen
import com.phonecleaner.aicleaner.presentation.ui.theme.onSecondaryFixedVariantLight
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigator(
    modifier: Modifier = Modifier
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showOnboardingAdLoading by remember { mutableStateOf(false) }
    val navGuard = rememberClickGuard()
    val backGuard = rememberClickGuard(350L)
    val context = LocalContext.current
    val activity = LocalActivity.current
    val onboardingScope = rememberCoroutineScope()
    val bannerAdViewModel: BannerAdViewModel = koinViewModel(
        viewModelStoreOwner = activity as ComponentActivity
    )
    val homeNativeVM: SharedNativeAdVM = koinViewModel(
        viewModelStoreOwner = activity
    )

    val innerBannerAd by bannerAdViewModel.bannerAd.collectAsStateWithLifecycle()
    val isBannerLoading by bannerAdViewModel.isLoading.collectAsStateWithLifecycle()
    val innerInterLoading by InterstitialAdManager.isLoadingDialogVisible.collectAsStateWithLifecycle()

    val shouldNotShowBannerAdTypes = setOf(
        AppDestination.Splash::class,
        AppDestination.SliderPremiumScreen::class,
        AppDestination.BlackPremiumScreen::class,
        AppDestination.PremiumZippoScreen::class,
        AppDestination.Onboarding::class,
        AppDestination.Feedback::class,
        AppDestination.AppLanguage::class
    )

    val backStack = rememberNavBackStack(AppDestination.Splash)

    LaunchedEffect(Unit) {
        if (backStack.lastOrNull() != AppDestination.Splash) {
            InitializationHelper.recoverIfSplashWasSkipped(activity) {
                (activity.application as? MyApplication)?.initializeAppOpen()
                if (backStack.lastOrNull() == AppDestination.Home) {
                    AppOpenAdManager.requestAdIfNeeded()
                    NativeAdManager.preloadExitNativeIfNeeded(context)
                    bannerAdViewModel.loadIfNeeded(activity)
                }
            }
        }
    }

    fun navigateTo(destination: NavKey) {
        if (backStack.lastOrNull() == destination) return
        if (destination == AppDestination.WhatsAppCleaner && !com.phonecleaner.aicleaner.data.storage.FolderAccess.isWhatsAppInstalled(context)) {
            android.widget.Toast.makeText(context, context.getString(com.phonecleaner.aicleaner.R.string.wa_missing_toast), android.widget.Toast.LENGTH_LONG).show()
            return
        }
        navGuard.run {
            tryCatch { backStack.add(destination) }
        }
    }

    fun switchTab(destination: AppDestination) {
        tryCatch {
            backStack.clear()
            backStack.add(AppDestination.Home)
            if (destination != AppDestination.Home) backStack.add(destination)
        }
    }

    fun navigateBack() {
        backGuard.run {
            tryCatch {
                when {
                    backStack.size > 1 -> backStack.removeLastOrNull()
                    backStack.lastOrNull().let { it != null && it != AppDestination.Home && it != AppDestination.Splash } -> {
                        backStack.clear()
                        backStack.add(AppDestination.Home)
                    }
                }
            }
        }
    }

    fun finishTask(summary: String) {
        tryCatch {
            val top = backStack.lastOrNull()
            val main = when (top) {
                is AppDestination.ProcessManager -> MainTask.PROCESS
                is AppDestination.BatteryOptimize -> MainTask.BATTERY
                else -> null
            }
            val extra = when (top) {
                is AppDestination.AiCleanup, is AppDestination.AiCategory -> ExtraTask.AI
                is AppDestination.WhatsAppCleaner -> ExtraTask.WHATSAPP
                is AppDestination.Duplicates -> ExtraTask.DUPLICATES
                else -> null
            }
            val all = TaskTracker.completesAll(main, extra)
            extra?.let { TaskTracker.markExtra(it) }
            if (all) {
                while (backStack.size > 1 && backStack.lastOrNull() != AppDestination.Home) backStack.removeLastOrNull()
                if (backStack.lastOrNull() != AppDestination.Home) { backStack.clear(); backStack.add(AppDestination.Home) }
                return@tryCatch
            }
            backStack.removeLastOrNull()
            if (backStack.lastOrNull() is AppDestination.TaskCompleted) backStack.removeLastOrNull()
            backStack.add(AppDestination.TaskCompleted(summary))
        }
    }

    fun replaceTop(destination: NavKey) {
        tryCatch {
            backStack.removeLastOrNull()
            backStack.add(destination)
        }
    }

    fun continueOnboardingOr(fallback: () -> Unit) {
        if (!homeScreenReachedForCurrentSession.value) {
            OnboardingFlowHelper.onboardingNavigation(
                context = context,
                activity = activity,
                scope = onboardingScope,
                backStack = backStack,
                showLoadingDialog = { showOnboardingAdLoading = true },
                hideLoadingDialog = { showOnboardingAdLoading = false }
            )
        } else {
            fallback()
        }
    }

    if (BuildConfig.DEBUG) {
        backStack.forEachIndexed { index, entry ->
            Log.d("MyAppTag", "AppNavigator: $index => $entry")
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            val currentDestination = backStack.lastOrNull()
            Constants.currentScreenName = currentDestination?.let { it::class.simpleName }.orEmpty()

            val shouldHideSystemBars =
                currentDestination is AppDestination.Splash
            if (shouldHideSystemBars) {
                HideSystemBars()
            }

            LaunchedEffect(currentDestination) {
                if (currentDestination == AppDestination.Home) {
                    OnboardingFlowHelper.onHomeScreenReached(context)
                    AppOpenAdManager.requestAdIfNeeded()
                    NativeAdManager.preloadExitNativeIfNeeded(context)
                    bannerAdViewModel.loadIfNeeded(activity)
                    InterstitialAdManager.onHomeReached(
                        activity = activity,
                        showLoadingDialog = { showOnboardingAdLoading = true },
                        hideLoadingDialog = { showOnboardingAdLoading = false }
                    )
                }
            }

            LaunchedEffect(showExitDialog) {
                InterstitialAdManager.setExitDialogVisible(showExitDialog)
            }

            BackHandler(enabled = currentDestination == AppDestination.Home) {
                InterstitialAdManager.showExitThen(activity) {
                    showExitDialog = true
                }
            }

            if (backStack.isNotEmpty()) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { navigateBack() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    modifier = Modifier
                        .padding(bottom = 0.sdp)
                        .background(Color.Transparent),
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    entryProvider = { route ->
                        when (route) {
                            is AppDestination.Splash -> NavEntry(route) {
                                StartScreen {
                                    continueOnboardingOr {
                                        replaceTop(AppDestination.Home)
                                    }
                                }
                            }

                            is AppDestination.Onboarding -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.theme.ForceLightTheme { OnboardingScreen(onFinished = { continueOnboardingOr { replaceTop(AppDestination.Home) } }) }
                            }

                            is AppDestination.SliderPremiumScreen -> NavEntry(route) {
                                SliderPremiumScreen(onClose = { continueOnboardingOr { navigateBack() } })
                            }

                            is AppDestination.BlackPremiumScreen -> NavEntry(route) {
                                PremiumScreen(onClose = { continueOnboardingOr { navigateBack() } })
                            }

                            is AppDestination.PremiumZippoScreen -> NavEntry(route) {
                                PremiumZippoScreen(onClose = { continueOnboardingOr { navigateBack() } })
                            }

                            is AppDestination.Home -> NavEntry(route) {
                                HomeScreen(
                                    showExitDialog = showExitDialog,
                                    changeExitDialogStatus = { res -> showExitDialog = res },
                                    onNavigateToPremium = { navigateTo(getPremiumDestination()) },
                                    onNavigateToJunk = { navigateTo(AppDestination.JunkCleaner) },
                                    onNavigateToApps = { navigateTo(AppDestination.AppsManager) },
                                    onNavigateToProcess = { navigateTo(AppDestination.ProcessManager()) },
                                    onNavigateToDuplicates = { navigateTo(AppDestination.Duplicates) },
                                    onNavigateToWhatsApp = { navigateTo(AppDestination.WhatsAppCleaner) },
                                    onNavigateToPhotoCompressor = { navigateTo(AppDestination.PhotoCompressor) },
                                    onNavigateToAiCleanup = { navigateTo(AppDestination.AiCleanup) },
                                    onNavigateToBattery = { navigateTo(AppDestination.BatteryOptimize) },
                                    onNavigateToDeviceInfo = { navigateTo(AppDestination.DeviceInfo) },
                                    onNavigateToSettings = { switchTab(AppDestination.Setting) }
                                )
                            }

                            is AppDestination.PhotoCompressor -> NavEntry(route) {
                                PhotoCompressorScreen(onBack = { navigateBack() }, onTaskCompleted = { finishTask(it) })
                            }

                            is AppDestination.WhatsAppCleaner -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.components.ProvideScanAccent(com.phonecleaner.aicleaner.presentation.ui.theme.Brand.WhatsApp, androidx.compose.material.icons.Icons.Rounded.Forum, com.phonecleaner.aicleaner.R.drawable.load_wa, 520f / 410f) { WhatsAppCleanerScreen(onBack = { navigateBack() }, onTaskCompleted = { finishTask(it) }) }
                            }

                            is AppDestination.Duplicates -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.components.ProvideScanAccent(com.phonecleaner.aicleaner.presentation.ui.theme.Brand.Duplicates, androidx.compose.material.icons.Icons.Rounded.ContentCopy) { DuplicatesScreen(onBack = { navigateBack() }, onTaskCompleted = { finishTask(it) }) }
                            }

                            is AppDestination.ProcessManager -> NavEntry(route) {
                                ProcessManagerScreen(onBack = { navigateBack() }, autoStart = route.autoStart, onTaskCompleted = { finishTask(it) })
                            }

                            is AppDestination.AppsManager -> NavEntry(route) {
                                AppsManagerScreen(onBack = { navigateBack() })
                            }

                            is AppDestination.JunkCleaner -> NavEntry(route) {
                                JunkCleanerScreen(onBack = { navigateBack() }, onTaskCompleted = { finishTask(it) })
                            }

                            is AppDestination.Setting -> NavEntry(route) {
                                SettingScreen(
                                    onNavigateToFeedback = { navigateTo(AppDestination.Feedback) },
                                    onNavigateToLanguage = {
                                        navigateTo(AppDestination.AppLanguage(isFromSplash = false))
                                    },
                                    onNavigateToPremium = { navigateTo(getPremiumDestination()) }
                                )
                            }

                            is AppDestination.Files -> NavEntry(route) {
                                FilesScreen(
                                    onOpenCategory = { navigateTo(AppDestination.FileCategoryRoute(it.name)) },
                                    onSearch = { navigateTo(AppDestination.FileSearch) },
                                    onBrowse = { navigateTo(AppDestination.StorageBrowser) },
                                    onSettings = { switchTab(AppDestination.Setting) }
                                )
                            }

                            is AppDestination.FileCategoryRoute -> NavEntry(route) {
                                FileCategoryScreen(
                                    category = FileCategory.valueOf(route.category),
                                    onBack = { navigateBack() }
                                )
                            }

                            is AppDestination.StorageBrowser -> NavEntry(route) {
                                StorageBrowserScreen(onBack = { navigateBack() }, onSearch = { navigateTo(AppDestination.FileSearch) })
                            }

                            is AppDestination.FileSearch -> NavEntry(route) {
                                FileCategoryScreen(category = null, onBack = { navigateBack() })
                            }

                            is AppDestination.AiCleanup -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.components.ProvideScanAccent(com.phonecleaner.aicleaner.presentation.ui.theme.Brand.Ai, androidx.compose.material.icons.Icons.Rounded.AutoAwesome, com.phonecleaner.aicleaner.R.drawable.load_ai, 500f / 390f) { AiCleanupScreen(
                                    onBack = { navigateBack() },
                                    onOpenDuplicates = { replaceTop(AppDestination.Duplicates) },
                                    onOpenCategory = { navigateTo(AppDestination.AiCategory(it.name)) },
                                    onFinished = { finishTask(it) }
                                ) }
                            }

                            is AppDestination.AiCategory -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.components.ProvideScanAccent(com.phonecleaner.aicleaner.presentation.ui.theme.Brand.Ai, androidx.compose.material.icons.Icons.Rounded.AutoAwesome, com.phonecleaner.aicleaner.R.drawable.load_ai, 500f / 390f) { AiCategoryScreen(
                                    kind = com.phonecleaner.aicleaner.data.ai.AiKind.valueOf(route.kind),
                                    onBack = { navigateBack() },
                                    onFinished = { finishTask(it) }
                                ) }
                            }

                            is AppDestination.BatteryOptimize -> NavEntry(route) {
                                BatteryOptimizeScreen(onBack = { navigateBack() }, onFinished = { finishTask(it) })
                            }
                            is AppDestination.TaskCompleted -> NavEntry(route) {
                                TaskCompletedScreen(
                                    summary = route.summary,
                                    onSkip = { navigateBack() },
                                    onOpenTask = { task ->
                                        navigateTo(
                                            when (task) {
                                                MainTask.JUNK -> AppDestination.JunkCleaner
                                                MainTask.PROCESS -> AppDestination.ProcessManager()
                                                MainTask.BATTERY -> AppDestination.BatteryOptimize
                                            }
                                        )
                                    },
                                    onOpenOther = { feature ->
                                        navigateTo(
                                            when (feature) {
                                                OtherFeature.AI -> AppDestination.AiCleanup
                                                OtherFeature.WHATSAPP -> AppDestination.WhatsAppCleaner
                                                OtherFeature.DUPLICATES -> AppDestination.Duplicates
                                            }
                                        )
                                    }
                                )
                            }
                            is AppDestination.DeviceInfo -> NavEntry(route) {
                                com.phonecleaner.aicleaner.presentation.ui.theme.ForceLightTheme {
                                    DeviceInfoScreen(
                                    onBack = { navigateBack() },
                                    onClean = { navigateTo(AppDestination.JunkCleaner) },
                                    onOptimize = { navigateTo(AppDestination.ProcessManager()) }
                                    )
                                }
                            }

                            is AppDestination.Feedback -> NavEntry(route) {
                                FeedbackScreen(onBackClick = { navigateBack() })
                            }

                            is AppDestination.AppLanguage -> NavEntry(route) {
                                AppLanguageScreen(
                                    onBack = { continueOnboardingOr { navigateBack() } },
                                    onDone = { continueOnboardingOr { navigateBack() } }
                                )
                            }

                            else -> NavEntry(route) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Unknown destination: $route",
                                        style = TextStyle(color = Color.Red, fontSize = 16.sp)
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                Log.e("AppNavigator", "Backstack is empty, skipping NavDisplay")
            }

            val homeReached by homeScreenReachedForCurrentSession.collectAsStateWithLifecycle()
            val showBottomNav = currentDestination in bottomBarDestinations
            val hideBannerOnThisScreen =
                currentDestination != null &&
                    currentDestination::class in shouldNotShowBannerAdTypes
            val density = LocalDensity.current
            val keyboardOpen = WindowInsets.ime.getBottom(density) > 0
            val showBanner = homeReached &&
                !hideBannerOnThisScreen &&
                !keyboardOpen &&
                !isPremiumUser() &&
                RemoteConstants.innerBannerAdType != 0 &&
                (innerBannerAd != null || isBannerLoading)

            LaunchedEffect(showBanner) {
                if (!showBanner) {
                    actualBannerOverlayHeight = 0.dp
                }
            }

            Spacer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(MaterialTheme.colorScheme.background)
            )

            if (showBottomNav || showBanner) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    if (showBottomNav) {
                        BottomNavigationBar(
                            modifier = modifier,
                            backStack = backStack
                        )
                    }

                    if (showBanner) {
                        BannerAdCompose(
                            bannerAd = innerBannerAd,
                            isLoading = isBannerLoading,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                actualBannerOverlayHeight =
                                    with(density) { coordinates.size.height.toDp() }
                            },
                            loadingHeight = 50.dp,
                            verticalInset = 4.sdp,
                            backgroundColor = onSecondaryFixedVariantLight
                        )
                    }
                }
            }
        }

        if (showOnboardingAdLoading || innerInterLoading) {
            LoadingDialog(
                text = stringResource(R.string.loading_ad),
                onDismiss = {}
            )
        }
    }
}
