package com.phonecleaner.aicleaner.core.analytics

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.data.remote.ApiHttpException
import kotlinx.coroutines.CancellationException
import java.io.InterruptedIOException

/**
 * Posts (and logs) the lifecycle of every backend call so API health is visible in analytics:
 *  - `send_<name>_api_call`          request started
 *  - `<name>_api_success`       response parsed OK
 *  - `<name>_api_timeout`       connect/read/call timeout
 *  - `<name>_api_error_<code>`  server answered with an HTTP / payload error code
 *  - `<name>_api_failed`        any other failure (no route, DNS, malformed body, ...)
 */
object ApiTracker {

    private const val TAG = "ApiTracker"

    /** Logged in release builds too, so API health can be checked with `adb logcat -s tagForApiTesting`. */
    private const val TAG_FOR_RELEASE = "tagForApiTesting"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun <T> track(api: String, block: suspend () -> T): T {
        val startedAt = SystemClock.elapsedRealtime()
        post("send_${api}_api_call")
        try {
            return block().also {
                post("${api}_api_success", "${SystemClock.elapsedRealtime() - startedAt}ms")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val tookMs = "${SystemClock.elapsedRealtime() - startedAt}ms"
            when (e) {
                is ApiHttpException -> post("${api}_api_error_${e.code}", "HTTP ${e.code}: ${e.message} ($tookMs)")
                is InterruptedIOException -> post("${api}_api_timeout", "${e.message} ($tookMs)")
                else -> post("${api}_api_failed", "${e.message ?: e.javaClass.simpleName} ($tookMs)")
            }
            throw e
        }
    }

    private fun post(event: String, detail: String? = null) {
        val line = "$event${detail?.let { " -> $it" }.orEmpty()}"
        line.debug(TAG)
        Log.i(TAG_FOR_RELEASE, line)
        appContext?.postAnalytic(event)
    }
}
