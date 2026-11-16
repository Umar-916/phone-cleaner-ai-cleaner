package com.phonecleaner.aicleaner.presentation.ui.screens.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeKeys
import com.phonecleaner.aicleaner.presentation.navigation.AppNavigator
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageHelper
import com.phonecleaner.aicleaner.presentation.ui.theme.PhoneCleanerProTheme
import network.chaintech.sdpcomposemultiplatform.SDPConfig
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val prefRepo: AppDataStore by inject()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val themeKey by prefRepo.themeKeyFlow.collectAsState(initial = ThemeKeys.SYSTEM)
            val appliedLocale by LanguageHelper.appliedLocale.collectAsState()
            val layoutDirection =
                if (LanguageHelper.isRtl(appliedLocale)) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                PhoneCleanerProTheme(themeKey = themeKey) {
                    AppNavigator()
                }
            }
        }

        tryCatch {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = Color(0xFF006C54).value.toInt(),
                    darkScrim = Color(0xFF006C54).value.toInt()
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = Color.White.value.toInt(),
                    darkScrim = Color.White.value.toInt()
                )
            )
        }

        SDPConfig.setScalingRatio(375.0)

        "Process: ${android.os.Process.myPid()}".debug("PROCESS_CHECK")

        "Instance: ${this@MainActivity}".debug("PROCESS_CHECK")
    }

    fun forceUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                tryCatch {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        1000,
                    )
                }
            }
        }
    }
}
