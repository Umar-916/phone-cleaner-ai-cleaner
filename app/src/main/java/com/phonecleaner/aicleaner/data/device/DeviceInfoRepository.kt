package com.phonecleaner.aicleaner.data.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

data class InfoRow(@StringRes val label: Int, val value: String)

data class InfoSection(@StringRes val title: Int, val rows: List<InfoRow>)

/** Reads device facts that need no permission: build properties, memory, storage and display. */
class DeviceInfoRepository(private val context: Context) {

    fun read(): List<InfoSection> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }
        val stat = StatFs(Environment.getDataDirectory().path)
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        val display = windowManager.defaultDisplay.also { it.getRealMetrics(metrics) }

        fun size(bytes: Long) = Formatter.formatFileSize(context, bytes)

        return listOf(
            InfoSection(
                R.string.device_section_device,
                listOf(
                    InfoRow(R.string.device_manufacturer, Build.MANUFACTURER.replaceFirstChar { it.uppercase() }),
                    InfoRow(R.string.device_model, Build.MODEL),
                    InfoRow(R.string.device_brand, Build.BRAND.replaceFirstChar { it.uppercase() }),
                    InfoRow(R.string.device_codename, Build.DEVICE)
                )
            ),
            InfoSection(
                R.string.device_section_system,
                listOf(
                    InfoRow(R.string.device_android, "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"),
                    InfoRow(R.string.device_patch, Build.VERSION.SECURITY_PATCH.ifBlank { "-" }),
                    InfoRow(R.string.device_build, Build.DISPLAY)
                )
            ),
            InfoSection(
                R.string.device_section_hardware,
                listOf(
                    InfoRow(R.string.device_processor, processorName()),
                    InfoRow(R.string.device_cores, Runtime.getRuntime().availableProcessors().toString()),
                    InfoRow(R.string.device_abi, Build.SUPPORTED_ABIS.firstOrNull().orEmpty()),
                    InfoRow(R.string.device_ram, size(memory.totalMem))
                )
            ),
            InfoSection(
                R.string.device_section_storage,
                listOf(
                    InfoRow(R.string.device_storage_total, size(stat.totalBytes)),
                    InfoRow(R.string.device_storage_used, size(stat.totalBytes - stat.availableBytes)),
                    InfoRow(R.string.device_storage_free, size(stat.availableBytes))
                )
            ),
            InfoSection(
                R.string.device_section_display,
                listOf(
                    InfoRow(R.string.device_resolution, "${metrics.widthPixels} × ${metrics.heightPixels}"),
                    InfoRow(R.string.device_density, "${metrics.densityDpi} dpi"),
                    InfoRow(R.string.device_refresh, "%.0f Hz".format(display.refreshRate))
                )
            )
        )
    }

    private fun processorName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val soc = "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL}".trim()
            if (soc.isNotBlank() && !soc.contains("unknown", ignoreCase = true)) return soc
        }
        return Build.HARDWARE
    }
}
