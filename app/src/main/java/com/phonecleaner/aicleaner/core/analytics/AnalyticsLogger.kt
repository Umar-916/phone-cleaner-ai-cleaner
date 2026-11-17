package com.phonecleaner.aicleaner.core.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.phonecleaner.aicleaner.core.common.tryCatch

object AnalyticsLogger {
    private var myFirebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) = initializeAnalytics(context.applicationContext)

    /** Logs a UI tap without needing a context; the logger is set up once in the application class. */
    fun tap(event: String) {
        tryCatch {
            val name = event.trim().lowercase().replace("[^a-z0-9_]".toRegex(), "_").take(40)
            myFirebaseAnalytics?.logEvent(name, null)
            Log.i("current_analytic", "tap: $name")
        }
    }

    private fun initializeAnalytics(context: Context) {

        if (myFirebaseAnalytics == null) {
            myFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }

    fun logEvent(context: Context, value: String) {

        tryCatch {

            initializeAnalytics(context.applicationContext)

            val cleanedValue = value
                .trim()
                .lowercase()
                .replace("[^a-z0-9_]".toRegex(), "_")
                .take(40)

            val bundle = Bundle().apply {
                putString(cleanedValue, cleanedValue)
            }

            myFirebaseAnalytics?.logEvent(cleanedValue, bundle)

            Log.i("current_analytic", "postAnalytic: $cleanedValue")
        }
    }

    fun logEventWithBundle(context: Context, value: String, bundle: Bundle) {

        tryCatch {

            initializeAnalytics(context.applicationContext)

            val cleanedValue = value
                .trim()
                .lowercase()
                .replace("[^a-z0-9_]".toRegex(), "_")
                .take(40)

            myFirebaseAnalytics?.logEvent(cleanedValue, bundle)

            Log.i("current_analytic", "postAnalytic: $cleanedValue")
        }
    }


    fun logAdLoaded(context: Context, adSource: String) {

        tryCatch {

            initializeAnalytics(context.applicationContext)

            val cleanedValue = "ad_loaded_$adSource"
                .trim()
                .lowercase()
                .replace("[^a-z0-9_]".toRegex(), "_")
                .take(40)

            val bundle = Bundle().apply {
                putString("ad_source", cleanedValue)
            }

            Log.i("opportunity_event", "ad_loaded: $cleanedValue")

            myFirebaseAnalytics?.logEvent("ad_loaded", bundle)
        }
    }

    fun logAdFailedToLoad(context: Context, adType: String) {

        tryCatch {

            initializeAnalytics(context.applicationContext)

            val cleanedValue = "ad_failed_$adType"
                .trim()
                .lowercase()
                .replace("[^a-z0-9_]".toRegex(), "_")
                .take(40)

            val bundle = Bundle().apply {
                putString("ad_failed_to_load", cleanedValue)
            }

            Log.i("opportunity_event", "ad_failed_to_load: $cleanedValue")

            myFirebaseAnalytics?.logEvent("ad_failed_to_load", bundle)
        }
    }

    fun logAdImpressionOpportunity(context: Context, placement: String) {

        tryCatch {

            initializeAnalytics(context.applicationContext)

            val cleanedValue = placement
                .trim()
                .lowercase()
                .replace("[^a-z0-9_]".toRegex(), "_")
                .take(40)

            val bundle = Bundle().apply {
                putString("placement", cleanedValue)
            }

            Log.i("opportunity_event", "opportunity: $placement")

            myFirebaseAnalytics?.logEvent(placement, bundle)
        }
    }
}