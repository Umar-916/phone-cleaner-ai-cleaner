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

    fun userAppCount(): Int = userPackages().size

    fun killBackground(): Int {
        val targets = userPackages()
        targets.forEach { pkg -> tryCatch { activityManager.killBackgroundProcesses(pkg) } }
        return targets.size
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
