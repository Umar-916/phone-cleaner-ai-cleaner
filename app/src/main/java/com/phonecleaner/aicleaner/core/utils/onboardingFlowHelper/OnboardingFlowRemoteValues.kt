package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper

object OnboardingFlowRemoteValues {

    var flowFirstRun: String? = ""
    var flowSecondRunAndOnwards: String? = ""

    fun getSelectedFlowJson(currentRunCount: Int): String {
        return when (currentRunCount) {
            1 -> flowFirstRun
            else -> flowSecondRunAndOnwards
        } ?: ""
    }

    fun clear() {
        flowFirstRun = null
        flowSecondRunAndOnwards = null
    }
}
