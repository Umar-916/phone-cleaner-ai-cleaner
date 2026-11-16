package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

data class LanguageItem(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagCode: String
) {
    val displayLabel: String
        get() = if (nativeName.isBlank() || nativeName.equals(name, ignoreCase = true)) {
            name
        } else {
            "$name ($nativeName)"
        }
}
