package com.phonecleaner.aicleaner.core.advertisement.taichi

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.phonecleaner.aicleaner.core.common.DataShare.PREF_KEY
import com.phonecleaner.aicleaner.core.common.tryCatch


/*** Preference ***/
private var preference: SharedPreferences? = null

private fun Activity.initializeSharedPreferencesIfRequired(){
    tryCatch {
        if (preference == null) {
            preference = this.applicationContext.getSharedPreferences(PREF_KEY, MODE_PRIVATE)
        }
    }
}

fun Activity.saveFloatToSharedPreferences(key: String, value: Float) {
    tryCatch {
        initializeSharedPreferencesIfRequired()
        preference?.let{
            it.edit()?.putFloat(key, value)?.apply()
        }
    }
}

// Get float
fun Activity.getFloatFromSharedPreferences(key: String, defaultValue: Float = 0f): Float {
    var value = defaultValue
    tryCatch {
        initializeSharedPreferencesIfRequired()
        value = preference?.getFloat(key, defaultValue) ?: defaultValue
    }
    return value
}



fun Activity.saveIntToSharedPreferences(key: String, value: Int) {
    tryCatch {
        initializeSharedPreferencesIfRequired()
        preference?.let{
            it.edit()?.putInt(key, value)?.apply()
        }
    }
}

// Get float
fun Activity.getIntFromSharedPreferences(key: String, defaultValue: Int = 0): Int {
    var value = defaultValue
    tryCatch {
        initializeSharedPreferencesIfRequired()
        value = preference?.getInt(key, defaultValue) ?: defaultValue
    }
    return value
}