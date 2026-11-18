package com.phonecleaner.aicleaner.domain.model

import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

enum class JunkCategory(@StringRes val label: Int, val selectedByDefault: Boolean) {
    TEMP(R.string.junk_cat_temp, true),
    THUMBNAILS(R.string.junk_cat_thumbnails, true),
    APK(R.string.junk_cat_apk, true),
    EMPTY_FOLDERS(R.string.junk_cat_empty, true),
    OLD_DOWNLOADS(R.string.junk_cat_old_downloads, true),
    LARGE_FILES(R.string.junk_cat_large, true),
    TRASH(R.string.junk_cat_trash, true)
}

enum class JunkGroup(@StringRes val label: Int, val categories: List<JunkCategory>) {
    RESIDUALS(R.string.junk_group_residuals, listOf(JunkCategory.EMPTY_FOLDERS, JunkCategory.THUMBNAILS)),
    OBSOLETE(R.string.junk_group_obsolete, listOf(JunkCategory.APK, JunkCategory.OLD_DOWNLOADS, JunkCategory.LARGE_FILES)),
    AD_JUNK(R.string.junk_group_ad_junk, listOf(JunkCategory.TEMP)),
    TRASH(R.string.junk_group_trash, listOf(JunkCategory.TRASH))
}

data class JunkItem(
    val uri: android.net.Uri?,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val category: JunkCategory,
    val isDirectory: Boolean = false
)

data class ScanProgress(
    val currentPath: String,
    val foundBytes: Long,
    val foundCount: Int,
    val fraction: Float = 0f,
    val categoryBytes: Map<JunkCategory, Long> = emptyMap()
)

sealed interface ScanEvent {
    data class Progress(val progress: ScanProgress) : ScanEvent
    data class Finished(val items: List<JunkItem>) : ScanEvent
}

data class CleanResult(val freedBytes: Long, val removedCount: Int, val removedPaths: List<String>)
