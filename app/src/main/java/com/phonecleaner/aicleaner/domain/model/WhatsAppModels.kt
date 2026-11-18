package com.phonecleaner.aicleaner.domain.model

import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

enum class WaCategory(@StringRes val label: Int) {
    IMAGES(R.string.wa_cat_images),
    VIDEOS(R.string.wa_cat_videos),
    AUDIO(R.string.wa_cat_audio),
    VOICE(R.string.wa_cat_voice),
    DOCUMENTS(R.string.wa_cat_documents),
    STICKERS(R.string.wa_cat_stickers),
    GIFS(R.string.wa_cat_gifs),
    STATUS(R.string.wa_cat_status)
}

enum class WaTab { RECEIVED, SENT }

val WaCategory.hasSentTab: Boolean get() = this != WaCategory.STICKERS && this != WaCategory.STATUS

enum class WaFilter(@StringRes val label: Int) {
    ALL(R.string.wa_filter_all),
    LARGE(R.string.wa_filter_large),
    OLD(R.string.wa_filter_old),
    DUPLICATES(R.string.wa_filter_duplicates)
}

enum class WaPreviewKind { IMAGE, VIDEO, NONE }

data class WaFile(
    val uri: android.net.Uri,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val category: WaCategory,
    val isDuplicate: Boolean
) {
    val isSent: Boolean get() = path.contains("/Sent/", ignoreCase = true)
    val previewKind: WaPreviewKind
        get() = when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "webp", "gif" -> WaPreviewKind.IMAGE
            "mp4", "3gp", "mkv", "mov", "webm" -> WaPreviewKind.VIDEO
            else -> WaPreviewKind.NONE
        }

    fun matches(filter: WaFilter, now: Long): Boolean = when (filter) {
        WaFilter.ALL -> true
        WaFilter.LARGE -> sizeBytes >= WA_LARGE_BYTES
        WaFilter.OLD -> now - modifiedMs >= WA_OLD_AGE_MS
        WaFilter.DUPLICATES -> isDuplicate
    }
}

const val WA_LARGE_BYTES = 25L * 1024 * 1024
const val WA_OLD_AGE_MS = 90L * 24 * 60 * 60 * 1000

sealed interface WaScanEvent {
    data class Progress(val count: Int, val bytes: Long) : WaScanEvent
    data class Finished(val files: List<WaFile>, val foundFolders: Boolean) : WaScanEvent
}

