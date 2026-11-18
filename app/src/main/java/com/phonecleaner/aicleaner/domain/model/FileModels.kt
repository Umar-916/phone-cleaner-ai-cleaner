package com.phonecleaner.aicleaner.domain.model

import android.net.Uri
import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

enum class FileCategory(@StringRes val label: Int) {
    IMAGES(R.string.files_images),
    VIDEO(R.string.files_video),
    AUDIO(R.string.files_audio)
}

data class FileEntry(
    val uri: Uri,
    val id: Long,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val mime: String,
    val category: FileCategory
) {
    val extension: String get() = name.substringAfterLast('.', "").lowercase()
    val folderName: String get() = path.substringBeforeLast('/', "").substringAfterLast('/')
    val isMedia: Boolean get() = category == FileCategory.IMAGES || category == FileCategory.VIDEO
}

data class CategorySummary(val count: Int = 0, val bytes: Long = 0L)
