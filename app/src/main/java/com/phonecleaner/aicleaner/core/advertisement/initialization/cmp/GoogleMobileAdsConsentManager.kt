package com.phonecleaner.aicleaner.core.advertisement.initialization.cmp

import android.app.Activity
import android.content.Context
import com.phonecleaner.aicleaner.core.common.debugRelease
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */

class GoogleMobileAdsConsentManager(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */

    /*
    * Tag: "UserMessagingPlatform"
    * For Getting Device ID
    * */


    fun gatherConsent(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {

        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.

        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                /*.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("1862A91A4087D13A53358602209E835D")*/
                .build()

        val params =
            ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

        // Requesting an update to consent information should be called on every app launch.

        "A".debugRelease("ConsentTesting")

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                "B".debugRelease("ConsentTesting", "loadAndShowConsentFormIfRequired")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->

                    "$formError".debugRelease("ConsentTesting", "loadAndShowConsentFormIfRequired")
                    "${formError?.message} -- ${formError?.errorCode}".debugRelease(
                        "ConsentTesting",
                        "loadAndShowConsentFormIfRequired"
                    )

                    consentInformation.canRequestAds()
                        .debugRelease("ConsentTesting", "canRequestAds SuccessListener")

                    // Consent has been gathered.
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                }
            },
            { requestConsentError ->

                consentInformation.canRequestAds()
                    .debugRelease("ConsentTesting", "canRequestAds FailureListener")
                "${requestConsentError}".debugRelease("ConsentTesting", "loadAndShowConsentFormIfRequired")
                "${requestConsentError.message} -- ${requestConsentError.errorCode}".debugRelease(
                    "ConsentTesting",
                    "loadAndShowConsentFormIfRequired"
                )

                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            }
        )

        consentInformation.canRequestAds()
            .debugRelease("ConsentTesting", "canRequestAds After requestConsentInfoUpdate")
    }

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
    }
}

fun Context.canShowAds(): Boolean {

    /** Helper variable to determine if the app can request ads. */

    return UserMessagingPlatform.getConsentInformation(this).canRequestAds()
}

fun Activity.privacyOptionRequired(): Boolean {

    /** Helper variable to determine if the privacy options form is required. */

    val isPrivacyOptionsRequired =
        UserMessagingPlatform.getConsentInformation(this).privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    return isPrivacyOptionsRequired
}

fun Activity.showPrivacyOptionSheet(callback: () -> Unit) {

    /** Helper method to call the UMP SDK method to show the privacy options form. */

    UserMessagingPlatform.showPrivacyOptionsForm(this) {

        it.debugRelease("ConsentTesting", "showPrivacyOptionsForm Dismiss")
        it?.message.debugRelease("ConsentTesting", "showPrivacyOptionsForm Dismiss")
        it?.errorCode.debugRelease("ConsentTesting", "showPrivacyOptionsForm Dismiss")
        callback.invoke()
    }

}