package com.phonecleaner.aicleaner.core.common

object DataShare {
    const val PREF_KEY = Constants.PREF_KEY
    const val USER_PREMIUM = Constants.USER_PREMIUM
    const val WEEKLY_PRODUCT_ID = Constants.WEEKLY_PRODUCT_ID
    const val WEEKLY_BASE_PLAN_KEY = Constants.WEEKLY_SUBSCRIPTION
    const val WEEKLY_TRIAL_OFFER_KEY = Constants.WEEKLY_TRIAL_SUBSCRIPTION

    var isPremium: Boolean = false

    var startTimerSeconds: Int = 0
    var endTimerSeconds: Int = 0

    fun hasTimerValues(): Boolean = startTimerSeconds > 0 || endTimerSeconds > 0

    /** End time set, start left at Off (0). */
    fun isEndOnlyTimer(): Boolean = startTimerSeconds == 0 && endTimerSeconds > 0

    fun setTimers(startSec: Int, endSec: Int) {
        startTimerSeconds = startSec
        endTimerSeconds = endSec
    }

    fun clearTimers() {
        startTimerSeconds = 0
        endTimerSeconds = 0
    }
}
