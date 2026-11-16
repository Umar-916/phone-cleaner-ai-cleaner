package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity

import androidx.annotation.Keep

@Keep
data class OnboardingFlowConfig(
    val screens: List<RemoteScreenConfig> = emptyList()
)
