package com.phonecleaner.aicleaner.data.process

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.phonecleaner.aicleaner.domain.model.BoostResult
import com.phonecleaner.aicleaner.domain.model.MemoryStats
import com.phonecleaner.aicleaner.domain.model.RecentApp
import com.phonecleaner.aicleaner.core.common.tryCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProcessRepository(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val packageManager = context.packageManager

    fun memory(): MemoryStats {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return MemoryStats(totalBytes = info.totalMem, availableBytes = info.availMem)
    }

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun recentApps(): List<RecentApp> = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) return@withContext emptyList()
        val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - RECENT_WINDOW_MS, now)
            .asSequence()
            .filter { it.lastTimeUsed > 0 && it.packageName != context.packageName }
            .groupBy { it.packageName }
            .mapNotNull { (pkg, stats) ->
                if (packageManager.getLaunchIntentForPackage(pkg) == null) return@mapNotNull null
                val label = runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrNull() ?: return@mapNotNull null
                RecentApp(pkg, label, stats.maxOf { it.lastTimeUsed }, stats.sumOf { it.totalTimeInForeground })
            }
            .sortedByDescending { it.lastUsedAt }
    }

    suspend fun boost(onProgress: (percent: Int, label: String) -> Unit = { _, _ -> }): BoostResult = withContext(Dispatchers.IO) {
        val before = memory().availableBytes
        val targets = userPackages()
        val step = (6000L / targets.size.coerceAtLeast(1)).coerceIn(80L, 450L)
        targets.forEachIndexed { index, pkg ->
            val label = labelOf(pkg)
            onProgress(((index + 1) * 100) / targets.size, label)
            tryCatch { activityManager.killBackgroundProcesses(pkg) }
            delay(step)
        }
        onProgress(100, "")
        delay(SETTLE_DELAY_MS)
        val after = memory().availableBytes
        BoostResult(freedBytes = (after - before).coerceAtLeast(0L), appsCount = targets.size)
    }

    private fun labelOf(pkg: String): String =
        runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrDefault(pkg)

    private fun userPackages(): List<String> {
        val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }
        return installed
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { it.packageName }
            .filter { it != context.packageName }
    }

    private companion object {
        const val RECENT_WINDOW_MS = 24L * 60 * 60 * 1000
        const val SETTLE_DELAY_MS = 1500L
    }
}
