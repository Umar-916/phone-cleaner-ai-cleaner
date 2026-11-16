package com.phonecleaner.aicleaner.core.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.funsol.iap.billing.FunSolBillingHelper
import com.funsol.iap.billing.listeners.BillingListener
import com.funsol.iap.billing.model.ErrorType
import com.funsol.iap.billing.model.FunsolPurchase
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.Constants.commonCoroutineExceptionHandler
import com.phonecleaner.aicleaner.core.common.DataShare
import com.phonecleaner.aicleaner.core.common.tryCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

const val USER_PREMIUM = DataShare.USER_PREMIUM
const val weeklyProductID = DataShare.WEEKLY_PRODUCT_ID
const val weeklyBasePlanKey = DataShare.WEEKLY_BASE_PLAN_KEY
const val weeklyTrialOfferKey = DataShare.WEEKLY_TRIAL_OFFER_KEY

private const val TAG = "billing"

@SuppressLint("StaticFieldLeak")
object BillingUtilsIAP {

    val isPurchased = MutableStateFlow(false)

    @JvmField
    var isPremium: MutableLiveData<Boolean> = MutableLiveData(false)


    var billingHelper: FunSolBillingHelper? = null

    var isPurchaseFlowActive = false

    suspend fun initializeBilling(appContext: Context) {

        withContext(Dispatchers.IO) {

            val sharedPreferences = MyPreferencesForPremium(context = appContext)

            val premium = sharedPreferences.getFromPref(USER_PREMIUM, false)
            DataShare.isPremium = premium
            isPurchased.value = premium
            isPremium.postValue(premium)
        }
    }

    fun setUpConnection(activity: Activity) {

        Log.i(TAG, "setUpConnection: ")

        Constants.myAppCoroutineScope.launch(commonCoroutineExceptionHandler) {

            ensureActive()

            val sharedPreferences = MyPreferencesForPremium(context = activity)

            val cachedPremium = sharedPreferences.getFromPref(USER_PREMIUM, false)
            DataShare.isPremium = cachedPremium
            isPurchased.value = cachedPremium
            isPremium.postValue(cachedPremium)


            billingHelper = FunSolBillingHelper(activity)
                .setSubProductIds(
                    mutableListOf(
                        weeklyProductID
                    )
                )
                .enableLogging(isEnableLog = true)

                .setBillingListener(object : BillingListener {
                    override fun onClientReady() {

                        Constants.myAppCoroutineScope.launch(commonCoroutineExceptionHandler + Dispatchers.Main) {

                            ensureActive()

                            Log.i(TAG, "onClientReady: ")

                            delay(1000L.milliseconds)

                            val premiumCheck = FunSolBillingHelper(activity).isPremiumUser
                            sharedPreferences.saveToPref(USER_PREMIUM, premiumCheck)
                            DataShare.isPremium = premiumCheck
                            isPurchased.value = premiumCheck
                            isPremium.postValue(premiumCheck)

                            Log.i(TAG, "setUpConnection: User is Premium: $premiumCheck")

                            tryCatch {
                                Log.i(TAG, "allPrices: ${billingHelper?.getAllProductPrices()}")
                            }
                        }
                    }

                    override fun onClientInitError() {
                        Log.i(TAG, "onClientInitError: ")
                    }

                    override fun onProductsPurchased(purchases: List<FunsolPurchase?>) {
                        tryCatch {
                            isPurchaseFlowActive = false
                            sharedPreferences.saveToPref(USER_PREMIUM, true)
                            DataShare.isPremium = true
                            isPurchased.value = true
                            isPremium.postValue(true)
                        }
                    }

                    override fun onPurchaseAcknowledged(purchase: FunsolPurchase) {
                        tryCatch {
                            isPurchaseFlowActive = false
                            sharedPreferences.saveToPref(USER_PREMIUM, true)
                            DataShare.isPremium = true
                            isPurchased.value = true
                            isPremium.postValue(true)
                        }
                    }

                    override fun onPurchaseConsumed(purchase: FunsolPurchase) {}

                    override fun onBillingError(error: ErrorType) {
                        when (error) {
                            ErrorType.CLIENT_NOT_READY -> {
                                Log.d(TAG, "onBillingError: CLIENT_NOT_READY")
                            }

                            ErrorType.CLIENT_DISCONNECTED -> {
                                Log.d(TAG, "onBillingError: CLIENT_DISCONNECTED")
                            }

                            ErrorType.PRODUCT_NOT_EXIST -> {
                                Log.d(TAG, "onBillingError: PRODUCT_NOT_EXIST")
                            }

                            ErrorType.BILLING_ERROR -> {
                                Log.d(TAG, "onBillingError: BILLING_ERROR")
                            }

                            ErrorType.USER_CANCELED -> {
                                isPurchaseFlowActive = false
                                Log.d(TAG, "onBillingError: USER_CANCELED")
                            }

                            ErrorType.SERVICE_UNAVAILABLE -> {
                                Log.d(TAG, "onBillingError: SERVICE_UNAVAILABLE")
                            }

                            ErrorType.BILLING_UNAVAILABLE -> {
                                Log.d(TAG, "onBillingError: BILLING_UNAVAILABLE")
                            }

                            ErrorType.ITEM_UNAVAILABLE -> {
                                Log.d(TAG, "onBillingError: ITEM_UNAVAILABLE")
                            }

                            ErrorType.DEVELOPER_ERROR -> {
                                Log.d(TAG, "onBillingError: DEVELOPER_ERROR")
                            }

                            ErrorType.ERROR -> {
                                Log.d(TAG, "onBillingError: ERROR")
                            }

                            ErrorType.ITEM_ALREADY_OWNED -> {
                                Log.d(TAG, "onBillingError: ITEM_ALREADY_OWNED")
                            }

                            ErrorType.ITEM_NOT_OWNED -> {
                                Log.d(TAG, "onBillingError: ITEM_NOT_OWNED")
                            }

                            ErrorType.SERVICE_DISCONNECTED -> {
                                Log.d(TAG, "onBillingError: SERVICE_DISCONNECTED")
                            }

                            ErrorType.ACKNOWLEDGE_ERROR -> {
                                Log.d(TAG, "onBillingError: ACKNOWLEDGE_ERROR")
                            }

                            ErrorType.ACKNOWLEDGE_WARNING -> {
                                Log.d(TAG, "onBillingError: ACKNOWLEDGE_WARNING")
                            }

                            ErrorType.OLD_PURCHASE_TOKEN_NOT_FOUND -> {
                                Log.d(TAG, "onBillingError: OLD_PURCHASE_TOKEN_NOT_FOUND")
                            }

                            else -> {

                            }
                        }
                    }
                })

            billingHelper?.initialize()

        }
    }


    fun getWeeklyPrice(): String {
        return billingHelper?.getSubscriptionProductPriceById(
            weeklyBasePlanKey
        )?.price ?: ""
    }


    fun isWeeklyTrialActive(activity: Activity?): Boolean {
        return try {
            activity?.let { currentActivity ->
                FunSolBillingHelper(currentActivity).isSubscriptionOfferAvailable(
                    weeklyBasePlanKey,
                    weeklyTrialOfferKey
                )
            } ?: false
        } catch (_: Exception) {
            false
        } catch (_: java.lang.Exception) {
            false
        }
    }


    fun subscribeWeekly(activity: Activity, withTrial: Boolean = false): Boolean {
        return try {
            if (withTrial) {
                billingHelper?.subscribe(activity, weeklyBasePlanKey, weeklyTrialOfferKey)
            } else {
                billingHelper?.subscribe(activity, weeklyBasePlanKey)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Subscription failed: ${e.message}")
            false
        }
    }

    fun subscribeWeeklyTrial(activity: Activity): Boolean {
        return subscribeWeekly(activity, withTrial = true)
    }
}
