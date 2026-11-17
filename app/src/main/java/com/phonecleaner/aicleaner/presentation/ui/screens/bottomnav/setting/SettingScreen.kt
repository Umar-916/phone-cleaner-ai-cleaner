package com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting

import com.phonecleaner.aicleaner.core.common.tapEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.BuildConfig
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsEvents
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.isPremiumUser
import com.phonecleaner.aicleaner.core.common.openInPlayStore
import com.phonecleaner.aicleaner.core.common.openUrl
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.shareAppLink
import com.phonecleaner.aicleaner.core.utils.rememberClickGuard
import com.phonecleaner.aicleaner.domain.SettingViewModel
import com.phonecleaner.aicleaner.presentation.dialogues.rate_us.RateUsDialog
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeDialog
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeKeys
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.navigation.getTotalBottomHeight
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers.SectionTitle
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers.SettingDivider
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers.SettingItem
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers.SettingSection
import com.phonecleaner.aicleaner.presentation.ui.screens.bottomnav.setting.helpers.UpgradeProCard
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageCatalog
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingScreen(
    settingViewModel: SettingViewModel = koinViewModel(),
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clickGuard = rememberClickGuard()

    val showRateUsDialog by settingViewModel.showRateUsDialog.collectAsStateWithLifecycle()
    val showThemeDialog by settingViewModel.showThemeDialog.collectAsStateWithLifecycle()
    val themeKey by settingViewModel.themeKey.collectAsStateWithLifecycle()
    val languageCode by settingViewModel.languageCode.collectAsStateWithLifecycle()
    val showHidden by settingViewModel.showHiddenFiles.collectAsStateWithLifecycle()
    var showAbout by remember { mutableStateOf(false) }

    val selectedTheme = when (themeKey) {
        ThemeKeys.DARK -> stringResource(R.string.dark)
        ThemeKeys.LIGHT -> stringResource(R.string.light)
        else -> stringResource(R.string.system_default)
    }

    LaunchedEffect(Unit) { context.postAnalytic(AnalyticsEvents.SETTING_DISPLAY) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (showRateUsDialog || showThemeDialog) 25.dp else 0.dp)
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                stringResource(R.string.settings),
                modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 28.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )

            if (!isPremiumUser()) {
                UpgradeProCard(onClick = { tapEvent("setting_screen_tap"); clickGuard.run(onNavigateToPremium) })
                Spacer(Modifier.height(20.dp))
            }

            SectionTitle(stringResource(R.string.preferences))
            Spacer(Modifier.height(10.dp))
            SettingSection {
                SettingItem(
                    icon = Icons.Rounded.Language,
                    tint = Brand.Blue,
                    title = stringResource(R.string.app_language_new),
                    value = LanguageCatalog.displayName(languageCode),
                    onClick = { tapEvent("setting_screen_tap_2");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.APP_LANGUAGE_CLICK)
                            onNavigateToLanguage()
                        }
                    }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.DarkMode,
                    tint = Brand.Photo,
                    title = stringResource(R.string.app_theme),
                    value = selectedTheme,
                    onClick = { tapEvent("setting_screen_tap_3");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.APP_THEME_CLICK)
                            context.postAnalytic(AnalyticsEvents.APP_THEME_DIALOG_APPEAR)
                            settingViewModel.openThemeDialog()
                        }
                    }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.VisibilityOff,
                    tint = Brand.Apps,
                    title = stringResource(R.string.setting_show_hidden),
                    subtitle = stringResource(R.string.setting_show_hidden_sub),
                    switchChecked = showHidden,
                    onSwitchChange = settingViewModel::setShowHiddenFiles
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle(stringResource(R.string.support_feedback))
            Spacer(Modifier.height(10.dp))
            SettingSection {
                SettingItem(
                    icon = Icons.Rounded.Star,
                    tint = Brand.Gold,
                    title = stringResource(R.string.rate_us),
                    onClick = { tapEvent("setting_screen_tap_4");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.RATE_US_CLICK)
                            settingViewModel.openRateUsDialog()
                        }
                    }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Chat,
                    tint = Brand.Blue,
                    title = stringResource(R.string.feedback),
                    onClick = { tapEvent("setting_screen_tap_5");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.FEEDBACK_CLICK)
                            onNavigateToFeedback()
                        }
                    }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Share,
                    tint = Brand.Good,
                    title = stringResource(R.string.share_app),
                    onClick = { tapEvent("setting_screen_tap_6");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.SHARE_APP_CLICK)
                            context.shareAppLink()
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle(stringResource(R.string.legal))
            Spacer(Modifier.height(10.dp))
            SettingSection {
                SettingItem(
                    icon = Icons.Rounded.PrivacyTip,
                    tint = Brand.Duplicates,
                    title = stringResource(R.string.privacy_policy),
                    onClick = { tapEvent("setting_screen_tap_7");
                        clickGuard.run {
                            context.postAnalytic(AnalyticsEvents.PRIVACY_POLICY_CLICK)
                            context.openUrl(Constants.PRIVACY_POLICY_URL)
                        }
                    }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Description,
                    tint = Brand.Junk,
                    title = stringResource(R.string.terms_of_service),
                    onClick = { tapEvent("setting_screen_tap_8"); clickGuard.run { context.openUrl(Constants.TERMS_URL) } }
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Info,
                    tint = Brand.Process,
                    title = stringResource(R.string.about),
                    value = BuildConfig.VERSION_NAME,
                    onClick = { tapEvent("setting_screen_tap_9"); showAbout = true }
                )
            }

            Spacer(Modifier.height(getTotalBottomHeight() + getBannerOverlayHeight() + 8.dp))
        }

        if (showRateUsDialog || showThemeDialog) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
        }

        when {
            showThemeDialog -> ThemeDialog(
                show = true,
                selectedTheme = themeKey,
                onDismiss = settingViewModel::dismissThemeDialog,
                onThemeSelected = { key ->
                    when (key) {
                        ThemeKeys.SYSTEM -> context.postAnalytic(AnalyticsEvents.APP_THEME_SYSTEM_DEFAULT_SELECT)
                        ThemeKeys.LIGHT -> context.postAnalytic(AnalyticsEvents.APP_THEME_LIGHT_MODE_SELECT)
                        ThemeKeys.DARK -> context.postAnalytic(AnalyticsEvents.APP_THEME_DARK_MODE_SELECT)
                    }
                },
                onApply = { key ->
                    context.postAnalytic(AnalyticsEvents.APP_THEME_APPLY_CLICK)
                    settingViewModel.applyTheme(key)
                }
            )

            showRateUsDialog -> RateUsDialog(
                show = true,
                onDismiss = settingViewModel::dismissRateUsDialog,
                onSubmit = {
                    context.postAnalytic("rate_us_open_play_store")
                    context.openInPlayStore()
                }
            )
        }

        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                title = { Text(stringResource(R.string.app_name)) },
                text = { Text(stringResource(R.string.about_body, BuildConfig.VERSION_NAME)) },
                confirmButton = { TextButton(onClick = { tapEvent("setting_screen_tap_10"); showAbout = false }) { Text(stringResource(R.string.junk_done_action)) } }
            )
        }
    }
}
