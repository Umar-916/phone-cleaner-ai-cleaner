package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper

import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingFlowConfig
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingScreen
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.RemoteScreenConfig

class OnboardingBackupFlow {

    companion object {
        val defaultBackUp = OnboardingFlowConfig(
            listOf(
                RemoteScreenConfig(id = OnboardingScreen.SPLASH_INTERSTITIAL.name, enabled = true),
                RemoteScreenConfig(id = OnboardingScreen.LANGUAGE.name, enabled = true),
                RemoteScreenConfig(id = OnboardingScreen.PREMIUM.name, enabled = true, screenType = 7)
            )
        )
    }

    var flowFirstRunBackupConfig: OnboardingFlowConfig? = OnboardingFlowConfig(
        listOf(
            RemoteScreenConfig(id = OnboardingScreen.SPLASH_INTERSTITIAL.name, enabled = false),
            RemoteScreenConfig(id = OnboardingScreen.LANGUAGE.name, enabled = true),
            RemoteScreenConfig(id = OnboardingScreen.ONBOARDING.name, enabled = true),
            RemoteScreenConfig(id = OnboardingScreen.PREMIUM.name, enabled = true, screenType = 7)
        )
    )

    var flowSecondRunAndOnwardsBackupConfig: OnboardingFlowConfig? = OnboardingFlowConfig(
        listOf(
            RemoteScreenConfig(id = OnboardingScreen.SPLASH_INTERSTITIAL.name, enabled = true),
            RemoteScreenConfig(id = OnboardingScreen.PREMIUM.name, enabled = true, screenType = 7)
        )
    )

    fun getBackupFlow(currentRunCount: Int): OnboardingFlowConfig {
        return when (currentRunCount) {
            1 -> flowFirstRunBackupConfig
            else -> flowSecondRunAndOnwardsBackupConfig
        } ?: defaultBackUp
    }

    fun clear() {
        flowFirstRunBackupConfig = null
        flowSecondRunAndOnwardsBackupConfig = null
    }
}
