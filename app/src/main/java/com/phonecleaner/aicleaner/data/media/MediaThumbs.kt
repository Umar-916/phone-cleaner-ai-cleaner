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

    private val gate = java.util.concurrent.Semaphore(2)
    private val cache = object : android.util.LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 12).toInt()) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun load(context: Context, file: MediaFile, size: Int): Bitmap? {
        val key = "${file.uri}@$size"
        cache.get(key)?.let { return it }
        gate.acquireUninterruptibly()
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            cache.get(key)?.let { return it }
            return decode(context, file, size)?.also { cache.put(key, it) }
        } finally {
            gate.release()
        }
    }

    private fun decode(context: Context, file: MediaFile, size: Int): Bitmap? {
        if (!file.isVideo) runCatching { decodeImage(context, file, size) }.getOrNull()?.let { return it }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { context.contentResolver.loadThumbnail(file.uri, Size(size, size), null) }.getOrNull()?.let { return it }
        }
        return runCatching { if (file.isVideo) videoFrame(context, file, size) else decodeImage(context, file, size) }.getOrNull()
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, file.uri)
                return android.graphics.ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val scale = (max(info.size.width, info.size.height) / size.toFloat()).coerceAtLeast(1f)
                    decoder.setTargetSize((info.size.width / scale).toInt().coerceAtLeast(1), (info.size.height / scale).toInt().coerceAtLeast(1))
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= size) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.RGB_565 }
        return context.contentResolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }
}
