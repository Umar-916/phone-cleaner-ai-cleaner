package com.phonecleaner.aicleaner.core.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

object StoragePermission {

    const val LEGACY_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

    val usesSettingsScreen: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun isGranted(context: Context): Boolean =
        if (usesSettingsScreen) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, LEGACY_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

    fun settingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = "package:${context.packageName}".toUri()
        }
}
