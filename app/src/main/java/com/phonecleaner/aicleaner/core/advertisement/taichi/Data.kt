package com.phonecleaner.aicleaner.core.advertisement.taichi

import androidx.annotation.Keep

@Keep
data class Data(
    val base: String,
    val rates: Map<String, Double>
)