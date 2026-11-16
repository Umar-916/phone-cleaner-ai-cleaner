package com.phonecleaner.aicleaner.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class AppDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        private val SELECTED_CATEGORY = stringPreferencesKey("selected_category")

        private val THEME_KEY = stringPreferencesKey("theme_key")
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")
        private val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")

        /** In-App Data */
        val PREMIUM_USER = booleanPreferencesKey("premium_user")
        private val NOTIFICATION_PERMISSION_REQUESTED =
            booleanPreferencesKey("notification_permission_requested")
        private val NOTIFICATION_PERMISSION_REQUEST_COUNT =
            stringPreferencesKey("notification_permission_request_count")

        private val HOME_SCREEN_COUNT = intPreferencesKey("home_screen_count")

        /** "Create Custom Sound" hint above the bottom bar button: shown until it (or the button) is tapped once. */

        private val SPLASH_SCREEN_COUNT = intPreferencesKey("splash_screen_count")

        private val DAY_COUNT_ANALYTICS_SENT = booleanPreferencesKey("day_count_analytics_sent")
        private val DAYS_COUNT = intPreferencesKey("days_count")
        private val PREVIOUS_APP_OPEN_DATE = longPreferencesKey("previous_app_open_date")

    }


    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }


    suspend fun savePremiumUser(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREMIUM_USER] = isPremium
        }
    }

    // Notification Permission Management

    suspend fun setNotificationPermissionRequested(requested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUESTED] = requested
        }
    }

    // Notification Permission Request Count Management

    suspend fun incrementNotificationPermissionRequestCount() {
        context.dataStore.edit { preferences ->
            val currentCount =
                preferences[NOTIFICATION_PERMISSION_REQUEST_COUNT]?.toIntOrNull() ?: 0
            preferences[NOTIFICATION_PERMISSION_REQUEST_COUNT] = (currentCount + 1).toString()
        }
    }

    suspend fun resetNotificationPermissionRequestCount() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_REQUEST_COUNT] = "0"
        }
    }

    val selectedCategory: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[SELECTED_CATEGORY] ?: "" }

    suspend fun saveSelectedCategory(category: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CATEGORY] = category
        }
    }


    val themeKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "system" }

    suspend fun saveThemeKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = key
        }
    }

    val showHiddenFilesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[SHOW_HIDDEN_FILES] ?: false }

    suspend fun setShowHiddenFiles(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_HIDDEN_FILES] = show }
    }

    val languageCodeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[LANGUAGE_CODE] ?: "en-US" }

    suspend fun saveLanguageCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = code
        }
    }



    fun getCountForHome(): Flow<Int> = context.dataStore.data.map { it[HOME_SCREEN_COUNT] ?: 0 }

    suspend fun incrementCountForHome() {
        context.dataStore.edit { prefs ->
            prefs[HOME_SCREEN_COUNT] = (prefs[HOME_SCREEN_COUNT] ?: 0) + 1
        }
    }

    suspend fun incrementCountForSplash(): Int {
        var newCount = 0
        context.dataStore.edit { prefs ->
            newCount = (prefs[SPLASH_SCREEN_COUNT] ?: 0) + 1
            prefs[SPLASH_SCREEN_COUNT] = newCount
        }
        return newCount
    }


    suspend fun dayCount(onAnalytics: suspend (String) -> Unit) {
        var shouldSendAnalytics = false
        var dayCount = 1

        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                val previousDate = prefs[PREVIOUS_APP_OPEN_DATE] ?: System.currentTimeMillis()

                if (!areSameDate(previousDate)) {
                    val previousDayCount = prefs[DAYS_COUNT] ?: 1
                    prefs[DAYS_COUNT] = previousDayCount + 1
                    prefs[DAY_COUNT_ANALYTICS_SENT] = false
                }

                prefs[PREVIOUS_APP_OPEN_DATE] = System.currentTimeMillis()

                if (prefs[DAY_COUNT_ANALYTICS_SENT] != true) {
                    dayCount = prefs[DAYS_COUNT] ?: 1
                    shouldSendAnalytics = true
                    prefs[DAY_COUNT_ANALYTICS_SENT] = true
                }
            }
        }

        if (shouldSendAnalytics) {
            onAnalytics("Day_$dayCount")
        }
    }

    private fun areSameDate(previousTime: Long): Boolean {
        return try {
            val previousDate = Date(previousTime)
            val currentDate = Date(System.currentTimeMillis())

            val calendar1 = Calendar.getInstance().apply {
                time = previousDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calendar2 = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            (calendar1 == calendar2)
        } catch (_: Exception) {
            true
        } catch (_: java.lang.Exception) {
            true
        }
    }

}
