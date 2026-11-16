package com.phonecleaner.aicleaner.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import com.phonecleaner.aicleaner.domain.model.MediaFile
import kotlin.math.max

object MediaThumbs {

    fun load(context: Context, file: MediaFile, size: Int): Bitmap? = runCatching {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                context.contentResolver.loadThumbnail(file.uri, Size(size, size), null)

            file.isVideo -> videoFrame(context, file, size)
            else -> decodeImage(context, file, size)
        }
    }.getOrNull()

    private fun videoFrame(context: Context, file: MediaFile, size: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)
            retriever.getFrameAtTime(0)?.let { Bitmap.createScaledBitmap(it, size, size, true) }
        } finally {
            retriever.release()
        }
    }

    private fun decodeImage(context: Context, file: MediaFile, size: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= size) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }
}
