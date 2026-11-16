package com.phonecleaner.aicleaner.core.advertisement.taichi

/**
 * Currency rates endpoint used by [CurrencyConverter].
 * Kept as the contract for the Funsol exchange-rate API.
 */
interface CurrencyConverterApi {
    companion object {
        const val PATH = "/api/currency-rates/"
        const val AUTHORIZATION = "token 437bcc20a5710f302c8b0b7df0f4115d319107a1"
    }
}
