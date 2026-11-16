package com.phonecleaner.aicleaner.core.advertisement.app_flyer


import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.adnetworks.generic.Scheme.AD_TYPE
import com.appsflyer.adrevenue.adnetworks.generic.Scheme.AD_UNIT
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AFInAppEventParameterName
import com.appsflyer.share.MediationNetwork
import com.phonecleaner.aicleaner.core.common.tryCatch


object AppFlyer {
    var TAG = "AppFlyer"

    fun logAdRevenue(
        adType: String,
        currency: Any,
        revenue: Double,
        networkName: String,
        mediationNetwork: MediationNetwork,
        adID: String
    ) {

        tryCatch {

            val adRevenueData = AFAdRevenueData(
                networkName,  // monetizationNetwork
                mediationNetwork,  // mediationNetwork
                currency.toString(),  // currencyIso4217Code
                revenue // revenue
            )

            val additionalParameters: MutableMap<String, Any> = HashMap()

            additionalParameters[AD_TYPE] = adType
            additionalParameters[AD_UNIT] = adID
            additionalParameters[AFInAppEventParameterName.AD_REVENUE_NETWORK_NAME] = networkName
            additionalParameters[AFInAppEventParameterName.REVENUE] = revenue
            additionalParameters[AFInAppEventParameterName.CURRENCY] = currency

            AppsFlyerLib.getInstance().logAdRevenue(
                adRevenueData, additionalParameters
            )

            Log.i("Apps_flyer_tag", "Event Sent $revenue")
        }
    }
}