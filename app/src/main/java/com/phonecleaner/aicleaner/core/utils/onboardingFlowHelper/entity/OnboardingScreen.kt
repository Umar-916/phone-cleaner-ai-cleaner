package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity

enum class OnboardingScreen {
    SPLASH_INTERSTITIAL,
    LANGUAGE,
    PREMIUM;

    companion object {
        @JvmStatic
        fun fromId(id: String?): OnboardingScreen? {
            val normalized = id?.trim()?.uppercase().orEmpty()
            if (normalized.isEmpty()) return null
            return entries.firstOrNull { it.name == normalized }
        }
    }
}
