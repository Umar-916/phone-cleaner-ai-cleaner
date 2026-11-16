package com.phonecleaner.aicleaner.domain

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.tryCatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class FeedbackEmail(
    val subject: String,
    val body: String
)

class FeedbackViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems = _selectedItems.asStateFlow()

    private val _showMaxSelectionToast = MutableStateFlow(false)
    val showMaxSelectionToast = _showMaxSelectionToast.asStateFlow()

    private val _showReportBanner = MutableStateFlow(true)
    val showReportBanner = _showReportBanner.asStateFlow()

    private var bannerShowCount = 0
    private val maxSelection = 3

    fun onTabSelected(tab: Int) {
        _selectedTab.value = tab
        _selectedItems.value = emptySet()
        _description.value = ""
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onItemSelected(item: String) {
        val current = _selectedItems.value
        _selectedItems.value = if (item in current) {
            current - item
        } else {
            // Enforce max selection limit
            if (current.size >= maxSelection) {
                _showMaxSelectionToast.value = true
                current // Don't add if limit reached
            } else {
                current + item
            }
        }
    }

    fun onMaxSelectionToastShown() {
        _showMaxSelectionToast.value = false
    }

    fun closeReportBanner() {
        bannerShowCount++
        if (bannerShowCount >= 2) {
            _showReportBanner.value = false
        }
    }

    fun prepareFeedbackEmail(): FeedbackEmail? {
        val items = _selectedItems.value
        if (items.isEmpty()) return null

        val type = if (_selectedTab.value == 0) "Issue" else "Suggestion"
        val subject = "Funny Sounds Feedback - $type"
        val body = buildString {
            appendLine("Type: $type")
            appendLine("Selected: ${items.joinToString(", ")}")
            val desc = _description.value
            if (desc.isNotBlank()) {
                appendLine("Description: $desc")
            }
        }
        return FeedbackEmail(subject = subject, body = body)
    }

    fun sendFeedBack(activity: Activity, feedBack: String) {

        tryCatch {
            val currentDateTime = SimpleDateFormat(
                "dd-MMMM-yyyy HH:mm:ss",
                Locale.US
            ).format(System.currentTimeMillis()) ?: ""

            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)

            val versionName = packageInfo.versionName ?: "Unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val timeZone = TimeZone.getDefault()

            val responseText = buildString {
                appendLine("App: Funny Sounds")
                appendLine("Package: ${activity.packageName}")
                appendLine("Device: ${Build.DEVICE}")
                appendLine("Model: ${Build.MODEL}")
                appendLine("Brand: ${Build.BRAND}")
                appendLine("Manufacturer: ${Build.MANUFACTURER}")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Time Zone: ${timeZone.id} (${timeZone.displayName})")
                appendLine("Version Name: $versionName")
                appendLine("Version Code: $versionCode")
                appendLine(currentDateTime)
                appendLine()
                appendLine(feedBack.trim())
                appendLine()

            }

            val uriText =
                StringBuilder().append("mailto:appsresort5@gmail.com?subject=")
                    .append(Uri.encode("Funny Sounds Feedback")).append("&body=")
                    .append(Uri.encode(responseText)).toString()

            responseText.debug("FeedbackTesting")

            val uri = uriText.toUri()

            val sendIntent = Intent(Intent.ACTION_SENDTO)

            sendIntent.data = uri

            tryCatch {
                activity.startActivity(Intent.createChooser(sendIntent, "Send email"))
            }
        }
    }

}
