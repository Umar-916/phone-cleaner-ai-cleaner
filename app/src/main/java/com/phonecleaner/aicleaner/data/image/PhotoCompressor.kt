package com.phonecleaner.aicleaner.data.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import com.phonecleaner.aicleaner.domain.model.CompressSettings
import com.phonecleaner.aicleaner.domain.model.EncodedImage
import com.phonecleaner.aicleaner.domain.model.OutputFormat
import com.phonecleaner.aicleaner.domain.model.PhotoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

class PhotoCompressor(private val context: Context) {

    private val resolver = context.contentResolver

    suspend fun readInfo(uri: Uri): PhotoInfo? = withContext(Dispatchers.IO) {
        runCatching {
            var name = "photo"
            var size = 0L
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = cursor.getString(it) ?: name }
                    cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = cursor.getLong(it) }
                }
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
            val swap = rotationDegrees(uri) % 180 != 0
            PhotoInfo(
                uri = uri,
                name = name,
                sizeBytes = size,
                width = if (swap) bounds.outHeight else bounds.outWidth,
                height = if (swap) bounds.outWidth else bounds.outHeight
            )
        }.getOrNull()
    }

    suspend fun decodePreview(uri: Uri, maxSide: Int): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val rotation = rotationDegrees(uri)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleFor(max(bounds.outWidth, bounds.outHeight), maxSide) })
            }
            bitmap?.let { rotate(it, rotation) }
        }.getOrNull()
    }

    suspend fun compress(photo: PhotoInfo, settings: CompressSettings): EncodedImage = withContext(Dispatchers.Default) {
        val (targetW, targetH) = settings.targetSize(photo)
        val rotation = rotationDegrees(photo.uri)
        val swap = rotation % 180 != 0
        val rawTargetW = if (swap) targetH else targetW
        val rawTargetH = if (swap) targetW else targetH
        val rawW = if (swap) photo.height else photo.width
        val rawH = if (swap) photo.width else photo.height

        var sample = 1
        while (rawW / (sample * 2) >= rawTargetW && rawH / (sample * 2) >= rawTargetH) sample *= 2

        val decoded = resolver.openInputStream(photo.uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Cannot decode image")

        val scaled = if (decoded.width != rawTargetW || decoded.height != rawTargetH) {
            Bitmap.createScaledBitmap(decoded, rawTargetW, rawTargetH, true).also { if (it !== decoded) decoded.recycle() }
        } else {
            decoded
        }
        val oriented = rotate(scaled, rotation)
        val flattened = if (settings.format == OutputFormat.JPEG && oriented.hasAlpha()) flattenOnWhite(oriented) else oriented

        val output = ByteArrayOutputStream()
        flattened.compress(compressFormat(settings.format), settings.quality.coerceIn(1, 100), output)
        EncodedImage(output.toByteArray(), flattened.width, flattened.height).also { flattened.recycle() }
    }

    fun decodeBytes(bytes: ByteArray, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleFor(max(bounds.outWidth, bounds.outHeight), maxSide) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    /** Saves into Pictures/PhoneCleaner as a new file; the original is never touched. */
    suspend fun save(bytes: ByteArray, baseName: String, format: OutputFormat): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = "${baseName.substringBeforeLast('.')}_compressed_${System.currentTimeMillis() % 100000}.${format.extension}"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, format.mime)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$FOLDER")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                } else {
                    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER).apply { mkdirs() }
                    @Suppress("DEPRECATION")
                    put(MediaStore.Images.Media.DATA, File(dir, fileName).absolutePath)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@runCatching null
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return@runCatching null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
            uri
        }.getOrNull()
    }

    private fun rotationDegrees(uri: Uri): Int = runCatching {
        resolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    }.getOrDefault(0)

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { if (it !== bitmap) bitmap.recycle() }
    }

    private fun flattenOnWhite(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(result).apply {
            drawColor(Color.WHITE)
            drawBitmap(source, 0f, 0f, null)
        }
        source.recycle()
        return result
    }

    @Suppress("DEPRECATION")
    private fun compressFormat(format: OutputFormat): Bitmap.CompressFormat = when (format) {
        OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
        OutputFormat.PNG -> Bitmap.CompressFormat.PNG
        OutputFormat.WEBP ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
    }

    private fun sampleFor(longSide: Int, maxSide: Int): Int {
        var sample = 1
        while (longSide / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    private companion object {
        const val FOLDER = "PhoneCleaner"
    }
}
