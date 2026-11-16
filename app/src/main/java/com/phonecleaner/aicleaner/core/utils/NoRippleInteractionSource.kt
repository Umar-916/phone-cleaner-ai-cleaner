package com.phonecleaner.aicleaner.core.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true

}

object ComponentHelper {

    fun Modifier.debounceClickable(
        delayMillis: Long = 1000L,
        onClick: () -> Unit
    ): Modifier = composed {
        var lastClickTime by remember { mutableLongStateOf(0L) }

        clickable(
            indication = null,
            interactionSource = NoRippleInteractionSource()
        ) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > delayMillis) {
                lastClickTime = currentTime

                onClick()
            }
        }
    }

}