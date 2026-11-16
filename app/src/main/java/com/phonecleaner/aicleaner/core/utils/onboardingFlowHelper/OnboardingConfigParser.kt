package com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper

import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.phonecleaner.aicleaner.core.common.debug
import com.phonecleaner.aicleaner.core.utils.onboardingFlowHelper.entity.OnboardingFlowConfig

@Keep
object OnboardingConfigParser {

    fun parse(
        json: String?,
        fallback: OnboardingFlowConfig
    ): OnboardingFlowConfig {
        val gson = Gson()
        try {
            if (json.isNullOrBlank()) {
                return fallback
            }

            val parsed: OnboardingFlowConfig? = try {
                gson.fromJson(json, OnboardingFlowConfig::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(OnboardingFlowHelper.TAG, "Parser: invalid JSON, using fallback flow.", e)
                null
            } catch (e: Exception) {
                Log.e(OnboardingFlowHelper.TAG, "Parser: unexpected error, using fallback flow.", e)
                null
            }

            if (parsed == null || parsed.screens.isEmpty()) {
                return fallback
            }

            "Parser: parsed ${parsed.screens.size} screen(s)".debug(OnboardingFlowHelper.TAG)
            return parsed
        } catch (e: Exception) {
            Log.e(OnboardingFlowHelper.TAG, "Parser: unexpected error, using fallback flow.", e)
            return fallback
        }
    }
}
