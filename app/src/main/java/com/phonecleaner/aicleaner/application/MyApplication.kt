package com.phonecleaner.aicleaner.application

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.phonecleaner.aicleaner.core.advertisement.AppOpenAdManager
import com.phonecleaner.aicleaner.core.advertisement.initialization.InitializationHelper
import com.phonecleaner.aicleaner.core.analytics.ApiTracker
import com.phonecleaner.aicleaner.core.common.buildAppImageLoader
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.di.initKoin
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        buildAppImageLoader(context)


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageHelper.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()

        ApiTracker.init(this)

        initKoin {
            androidLogger() // Optional: For Koin logging
            androidContext(this@MyApplication) // Provide Android context to Koin
        }

        tryCatch {
            checkForProcess()
        }
    }

    private fun checkForProcess() {

        tryCatch {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.d("Process", "${getProcessName()}")

                if (getProcessName().toString() == packageName) {
                    initialization()
                }

            } else {

                val pid = Process.myPid()
                val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager

                for (processInfo in manager.runningAppProcesses) {

                    if (processInfo.pid == pid) {

                        Log.d("Process", "${processInfo.processName}")

                        if (processInfo.processName == packageName) {
                            initialization()
                        }

                        break
                    }
                }
            }
        }
    }

    private fun initialization() {
        tryCatch {

            InitializationHelper.resetAll()

            applicationClassInitialized = false
        }
    }

    /**** AppOpenManager ****/

    private var applicationClassInitialized = false

    var appOpenManager: AppOpenAdManager? = null

    fun initializeAppOpen() {

        if (!applicationClassInitialized) {

            applicationClassInitialized = true

            appOpenManager = AppOpenAdManager(this@MyApplication)
        }
    }
}