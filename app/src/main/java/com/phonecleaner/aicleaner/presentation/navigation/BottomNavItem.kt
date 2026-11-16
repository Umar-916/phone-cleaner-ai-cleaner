package com.phonecleaner.aicleaner.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.phonecleaner.aicleaner.R

sealed class BottomNavItem(
    @StringRes val title: Int,
    val icon: ImageVector,
    val destination: AppDestination,
    val analyticsName: String
) {
    object Cleaner : BottomNavItem(R.string.nav_cleaner, Icons.Rounded.CleaningServices, AppDestination.Home, "cleaner")
    object Files : BottomNavItem(R.string.nav_files, Icons.Rounded.FolderOpen, AppDestination.Files, "files")
    object Settings : BottomNavItem(R.string.settings, Icons.Rounded.Settings, AppDestination.Setting, "settings")
}
