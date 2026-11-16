package com.phonecleaner.aicleaner.core.advertisement.initialization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.api.PurchaseClient
import com.appsflyer.api.Store
import com.appsflyer.internal.models.InAppPurchaseValidationResult
import com.appsflyer.internal.models.SubscriptionValidationResult
import com.appsflyer.share.attribution.AppsFlyerRequestListener
import com.funsol.fcm.FunFCM
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.firebase.FirebaseApp
import com.phonecleaner.aicleaner.BuildConfig
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.advertisement.initialization.cmp.GoogleMobileAdsConsentManager
import com.phonecleaner.aicleaner.core.advertisement.taichi.CurrencyConverter
import com.phonecleaner.aicleaner.core.billing.BillingUtilsIAP
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.common.isNetworkAvailable
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.core.remote.RemoteConfig
import com.phonecleaner.aicleaner.application.MyApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

interface AppInitializationListener {

    fun onInitializationCompleted()

    fun showIndeterminateProgress()

    /** Remote config fetch finished (success, failure, timeout or skipped offline). */
    fun onRemoteConfigFetched() {}
}

val initTag = "initializeApp"

object InitializationHelper {

    /*** Coroutine Scope & Exception Handler ***/

    private val commonCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("CoroutineException", "AppInitialization", throwable)
    }

    private val initializationScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + commonCoroutineExceptionHandler)

    /*** Flags ***/

    private var appInitialized = false

    private var appInitializationCompleted = false

    private var sdkInitialized = false

    /** True once the SDK finished initializing. It throws on ad requests before that, so canRequestAds() checks it. */
    @Volatile
    var mobileAdsInitialized = false

    /** MobileAds.initialize was already called in this process (completed or not); stops a second call. */
    @Volatile
    private var mobileAdsInitStarted = false

    /*var isRealInternetAvailable = false*/

    /** True once this process finished its remote config fetch step (even if it failed or timed out). */
    @Volatile
    var isRemoteFetchCompleted = false

    /*** Listener ***/

    private var appInitializationListener: AppInitializationListener? = null


    /*** Functions ***/


    /*** Setup Listener ***/

    fun setupInitializationListener(listener: AppInitializationListener?) {

        /**** IMPORTANT: Always Set The InitializationListener Before Calling initializeApp ****/
        /**** IMPORTANT: Set it null at onDestroyView ****/

        appInitializationListener = listener
    }

    /*** Reset ***/

    fun resetAll() {

        /**** IMPORTANT: Call From Application Class OnCreate (Only Once - Via Process Check) ****/

        appInitialized = false

        sdkInitialized = false
    }

    /*** Sequential Initialization ***/

    fun initializeApp(activity: Activity) {

        /**** IMPORTANT: Always Set The InitializationListener Before Calling this Function ****/

        if (!appInitialized) {

            appInitialized = true

            appInitializationCompleted = false

            mobileAdsInitialized = false

            "Initialization Started".debug(initTag)

            initializationScope.launch(commonCoroutineExceptionHandler + Dispatchers.Main) {

                ensureActive()

                activity.applicationContext?.let { appContext ->

                    appInitializationListener?.showIndeterminateProgress()

                    /***** Init Billing *****/

                    /*** Only Initialize the value from the Preference ***/

                    "Initializing Billing Preference".debug(initTag)

                    withContext(Dispatchers.IO) {

                        BillingUtilsIAP.initializeBilling(activity)

                        "Initializing Billing Preference Complete".debug(initTag)
                        "".debug(initTag)
                    }

                    "Initializing Firebase".debug(initTag)
                    initializeFirebase(appContext)
                    "Initializing Firebase Complete".debug(initTag)
                    "".debug(initTag)

                    "Fetching Consent".debug(initTag)

                    getConsent(activity) {

                        "Fetching Consent Complete".debug(initTag)
                        "".debug(initTag)

                        initializationScope.launch(commonCoroutineExceptionHandler + Dispatchers.IO) {

                            ensureActive()

                            /***** Billing *****/

                            "Fetching Billing (Background)".debug(initTag)
                            "".debug(initTag)

                            /***
                             * Refer to _root_ide_package_.com.example.medicalreportapp.billing.BillingUtilsIAP Code.
                             * Verify: Setup At IO. Init via Preference. Exception Handler.
                             * ***/

                            tryCatch {

                                /***
                                 * Important: Billing must be called once at max per session
                                 * To Avoid Concurrent Modification in billing library in rare case scenario
                                 * If setup connection is called more than once before FunSolBillingHelper.initialize()
                                 * was completed, the instead of onClientAllReadyConnected(), Concurrent Modification might
                                 * occur. Once FunSolBillingHelper.initialize() is completed, onClientAllReadyConnected()
                                 * will be called. Such scenario can occur if billing was called via Application Class can app
                                 * have Yandex Mediation or SDK causing multiple processes. Or if activity recreated.
                                 * Not an issue in our case as called via Splash Fragment and Flag already checks that this code
                                 * is called once.
                                 * ***/

                                callBilling(activity)
                            }

                            /***** Remote *****/

                            /***
                             * Refer to RemoteConfig Code. (Verify Callback in either case: Success or Failure)
                             * ***/

                            fetchRemote(activity) {

                                isRemoteFetchCompleted = true
                                tryCatch { appInitializationListener?.onRemoteConfigFetched() }

                                initializationScope.launch(commonCoroutineExceptionHandler + Dispatchers.IO) {

                                    ensureActive()

                                    /***** AppOpen  *****/

                                    initializeMobileAds(appContext) {

                                        afterMobileAdsInitialized(activity)
                                    }
                                }
                            }
                        }
                    }

                }
            }

        } else {

            if (appInitializationCompleted) {
                appInitializationListener?.onInitializationCompleted()
            }
        }
    }

    /**
     * The process was recreated straight onto an inner screen (restored back stack), so Splash never ran
     * [initializeApp]. Load the saved premium state and start Mobile Ads once so ads can be requested safely.
     */
    fun recoverIfSplashWasSkipped(activity: Activity, onAdsReady: () -> Unit) {
        if (appInitialized || mobileAdsInitStarted) return

        initializationScope.launch {
            tryCatch { BillingUtilsIAP.initializeBilling(activity) }
            initializeMobileAds(activity.applicationContext) {}
            // Ads requested while the SDK was starting were skipped; let the screen request them again.
            if (mobileAdsInitialized) {
                withContext(Dispatchers.Main) { onAdsReady() }
            }
        }
    }

    suspend fun callBilling(activity: Activity) {
        tryCatch {
            BillingUtilsIAP.setUpConnection(activity)
        }
    }

    private fun afterMobileAdsInitialized(activity: Activity) {

        initializationScope.launch(Dispatchers.Main + commonCoroutineExceptionHandler) {

            ensureActive()

            /***** App Open Manager *****/

            "Initializing App Open Class".debug(initTag)

            /***
             * Refer to MyApplication Code.
             * AppOpen Manager Need to be Initialized only once.
             * In case of App with service whose process exit, in those
             * case application class wont be called again.
             * Thus without check multiple AppOpenManager can be
             * initialized causing multiple LifecycleObserver.
             * In our case a flag
             * ***/

            activity.application?.let { currentApplication ->
                if (currentApplication is MyApplication) {
                    currentApplication.initializeAppOpen()
                }
            }

            "Initializing App Open Class Complete".debug(initTag)
            "".debug(initTag)

            /***** Initialization Completed *****/

            "Initialization Completed".debug(initTag)

            /*****
             * Start The Splash Task such as
             * Splash Handler, Ad Calls, Force Update,
             * Heavy UI Operations like loading lottie at splash or loading some high
             * resolution image at splash etc...
             * Or Other Task (e.g in case of deeplink) in
             * the following interface callback onInitializationCompleted()
             * *****/

            appInitializationCompleted = true

            appInitializationListener?.onInitializationCompleted()

            withContext(Dispatchers.IO) {

                delay(4000L.milliseconds)

                /*****
                 * Any Task which is not required immediately at splash.
                 * Such as SDKs like OneSignal, AppFlyer etc...
                 * Or some Preload data, api call for functionality
                 * Call that in initializeSdksAndData()
                 * This will be called after a small delau of 4000L
                 * To ease out the immediate load at splash.
                 * ****/

                "Initializing SDKs and Data".debug(initTag)

                initializeSdksAndData(activity)
            }
        }
    }



    /*** Initialize SDKs And Data ***/

    private suspend fun initializeSdksAndData(activity: Activity) {

        /*****
         * Any Task which is not required immediately at splash.
         * Such as SDKs like OneSignal, AppFlyer etc...
         * Or some Preload data, api call for functionality
         * Call that in initializeSdksAndData()
         * This will be called after a small delau of 4000L
         * To ease out the immediate load at splash.
         * ****/

        if (!sdkInitialized) {

            sdkInitialized = true

            activity.applicationContext?.let { appContext ->

                tryCatch {

                    if (!BillingUtilsIAP.isPurchased.value) {

                        initializeTaiChi(appContext)

                        delay(250L.milliseconds)
                    }
                }

                tryCatch {
                    initializeFcm(appContext)
                }

                delay(250L.milliseconds)

                tryCatch {

                    if (!BillingUtilsIAP.isPurchased.value) {

                        initAppFlyer(appContext)

                        delay(250L.milliseconds)
                    }
                }
            }
        }
    }


    suspend fun initializeFcm(context: Context) {
        tryCatch {

            "Initializing FCM".debug(initTag)

            val fcmManager = FunFCM()
            fcmManager.setup(context, BuildConfig.APPLICATION_ID)
        }
    }


    /*** Mobile Ads ***/

    var maxMobileAdWaitTime = 7000L
    var maxRemoteConfigWaitTime = 7000L

    suspend fun initializeMobileAds(context: Context, callback: () -> Unit) {

        withContext(Dispatchers.IO) {

            if (!mobileAdsInitialized) {

                if (!BillingUtilsIAP.isPurchased.value/* && context.isNetworkAvailable()*/ /*&& isRealInternetAvailable*/) {

                    "Initializing Mobile Ads".debug(initTag)

                    // Thread-safe flag to ensure the callback is only executed once (either via SDK or timeout)
                    val isCallbackInvoked = AtomicBoolean(false)
                    // Bridge for the callback-based SDK to coroutines
                    val initDeferred = CompletableDeferred<Unit>()

                    val appId = context.getString(R.string.admob_id)
                    mobileAdsInitStarted = true
                    MobileAds.initialize(
                        context,
                        InitializationConfig.Builder(appId).build()
                    ) {

                        // Mark as initialized globally for the rest of the app
                        mobileAdsInitialized = true

                        // Resume the suspended coroutine below
                        initDeferred.complete(Unit)

                        // Invoke callback only if the 10s timeout hasn't already done so
                        if (isCallbackInvoked.compareAndSet(false, true)) {
                            "Initializing Mobile Ads Complete (on-time)".debug(initTag)
                            "".debug(initTag)
                            callback.invoke()
                        } else {
                            // This means the timeout triggered first, but we log the eventual success
                            "Initializing Mobile Ads Complete (late)".debug(initTag)
                            "".debug(initTag)
                        }
                    }

                    // Suspend the current coroutine for a maximum of 10 seconds.
                    // This waits for initDeferred.complete(Unit) to be called by the SDK.
                    // If 10s pass, it returns null and we move forward anyway.
                    withTimeoutOrNull(maxMobileAdWaitTime.milliseconds) {
                        initDeferred.await()
                    }

                    // If isCallbackInvoked is still false, it means we reached the 10s timeout.
                    // We trigger the callback now to unblock the Splash/Init flow.
                    if (isCallbackInvoked.compareAndSet(false, true)) {
                        "Mobile Ads Initialization Timeout (${maxMobileAdWaitTime}ms reached) - Proceeding".debug(
                            initTag
                        )
                        "".debug(initTag)
                        callback.invoke()
                    }

                } else {

                    "Mobile Ads Skipped".debug(initTag)
                    "".debug(initTag)

                    mobileAdsInitialized = false

                    callback.invoke()
                }

            } else {
                callback.invoke()
            }

        }
    }

    /*** Remote Config ***/

    private suspend fun fetchRemote(activity: Activity, callback: () -> Unit) {

        withContext(Dispatchers.IO) {

            if (activity.isNetworkAvailable() ) {

                "Fetching Remote".debug(initTag)

                val isCallbackInvoked = AtomicBoolean(false)
                val fetchDeferred = CompletableDeferred<Unit>()

                RemoteConfig.fetchValues {

                    fetchDeferred.complete(Unit)

                    if (isCallbackInvoked.compareAndSet(false, true)) {
                        "Fetching Remote Complete (on-time)".debug(initTag)
                        "".debug(initTag)
                        callback.invoke()
                    } else {
                        "Fetching Remote Complete (late)".debug(initTag)
                        "".debug(initTag)
                    }
                }

                withTimeoutOrNull(maxRemoteConfigWaitTime.milliseconds) {
                    fetchDeferred.await()
                }

                if (isCallbackInvoked.compareAndSet(false, true)) {
                    "Remote Config Timeout (${maxRemoteConfigWaitTime}ms reached) - Proceeding".debug(
                        initTag
                    )
                    "".debug(initTag)
                    callback.invoke()
                }

            } else {

                "Fetching Remote Skipped".debug(initTag)
                "".debug(initTag)

                callback.invoke()
            }
        }
    }


    /**** Tai Chi ****/

    suspend fun initializeTaiChi(context: Context) {

        withContext(Dispatchers.IO) {

            if (!BillingUtilsIAP.isPurchased.value) {

                if (context.isNetworkAvailable()) {

                    "Initializing TaiChi".debug(initTag)

                    tryCatch {
                        CurrencyConverter.fetchCurrencyRates()
                    }

                } else {
                    tryCatch {
                        CurrencyConverter.loadDefaultData()
                    }
                }
            }

        }
    }

    /**** App Flyer ****/

    private suspend fun initAppFlyer(context: Context) {

        tryCatch {

            "Initializing App Flyer".debug(initTag)

            withContext(Dispatchers.IO) {

                tryCatch {
                    AppsFlyerLib.getInstance().setDebugLog(true)
                }

                AppsFlyerLib.getInstance().init(
                    Constants.APP_FLYER_DEV_KEY,
                    null,
                    context
                )


                AppsFlyerLib.getInstance().registerSessionReadyListener {

                    tryCatch {
                        Log.d("AppFlyer", "registerSessionReadyListener")
                    }

                    AppsFlyerLib.getInstance().start(
                        object : AppsFlyerRequestListener {

                            override fun onSuccess() {


                                tryCatch {
                                    Log.d("AppFlyer", "Launch sent successfully")
                                }

                                tryCatch {

                                    val afPurchaseClient =
                                        PurchaseClient.Builder(context, Store.GOOGLE)
                                            // Enable Subscriptions auto logging
                                            .logSubscriptions(true)
                                            // Enable In Apps auto logging
                                            .autoLogInApps(true)
                                            // set production environment
                                            .setSandbox(false)
                                            // Subscription Purchase Event Data source listener. Invoked before sending data to AppsFlyer servers
                                            // to let customer add extra parameters to the payload
                                            .setSubscriptionPurchaseEventDataSource { _ ->
                                                mapOf()
                                            }

                                            // In Apps Purchase Event Data source listener. Invoked before sending data to AppsFlyer servers
                                            // to let customer add extra parameters to the payload
                                            .setInAppPurchaseEventDataSource { purchases ->
                                                mapOf()
                                            }

                                            // Subscriptions Purchase Validation listener. Invoked after getting response from AppsFlyer servers
                                            // to let customer know if purchase was validated successfully
                                            .setSubscriptionValidationResultListener(object :
                                                PurchaseClient.SubscriptionPurchaseValidationResultListener {

                                                override fun onResponse(currentResult: Map<String, SubscriptionValidationResult>?) {

                                                    tryCatch {

                                                        "".debug("AppFlyer", "onResponse A")

                                                        currentResult?.let { result ->
                                                            result.forEach { (k: String, currentValidation: SubscriptionValidationResult?) ->
                                                                currentValidation?.let { v ->
                                                                    if (v.success) {
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            "AppsFlyerTest [PurchaseConnector]: Subscription with ID $k was validated successfully"
                                                                        )
                                                                        val subscriptionPurchase =
                                                                            v.subscriptionPurchase
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            subscriptionPurchase.toString()
                                                                        )
                                                                    } else {
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            "AppsFlyerTest [PurchaseConnector]: Subscription with ID $k wasn't validated successfully"
                                                                        )
                                                                        val failureData =
                                                                            v.failureData
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            failureData.toString()
                                                                        )
                                                                    }
                                                                }

                                                            }
                                                        }
                                                    }
                                                }

                                                override fun onFailure(
                                                    result: String,
                                                    error: Throwable?
                                                ) {

                                                    tryCatch {

                                                        "$result $error".debug(
                                                            "AppFlyer",
                                                            "onFailure"
                                                        )

                                                        Log.i(
                                                            "AppFlyer",
                                                            "AppsFlyerTest [PurchaseConnector]: Validation fail: $result"
                                                        )
                                                        error?.printStackTrace()
                                                    }
                                                }
                                            })

                                            // In Apps Purchase Validation listener. Invoked after getting response from AppsFlyer servers
                                            // to let customer know if purchase was validated successfully
                                            .setInAppValidationResultListener(object :
                                                PurchaseClient.InAppPurchaseValidationResultListener {
                                                override fun onResponse(currentResult: Map<String, InAppPurchaseValidationResult>?) {

                                                    tryCatch {

                                                        "".debug("AppFlyer", "onResponse B")

                                                        currentResult?.let { result ->
                                                            result?.forEach { (k: String, currentValidation: InAppPurchaseValidationResult?) ->
                                                                currentValidation?.let { v ->
                                                                    if (v.success) {
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            "AppsFlyerTest [PurchaseConnector]:  Product with Purchase Token$k was validated successfully"
                                                                        )
                                                                        val productPurchase =
                                                                            v.productPurchase
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            productPurchase.toString()
                                                                        )
                                                                    } else {
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            "AppsFlyerTest [PurchaseConnector]:  Product with Purchase Token $k wasn't validated successfully"
                                                                        )
                                                                        val failureData =
                                                                            v.failureData
                                                                        Log.i(
                                                                            "AppFlyer",
                                                                            failureData.toString()
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                override fun onFailure(
                                                    result: String,
                                                    error: Throwable?
                                                ) {
                                                    tryCatch {

                                                        "$result $error".debug(
                                                            "AppFlyer",
                                                            "onFailure"
                                                        )

                                                        Log.i(
                                                            "AppFlyer",
                                                            "AppsFlyerTest [PurchaseConnector]: Validation fail: $result"
                                                        )
                                                        error?.printStackTrace()
                                                    }
                                                }
                                            })
                                            // Build the client
                                            .build()

                                    // Start the SDK instance to observe transactions.
                                    afPurchaseClient.startObservingTransactions()
                                }
                            }

                            override fun onError(errorCode: Int, errorDesc: String) {
                                tryCatch {
                                    Log.d(
                                        "AppFlyer",
                                        "Launch failed to be sent:\nError code: $errorCode\nError description: $errorDesc"
                                    )
                                }
                            }
                        })
                }


            }
        }
    }


    /**** Fire Base Initialization ****/

    private fun initializeFirebase(context: Context) {
        tryCatch {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        }
    }

    /*** Consent ***/

    private fun getConsent(activity: Activity, callback: () -> Unit) {

        if (BillingUtilsIAP.isPurchased.value) {

            callback.invoke()

        } else {

            activity.applicationContext?.let { appContext ->

                try {

                    val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager =
                        GoogleMobileAdsConsentManager.getInstance(appContext)

                    googleMobileAdsConsentManager.gatherConsent(activity) { consent ->
                        callback.invoke()
                    }

                } catch (_: Exception) {
                    callback.invoke()
                }

            } ?: callback.invoke()
        }

    }
}


