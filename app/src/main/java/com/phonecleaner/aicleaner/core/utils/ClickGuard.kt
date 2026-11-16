package com.phonecleaner.aicleaner.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Ensures only the first action in [windowMs] runs.
 * Use for single-fire clicks / navigation so overlapping composables
 * cannot trigger two callbacks from one tap.
 */
class ClickGuard(private val windowMs: Long = DEFAULT_WINDOW_MS) {
    @Volatile
    private var lastActionAt: Long = 0L

    fun run(action: () -> Unit): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastActionAt < windowMs) return false
        lastActionAt = now
        action()
        return true
    }

    companion object {
        const val DEFAULT_WINDOW_MS = 600L
    }
}

@Composable
fun rememberClickGuard(windowMs: Long = ClickGuard.DEFAULT_WINDOW_MS): ClickGuard {
    return remember(windowMs) { ClickGuard(windowMs) }
}
