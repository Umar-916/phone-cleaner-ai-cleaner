package com.phonecleaner.aicleaner.core.common

import com.phonecleaner.aicleaner.core.advertisement.initialization.InitializationHelper
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.datastore.core.IOException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonecleaner.aicleaner.BuildConfig
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.analytics.AnalyticsLogger
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeKeys
import org.koin.compose.koinInject
import java.io.FileNotFoundException
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

fun Any?.debug(tag: String = "DebugLog", message: String = "") {
    if (BuildConfig.DEBUG) {
        Log.d(tag, "$message $this")
    }
}

fun Any?.debugRelease(tag: String = "DebugLog", message: String = "") {
    Log.d(tag, "$message $this")
}

fun Context.postAnalytic(text: String) {
    tryCatch {
        AnalyticsLogger.logEvent(this, text)
    }
}

fun Activity.postAnalytic(text: String) {
    tryCatch {
        AnalyticsLogger.logEvent(this, text)
    }
}

fun isPremiumUser(): Boolean {
    return DataShare.isPremium || BillingUtilsIAP.isPurchased.value || BillingUtilsIAP.isPremium.value == true
}

fun Context.canRequestAds(): Boolean {
    if (!Constants.ADS_ENABLED || isPremiumUser() || !isNetworkAvailable() || !InitializationHelper.mobileAdsInitialized) {
        return false
    }
    return (this as? Activity)?.isValidContext() ?: true
}

fun Context.showToast(text: String) {

    tryCatch {

        Handler(Looper.getMainLooper()).post {

            tryCatch {
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun Context.getSafeString(stringID: Int): String {

    var targetString = ""

    tryCatch {
        targetString = this.getString(stringID)
    }

    return targetString
}

inline fun tryCatch(block: () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        t.localizedMessage.debug()
    } catch (e: NullPointerException) {
        e.localizedMessage.debug()
    } catch (e: IllegalArgumentException) {
        e.localizedMessage.debug()
    } catch (e: IllegalStateException) {
        e.localizedMessage.debug()
    } catch (e: IndexOutOfBoundsException) {
        e.localizedMessage.debug()
    } catch (e: UnsupportedOperationException) {
        e.localizedMessage.debug()
    } catch (e: ClassCastException) {
        e.localizedMessage.debug()
    } catch (e: IOException) {
        e.localizedMessage.debug()
    } catch (e: Exception) {
        e.localizedMessage.debug()
    } catch (e: java.lang.Exception) {
        e.localizedMessage.debug()
    } catch (e: java.io.IOException) {
        e.localizedMessage.debug()
    } catch (e: FileNotFoundException) {
        e.localizedMessage.debug()
    } catch (e: UninitializedPropertyAccessException) {
        e.localizedMessage.debug()
    }
}


fun Context.isNetworkAvailable(): Boolean {
    var result = false
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    cm?.let {
        it.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
    return result
}

fun Activity?.isValidContext(): Boolean {
    return this != null && !this.isFinishing && !this.isDestroyed
}

fun Long.timeDifference(): Int {
    val current = Calendar.getInstance().timeInMillis
    val elapsedTime = current - this
    return TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toInt()
}

@Composable
fun SetStatusBarIconsDarkWithReset(darkIcons: Boolean) {
    val view = LocalView.current
    val activity = view.context as? Activity

    DisposableEffect(Unit) {
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, view) }
        val originalDarkIcons = controller?.isAppearanceLightStatusBars

        controller?.isAppearanceLightStatusBars = darkIcons // Use light icons (for dark background)

        onDispose {
            controller?.isAppearanceLightStatusBars = originalDarkIcons == true
        }
    }
}

@Composable
fun ApplySystemBarColors(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set colors
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            // Set icon light/dark
            insetsController.isAppearanceLightStatusBars = darkIcons
            insetsController.isAppearanceLightNavigationBars = darkIcons
        }
    }
}

private object SystemBarsHideHolders {
    private var count = 0
    private var savedBehavior: Int? = null

    fun acquire(controller: WindowInsetsControllerCompat) {
        if (count == 0) {
            savedBehavior = controller.systemBarsBehavior
        }
        count++
    }

    fun release(controller: WindowInsetsControllerCompat) {
        count = (count - 1).coerceAtLeast(0)
        if (count == 0) {
            controller.show(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
            )
            savedBehavior?.let { controller.systemBarsBehavior = it }
            savedBehavior = null
        }
    }

    val isHeld: Boolean get() = count > 0
}

@Composable
fun HideSystemBars() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(window, view, lifecycleOwner) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val bars = WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()

        fun hideBars() {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(bars)
        }

        SystemBarsHideHolders.acquire(controller)
        hideBars()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && SystemBarsHideHolders.isHeld) {
                hideBars()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Nav transitions keep both screens composed; splash onDispose can show bars
        // after premium already hid them. Re-apply after the next frame so hide wins.
        val keepHidden = Runnable {
            if (SystemBarsHideHolders.isHeld) hideBars()
        }
        view.post(keepHidden)

        onDispose {
            view.removeCallbacks(keepHidden)
            lifecycleOwner.lifecycle.removeObserver(observer)
            SystemBarsHideHolders.release(controller)
        }
    }
}

/** ShareApp */
fun Context.shareAppLink() {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(
            Intent.EXTRA_TEXT,
            "Clean junk, free memory and manage your files with Phone Cleaner AI Cleaner Pro: https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
        type = "text/plain"
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_via)))
}

private const val SHARE_DOWNLOAD_LIMIT = 15L * 1024 * 1024

/** "Sound name - App name" followed by the Play Store link, sent along with every shared sound. */
private fun Context.soundShareText(displayName: String) =
    "$displayName - ${getString(R.string.app_name)}\n" +
        "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

/** Opens the "Share via" chooser with the audio [file] (when there is one) and the app link as text. */
private fun Context.startSoundShare(file: java.io.File?, mimeType: String, displayName: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, soundShareText(displayName))
        putExtra(Intent.EXTRA_SUBJECT, displayName)
        putExtra(Intent.EXTRA_TITLE, displayName)
        if (file != null) {
            val uri = androidx.core.content.FileProvider.getUriForFile(this@startSoundShare, "$packageName.fileprovider", file)
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_via)))
}

/** An empty, cleaned share folder plus a safe file name for [displayName]. */
private fun Context.sharedSoundFile(displayName: String, extension: String): java.io.File {
    val safeName = displayName.replace(Regex("[\\/:*?\"<>|\r\n]"), "_").trim().take(60).ifEmpty { "sound" }
    val dir = java.io.File(cacheDir, "shared_sounds").apply { mkdirs() }
    // Only stale copies go: the app that received the previous share may still be reading its file.
    val staleBefore = System.currentTimeMillis() - 60 * 60 * 1000L
    dir.listFiles()?.forEach { if (it.lastModified() < staleBefore) it.delete() }
    return java.io.File(dir, "$safeName.$extension")
}

private fun mimeFor(extension: String) = if (extension.equals("mp3", true)) "audio/mpeg" else "audio/mp4"

/**
 * Shares a sound the user made (an audio file) through the "Share via" chooser, so WhatsApp and every other
 * app can take it, together with the app link. If the file can't be read only the app link is shared.
 */
fun Context.shareSoundFile(source: java.io.File, displayName: String) {
    runCatching {
        val ext = source.extension.ifEmpty { "m4a" }
        val copy = sharedSoundFile(displayName, ext)
        source.copyTo(copy, overwrite = true)
        startSoundShare(copy, mimeFor(ext), displayName)
    }.onFailure { runCatching { startSoundShare(null, "", displayName) } }
}

/**
 * Shares any sound of the app as an audio file plus the app link: the user's own file, the bundled mp3, or
 * (for backend-only sounds) the downloaded copy. Whatever can't be fetched (e.g. offline) falls back to
 * sharing just the app link, so the button never does nothing.
 */
suspend fun Context.shareSound(
    displayName: String,
    filePath: String?,
    assetPath: String,
    remoteUrl: String?
) {
    val prepared: Pair<java.io.File, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            when {
                !filePath.isNullOrBlank() -> {
                    val source = java.io.File(filePath)
                    val ext = source.extension.ifEmpty { "m4a" }
                    sharedSoundFile(displayName, ext).also { source.copyTo(it, overwrite = true) } to ext
                }

                assetPath.isNotBlank() -> {
                    val ext = assetPath.substringAfterLast('.', "mp3")
                    val target = sharedSoundFile(displayName, ext)
                    assets.open(assetPath).use { input -> target.outputStream().use { input.copyTo(it) } }
                    target to ext
                }

                !remoteUrl.isNullOrBlank() && isNetworkAvailable() -> {
                    val ext = remoteUrl.substringBefore('?').substringAfterLast('.', "mp3").take(4).ifEmpty { "mp3" }
                    val target = sharedSoundFile(displayName, ext)
                    val connection = (java.net.URL(remoteUrl).openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 10_000
                        readTimeout = 10_000
                    }
                    try {
                        if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
                        connection.inputStream.use { input ->
                            target.outputStream().use { out ->
                                val buffer = ByteArray(16 * 1024)
                                var total = 0L
                                while (true) {
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    total += n
                                    if (total > SHARE_DOWNLOAD_LIMIT) error("too large")
                                    out.write(buffer, 0, n)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        target.delete()
                        throw e
                    } finally {
                        connection.disconnect()
                    }
                    target to ext
                }

                else -> null
            }
        }.getOrNull()
    }
    runCatching { startSoundShare(prepared?.first, mimeFor(prepared?.second.orEmpty()), displayName) }
}

fun Context.openInPlayStore() {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$packageName".toUri()
        )
        startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri()
            )
            startActivity(intent)
        } catch (_: Exception) {

            showToast(
                getSafeString(R.string.unable_to_open_play_store)
            )

        }
    }
}

/**Privacy Policy */
fun Context.openUrl(url: String, errorMessage: String = "Unable to open link") {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        ContextCompat.startActivity(this, intent, null)
    } catch (e: Exception) {
        e.printStackTrace()

        showToast(
            errorMessage
        )
    }
}

fun Context.openWebLinkExternally(url: String) {
    tryCatch {
        if (isNetworkAvailable()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = url.toUri()
                startActivity(intent)
            } catch (_: Exception) {
                showToast(getSafeString(R.string.something_went_wrong))
            } catch (_: java.lang.Exception) {
                showToast(getSafeString(R.string.something_went_wrong))
            }
        } else {
            showToast(getSafeString(R.string.internet_not_available))
        }
    }
}

@Composable
fun isAppInDarkTheme(): Boolean {
    val prefRepo: AppDataStore = koinInject()

    val themeKey by prefRepo.themeKeyFlow.collectAsStateWithLifecycle(
        initialValue = ThemeKeys.SYSTEM
    )

    val isSystemDark = isSystemInDarkTheme()

    return when (themeKey) {
        ThemeKeys.DARK -> true
        ThemeKeys.LIGHT -> false
        else -> isSystemDark
    }
}

@Composable
fun Modifier.keyboardPadding(): Modifier {
    val imeInsets = WindowInsets.ime
    val bottomPadding = imeInsets.getBottom(LocalDensity.current)
    return this.padding(bottom = with(LocalDensity.current) { bottomPadding.toDp() })
}




