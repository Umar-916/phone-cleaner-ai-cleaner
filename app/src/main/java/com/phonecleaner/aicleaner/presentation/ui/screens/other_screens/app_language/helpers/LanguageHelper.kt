package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import android.view.View
import java.util.Locale
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageHelper {

    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE_CODE = "selected_language_code"

    // Locale currently applied to the UI. Language changes during onboarding don't recreate the
    // activity, so Compose reads the layout direction from here instead of the initial configuration.
    private val _appliedLocale = MutableStateFlow(Locale.getDefault())
    val appliedLocale = _appliedLocale.asStateFlow()

    fun isRtl(locale: Locale): Boolean =
        TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL

    fun getSavedLanguageCode(context: Context): String {
        val stored = prefs(context)
            .getString(KEY_LANGUAGE_CODE, LanguageCatalog.DEFAULT_LANGUAGE_CODE)
            ?: LanguageCatalog.DEFAULT_LANGUAGE_CODE
        return LanguageCatalog.normalizeCode(stored)
    }

    fun persistLanguageCode(context: Context, languageCode: String) {
        val normalized = LanguageCatalog.normalizeCode(languageCode)
        prefs(context).edit(commit = true) {
            putString(KEY_LANGUAGE_CODE, normalized)
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun wrapContext(base: Context): Context {
        return applyLocaleToContext(base, getSavedLanguageCode(base))
    }

    fun applyLanguage(
        context: Context,
        languageCode: String,
        recreateActivity: Boolean = true
    ) {
        val normalized = LanguageCatalog.normalizeCode(languageCode)
        persistLanguageCode(context, normalized)
        val locale = localeFor(normalized)
        Locale.setDefault(locale)
        _appliedLocale.value = locale

        val activity = context.findActivity()
        if (activity != null) {
            applyLocaleToResources(activity, locale)
        }

        if (!recreateActivity) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.applicationContext
                .getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(normalized)
        }

        activity?.recreate()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun applyLocaleToContext(base: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        Locale.setDefault(locale)
        _appliedLocale.value = locale
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        }
        return base.createConfigurationContext(configuration)
    }

    @SuppressLint("ObsoleteSdkInt")
    @Suppress("DEPRECATION")
    private fun applyLocaleToResources(context: Context, locale: Locale) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        }
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    private fun localeFor(languageCode: String): Locale {
        return Locale.forLanguageTag(languageCode)
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
