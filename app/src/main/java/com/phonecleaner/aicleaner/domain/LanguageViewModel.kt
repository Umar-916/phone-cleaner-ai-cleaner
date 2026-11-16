package com.phonecleaner.aicleaner.domain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.core.common.Constants.homeScreenReachedForCurrentSession
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageCatalog
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageHelper
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanguageViewModel(
    private val appDataStore: AppDataStore,
    appContext: Context
) : ViewModel() {

    val languages: List<LanguageItem> = LanguageCatalog.languages

    private val _savedLanguageCode = MutableStateFlow(
        LanguageHelper.getSavedLanguageCode(appContext)
    )
    val savedLanguageCode = _savedLanguageCode.asStateFlow()

    private val _pendingLanguageCode = MutableStateFlow(_savedLanguageCode.value)
    val pendingLanguageCode = _pendingLanguageCode.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = LanguageCatalog.normalizeCode(appDataStore.languageCodeFlow.first())
            val applied = LanguageCatalog.normalizeCode(_savedLanguageCode.value)
            val resolved = if (applied != LanguageCatalog.DEFAULT_LANGUAGE_CODE) applied else stored
            _savedLanguageCode.value = resolved
            _pendingLanguageCode.value = resolved
        }
    }

    fun selectLanguage(code: String) {
        _pendingLanguageCode.value = code
    }

    fun confirmSelection(context: Context, onApplied: () -> Unit) {
        val code = LanguageCatalog.normalizeCode(_pendingLanguageCode.value)
        viewModelScope.launch {
            appDataStore.saveLanguageCode(code)
            LanguageHelper.persistLanguageCode(context, code)
            val languageChanged = code != _savedLanguageCode.value
            _savedLanguageCode.value = code
            withContext(Dispatchers.Main) {
                onApplied()
                if (!languageChanged) {
                    return@withContext
                }
                LanguageHelper.applyLanguage(
                    context = context,
                    languageCode = code,
                    recreateActivity = homeScreenReachedForCurrentSession.value
                )
            }
        }
    }
}
