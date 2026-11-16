package com.phonecleaner.aicleaner.domain.model

import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

enum class JunkCategory(@StringRes val label: Int, val selectedByDefault: Boolean) {
    TEMP(R.string.junk_cat_temp, true),
    THUMBNAILS(R.string.junk_cat_thumbnails, true),
    APK(R.string.junk_cat_apk, true),
    EMPTY_FOLDERS(R.string.junk_cat_empty, true),
    OLD_DOWNLOADS(R.string.junk_cat_old_downloads, false),
    LARGE_FILES(R.string.junk_cat_large, false)
}

data class JunkItem(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val category: JunkCategory,
    val isDirectory: Boolean = false
)

data class ScanProgress(val currentPath: String, val foundBytes: Long, val foundCount: Int)

sealed interface ScanEvent {
    data class Progress(val progress: ScanProgress) : ScanEvent
    data class Finished(val items: List<JunkItem>) : ScanEvent
}

data class CleanResult(val freedBytes: Long, val removedCount: Int, val removedPaths: List<String>)
