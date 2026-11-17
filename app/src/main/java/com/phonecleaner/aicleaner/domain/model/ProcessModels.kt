package com.phonecleaner.aicleaner.domain.model

data class MemoryStats(val totalBytes: Long = 0L, val availableBytes: Long = 0L) {
    val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0L)
    val usedFraction: Float get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
}

data class BoostResult(val freedBytes: Long, val appsCount: Int)

data class RecentApp(
    val packageName: String,
    val label: String,
    val lastUsedAt: Long,
    val foregroundMs: Long
)
