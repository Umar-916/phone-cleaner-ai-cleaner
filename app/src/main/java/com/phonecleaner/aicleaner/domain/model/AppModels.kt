package com.phonecleaner.aicleaner.domain.model

data class InstalledApp(
    val packageName: String,
    val label: String,
    val versionName: String,
    val isSystem: Boolean,
    val apkSizeBytes: Long,
    val installedAt: Long,
    val canLaunch: Boolean
)

enum class AppFilter { ALL, USER, SYSTEM }

enum class AppSort { NAME, SIZE, RECENT }
