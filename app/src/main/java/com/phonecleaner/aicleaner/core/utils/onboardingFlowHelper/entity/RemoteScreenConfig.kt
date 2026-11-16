package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity

import androidx.annotation.Keep

@Keep
data class RemoteScreenConfig(
    val id: String? = null,
    val enabled: Boolean = true,
    val screenType: Int? = null
)
