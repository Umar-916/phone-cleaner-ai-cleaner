package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.phonecleaner.aicleaner.core.common.DataShare.PREF_KEY

class OnboardingPreferenceHelper {

    private var preferences: SharedPreferences? = null
    private val runCount = "onboarding_run_count"

    private fun getPreference(context: Context): SharedPreferences? {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_KEY, MODE_PRIVATE)
        }
        return preferences
    }

    fun getCurrentRunCount(context: Context): Int {
        return getPreference(context)?.getInt(runCount, 1) ?: 1
    }

    fun incrementRunCount(context: Context) {
        getPreference(context)?.edit {
            putInt(runCount, getCurrentRunCount(context) + 1)
        }
    }

    fun clear() {
        preferences = null
    }
}
