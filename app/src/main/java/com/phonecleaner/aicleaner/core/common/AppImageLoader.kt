package com.phonecleaner.aicleaner.core.common

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * App-wide Coil loader. The image CDN is slow under many parallel requests, so it uses generous
 * timeouts, a capped parallelism and one automatic retry instead of Coil's default 10s / no retry.
 */
fun buildAppImageLoader(context: Context): ImageLoader {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .dispatcher(Dispatcher().apply {
            maxRequests = MAX_PARALLEL_REQUESTS
            maxRequestsPerHost = MAX_PARALLEL_REQUESTS
        })
        .addInterceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (_: IOException) {
                chain.proceed(chain.request())
            }
        }
        .build()

    return ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { client })) }
        .build()
}

private const val MAX_PARALLEL_REQUESTS = 4
