package com.phonecleaner.aicleaner.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.phonecleaner.aicleaner.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppsRepository(private val context: Context) {

    suspend fun loadApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        installedPackages(pm)
            .filter { it.packageName != context.packageName }
            .mapNotNull { info -> info.toInstalledApp(pm) }
    }

    private fun installedPackages(pm: PackageManager): List<PackageInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

    private fun PackageInfo.toInstalledApp(pm: PackageManager): InstalledApp? {
        val appInfo = applicationInfo ?: return null
        return InstalledApp(
            packageName = packageName,
            label = appInfo.loadLabel(pm).toString(),
            versionName = versionName.orEmpty(),
            isSystem = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            apkSizeBytes = appInfo.sourceDir?.let { File(it).length() } ?: 0L,
            installedAt = firstInstallTime,
            canLaunch = pm.getLaunchIntentForPackage(packageName) != null
        )
    }
}
