package com.phonecleaner.aicleaner.core.advertisement.taichi

import androidx.annotation.Keep

@Keep
data class CurrencyRate(
    val `data`: Data,
    val message: Any,
    val status: String
)