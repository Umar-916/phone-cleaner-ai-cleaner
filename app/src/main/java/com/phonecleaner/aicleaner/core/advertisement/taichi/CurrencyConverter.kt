package com.phonecleaner.aicleaner.core.advertisement.taichi

import android.util.Log
import com.google.gson.Gson
import com.phonecleaner.aicleaner.core.common.Constants
import com.phonecleaner.aicleaner.core.common.Constants.commonCoroutineExceptionHandler
import com.phonecleaner.aicleaner.core.common.tryCatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object CurrencyConverter {

    private const val BASE_URL = "https://exchangerate.funsol.cloud"

    private var currencyRates: Map<String, Double> = emptyMap()
    var currencyRatesJson: CurrencyRate? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun fetchCurrencyRates() {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL${CurrencyConverterApi.PATH}")
                    .header("Authorization", CurrencyConverterApi.AUTHORIZATION)
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    val result = body?.let { gson.fromJson(it, CurrencyRate::class.java) }
                    if (response.isSuccessful && result != null) {
                        currencyRatesJson = result
                        currencyRates = result.data.rates
                        Log.d("CurrencyConverter", "currencyRates: $currencyRates")
                    } else {
                        currencyRatesJson?.data?.rates?.let {
                            currencyRates = it
                        }
                        Log.e("CurrencyConverter", "Error: api ${response.message}")
                    }
                }
            } catch (e: Exception) {
                currencyRatesJson?.data?.rates?.let {
                    currencyRates = it
                }
                Log.e("CurrencyConverter", "Catch failed: ${e.message}")
            }
        }
    }

    suspend fun loadDefaultData() {
        tryCatch {
            currencyRatesJson?.data?.rates?.let {
                currencyRates = it
            }
        }
    }

    fun convertToUSD(amount: Double, fromCurrency: String, callback: (converted: Double) -> Unit) {
        Constants.myAppCoroutineScope.launch(Dispatchers.IO + commonCoroutineExceptionHandler) {
            ensureActive()

            if (fromCurrency.equals("USD", true)) {
                callback(amount)
                return@launch
            }

            if (currencyRates.isEmpty()) {
                Log.d("OnPaidEventListenerTag", "convertToUSD: currencyRates is Empty")
                fetchCurrencyRates()
            }

            var rate = currencyRates[fromCurrency.uppercase()] ?: 1.0
            if (rate <= 0.0) {
                rate = 1.0
            }

            val usdAmount = amount / rate
            Log.d(
                "OnPaidEventListenerTag",
                "convertToUSD: rate  $rate  $fromCurrency = $amount  usd = $usdAmount "
            )
            callback(usdAmount)
        }
    }
}
