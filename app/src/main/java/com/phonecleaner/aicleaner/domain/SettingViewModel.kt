package com.phonecleaner.aicleaner.domain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.presentation.dialogues.theme.ThemeKeys
import com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers.LanguageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(
    private val appDataStore: AppDataStore,
    private val appContext: Context
) : ViewModel() {

    val themeKey = appDataStore.themeKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeKeys.SYSTEM)

    val languageCode = appDataStore.languageCodeFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LanguageHelper.getSavedLanguageCode(appContext)
        )

    val showHiddenFiles = appDataStore.showHiddenFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setShowHiddenFiles(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { appDataStore.setShowHiddenFiles(show) }
    }

    private val _showRateUsDialog =MutableStateFlow(false)
    val showRateUsDialog = _showRateUsDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog = _showThemeDialog.asStateFlow()

    fun openRateUsDialog() {
        _showThemeDialog.value = false
        _showRateUsDialog.value = true
    }

    fun dismissRateUsDialog() {
        _showRateUsDialog.value = false
    }

    fun openThemeDialog() {
        _showRateUsDialog.value = false
        _showThemeDialog.value = true
    }

    fun dismissThemeDialog() {
        _showThemeDialog.value = false
    }

    fun applyTheme(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.saveThemeKey(key)
        }
        _showThemeDialog.value = false
    }
}
