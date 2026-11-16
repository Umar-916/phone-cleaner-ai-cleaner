package com.phonecleaner.aicleaner.core.billing

import android.content.Context
import androidx.core.content.edit
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.Constants.commonCoroutineExceptionHandler
import com.phonecleaner.aicleaner.core.common.DataShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class MyPreferencesForPremium(val context: Context) {

    inline fun <reified T> saveToPref(key: String, value: T?) {

        Constants.myAppCoroutineScope.launch(Dispatchers.IO + commonCoroutineExceptionHandler) {

            ensureActive()

            context.getSharedPreferences(DataShare.PREF_KEY, Context.MODE_PRIVATE).edit {
                when (value) {
                    is String -> putString(key, value).apply()
                    is Int -> putInt(key, value).apply()
                    is Long -> putLong(key, value).apply()
                    is Float -> putFloat(key, value).apply()
                    is Boolean -> putBoolean(key, value).apply()
                    else -> throw IllegalArgumentException("Unsupported type")
                }
            }
            if (key == DataShare.USER_PREMIUM && value is Boolean) {
                DataShare.isPremium = value
            }
        }

    }

    inline fun <reified T> getFromPref(key: String, default: T): T {
        context.getSharedPreferences(DataShare.PREF_KEY, Context.MODE_PRIVATE).apply {
            return when (default) {
                is String -> getString(key, default) as T
                is Int -> getInt(key, default) as T
                is Long -> getLong(key, default) as T
                is Float -> getFloat(key, default) as T
                is Boolean -> getBoolean(key, default) as T
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

}