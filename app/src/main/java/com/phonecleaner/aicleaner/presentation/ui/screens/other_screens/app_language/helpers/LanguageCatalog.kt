package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

object LanguageCatalog {

    const val DEFAULT_LANGUAGE_CODE = "en-US"

    val languages: List<LanguageItem> = listOf(
        LanguageItem("en-US", "English (US)", "", "us"),
        LanguageItem("en-GB", "English (UK)", "", "gb"),
        LanguageItem("en-IN", "English (India)", "", "in"),
        LanguageItem("ar", "Arabic", "العربية", "sa"),
        LanguageItem("bn", "Bangla", "বাংলা", "bd"),
        LanguageItem("zh-CN", "Chinese (Simplified)", "简体中文", "cn"),
        LanguageItem("zh-TW", "Chinese (Traditional)", "繁體中文", "tw"),
        LanguageItem("zh-HK", "Chinese (Hong Kong)", "繁體中文（香港）", "hk"),
        LanguageItem("fr-CA", "French (Canada)", "Français (Canada)", "ca"),
        LanguageItem("fr-FR", "French (France)", "Français (France)", "fr"),
        LanguageItem("de", "German", "Deutsch", "de"),
        LanguageItem("el", "Greek", "Ελληνικά", "gr"),
        LanguageItem("he", "Hebrew", "עברית", "il"),
        LanguageItem("hi", "Hindi", "हिंदी", "in"),
        LanguageItem("id", "Indonesian", "Bahasa Indonesia", "id"),
        LanguageItem("it", "Italian", "Italiano", "it"),
        LanguageItem("ja", "Japanese", "日本語", "jp"),
        LanguageItem("ko", "Korean", "한국어", "kr"),
        LanguageItem("ms", "Malay", "Bahasa Melayu", "my"),
        LanguageItem("fa", "Persian", "فارسی", "ir"),
        LanguageItem("pt-BR", "Portuguese (Brazil)", "Português (Brasil)", "br"),
        LanguageItem("pt-PT", "Portuguese (Portugal)", "Português (Portugal)", "pt"),
        LanguageItem("ru", "Russian", "Русский", "ru"),
        LanguageItem("es-419", "Spanish (Latin America)", "Español (Latinoamérica)", "mx"),
        LanguageItem("es-ES", "Spanish (Spain)", "Español (España)", "es"),
        LanguageItem("es-US", "Spanish (US)", "Español (EE. UU.)", "us"),
        LanguageItem("ta", "Tamil", "தமிழ்", "in"),
        LanguageItem("th", "Thai", "ไทย", "th"),
        LanguageItem("tr", "Turkish", "Türkçe", "tr"),
        LanguageItem("uk", "Ukrainian", "Українська", "ua"),
        LanguageItem("ur", "Urdu", "اردو", "pk"),
        LanguageItem("vi", "Vietnamese", "Tiếng Việt", "vn")
    )

    fun normalizeCode(code: String): String {
        val mapped = when (code) {
            "en" -> "en-US"
            "pt" -> "pt-BR"
            "fr" -> "fr-FR"
            "zh" -> "zh-CN"
            "es" -> "es-419"
            "in" -> "id"
            "iw" -> "he"
            "fil" -> DEFAULT_LANGUAGE_CODE
            else -> code
        }
        return languages.firstOrNull { it.code.equals(mapped, ignoreCase = true) }?.code
            ?: DEFAULT_LANGUAGE_CODE
    }

    fun findByCode(code: String): LanguageItem {
        val normalized = normalizeCode(code)
        return languages.first { it.code == normalized }
    }

    fun displayName(code: String): String = findByCode(code).name
}
