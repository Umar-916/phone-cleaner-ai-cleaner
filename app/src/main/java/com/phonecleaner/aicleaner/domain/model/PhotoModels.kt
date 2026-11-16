package com.phonecleaner.aicleaner.domain.model

import android.graphics.Bitmap
import android.net.Uri
import kotlin.math.roundToInt

enum class OutputFormat(val mime: String, val extension: String, val label: String) {
    JPEG("image/jpeg", "jpg", "JPEG"),
    PNG("image/png", "png", "PNG"),
    WEBP("image/webp", "webp", "WebP")
}

data class PhotoInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int
)

data class CompressSettings(
    val format: OutputFormat = OutputFormat.JPEG,
    val quality: Int = 80,
    val custom: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val keepAspect: Boolean = true
) {
    val usesQuality: Boolean get() = format != OutputFormat.PNG

    fun targetSize(photo: PhotoInfo): Pair<Int, Int> {
        if (!custom || width <= 0) return photo.width to photo.height
        val targetWidth = width.coerceIn(1, MAX_SIDE)
        val targetHeight = if (keepAspect || height <= 0) {
            (targetWidth.toFloat() * photo.height / photo.width).roundToInt()
        } else {
            height
        }
        return targetWidth to targetHeight.coerceIn(1, MAX_SIDE)
    }

    companion object {
        const val MAX_SIDE = 8192
    }
}

class EncodedImage(val bytes: ByteArray, val width: Int, val height: Int)

data class CompressedFile(val name: String, val originalBytes: Long, val newBytes: Long, val uri: Uri)

data class PhotoPreview(
    val original: Bitmap? = null,
    val compressed: Bitmap? = null,
    val compressedBytes: Int = 0,
    val outWidth: Int = 0,
    val outHeight: Int = 0,
    val loading: Boolean = true
)
