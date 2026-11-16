package com.phonecleaner.aicleaner.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class AppDestination : NavKey {
    @Serializable
    data object Splash : AppDestination()

    @Serializable
    data object Home : AppDestination()

    @Serializable
    data object Setting : AppDestination()

    @Serializable
    data object Files : AppDestination()

    @Serializable
    data class FileCategoryRoute(val category: String) : AppDestination()

    @Serializable
    data object FileSearch : AppDestination()

    @Serializable
    data object StorageBrowser : AppDestination()

    @Serializable
    data object JunkCleaner : AppDestination()

    @Serializable
    data object AppsManager : AppDestination()

    @Serializable
    data object ProcessManager : AppDestination()

    @Serializable
    data object Duplicates : AppDestination()

    @Serializable
    data object WhatsAppCleaner : AppDestination()

    @Serializable
    data object PhotoCompressor : AppDestination()

    @Serializable
    data object AiCleanup : AppDestination()

    @Serializable
    data object BatteryManager : AppDestination()

    @Serializable
    data class TaskCompleted(val summary: String) : AppDestination()

    @Serializable
    data object Feedback : AppDestination()

    @Serializable
    data class AppLanguage(val isFromSplash: Boolean = false) : AppDestination()

    @Serializable
    data class SliderPremiumScreen(val isFromSplash: Boolean) : AppDestination()

    @Serializable
    data class BlackPremiumScreen(val isFromSplash: Boolean) : AppDestination()

    @Serializable
    data class PremiumZippoScreen(val isFromSplash: Boolean) : AppDestination()
}
