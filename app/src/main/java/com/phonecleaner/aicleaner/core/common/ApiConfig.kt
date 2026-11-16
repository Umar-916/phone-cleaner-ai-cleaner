package com.phonecleaner.aicleaner.core.common

import com.phonecleaner.aicleaner.BuildConfig

/** Single place for backend API configuration. */

object ApiConfig {
    val BASE_URL = if (BuildConfig.DEBUG) "https://staging.cognise.art:5039" else "https://pranksound.cognise.art"
    const val CATEGORIES_ENDPOINT = "api/categories"
    const val SOUNDS_ENDPOINT = "api/sounds"
    const val TRENDING_ENDPOINT = "api/trending"

     val CATEGORIES_URL = "$BASE_URL/$CATEGORIES_ENDPOINT"

    val SOUNDS_URL = "$BASE_URL/$SOUNDS_ENDPOINT"

    val TRENDING_URL = "$BASE_URL/$TRENDING_ENDPOINT"

     val API_TOKEN = if (BuildConfig.DEBUG) "f0e2ea9d9d37df6dd1847acfe2a82c45fb756c76" else "1224cb599332534c630d8af03e01c104d25c1c32"

}
