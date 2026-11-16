package com.phonecleaner.aicleaner.data.whatsapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.util.Size
import com.phonecleaner.aicleaner.domain.model.CleanResult
import com.phonecleaner.aicleaner.domain.model.WaCategory
import com.phonecleaner.aicleaner.domain.model.WaFile
import com.phonecleaner.aicleaner.domain.model.WaPreviewKind
import com.phonecleaner.aicleaner.domain.model.WaScanEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import kotlin.math.max

class WhatsAppScanner(private val context: Context) {

    fun scan(): Flow<WaScanEvent> = flow {
        val roots = mediaRoots()
        val files = ArrayList<WaFile>()
        var bytes = 0L
        var lastEmit = 0L

        for (root in roots) {
            val categoryDirs = root.listFiles { file -> file.isDirectory }.orEmpty()
            for (dir in categoryDirs) {
                val category = categoryFor(dir.name) ?: continue
                val stack = ArrayDeque<File>()
                stack.addLast(dir)
                while (stack.isNotEmpty()) {
                    currentCoroutineContext().ensureActive()
                    for (child in stack.removeLast().listFiles().orEmpty()) {
                        if (child.isDirectory) {
                            stack.addLast(child)
                        } else if (child.name != ".nomedia" && child.length() > 0) {
                            files += WaFile(child.absolutePath, child.name, child.length(), child.lastModified(), category, false)
                            bytes += child.length()
                        }
                    }
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastEmit >= EMIT_INTERVAL_MS) {
                        lastEmit = now
                        emit(WaScanEvent.Progress(files.size, bytes))
                    }
                }
            }
        }

        emit(WaScanEvent.Finished(markDuplicates(files), roots.isNotEmpty()))
    }.flowOn(Dispatchers.IO)

    suspend fun delete(files: List<WaFile>, onProgress: (Int) -> Unit): CleanResult = withContext(Dispatchers.IO) {
        var freed = 0L
        val removed = ArrayList<String>()
        files.forEachIndexed { index, file ->
            ensureActive()
            if (File(file.path).delete()) {
                freed += file.sizeBytes
                removed += file.path
            }
            onProgress(index + 1)
        }
        if (removed.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, removed.toTypedArray(), null, null)
        }
        CleanResult(freed, removed.size, removed)
    }

    fun thumbnail(file: WaFile, sizePx: Int): Bitmap? = runCatching {
        val source = File(file.path)
        when (file.previewKind) {
            WaPreviewKind.IMAGE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createImageThumbnail(source, Size(sizePx, sizePx), null)
            } else {
                decodeSampled(source, sizePx)
            }

            WaPreviewKind.VIDEO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(source, Size(sizePx, sizePx), null)
            } else {
                videoFrame(source, sizePx)
            }

            WaPreviewKind.NONE -> null
        }
    }.getOrNull()

    private fun mediaRoots(): List<File> {
        val external = Environment.getExternalStorageDirectory()
        return listOf(
            File(external, "Android/media/com.whatsapp/WhatsApp/Media"),
            File(external, "WhatsApp/Media"),
            File(external, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media"),
            File(external, "WhatsApp Business/Media")
        ).filter { it.isDirectory }
    }

    private fun categoryFor(folderName: String): WaCategory? {
        val name = folderName.lowercase()
        return when {
            name == ".statuses" -> WaCategory.STATUS
            "voice notes" in name -> WaCategory.VOICE
            "animated gifs" in name -> WaCategory.GIFS
            "stickers" in name -> WaCategory.STICKERS
            "images" in name -> WaCategory.IMAGES
            "video" in name -> WaCategory.VIDEOS
            "audio" in name -> WaCategory.AUDIO
            "documents" in name -> WaCategory.DOCUMENTS
            else -> null
        }
    }

    private suspend fun markDuplicates(files: List<WaFile>): List<WaFile> {
        val duplicatePaths = HashSet<String>()
        val candidates = files.groupBy { it.sizeBytes }.values.filter { it.size > 1 }
        for (sameSize in candidates) {
            currentCoroutineContext().ensureActive()
            val byHash = HashMap<String, MutableList<WaFile>>()
            sameSize.forEach { file -> hash(file)?.let { byHash.getOrPut(it) { ArrayList() } += file } }
            byHash.values.filter { it.size > 1 }.forEach { group ->
                group.sortedBy { it.modifiedMs }.drop(1).forEach { duplicatePaths += it.path }
            }
        }
        return files.map { if (it.path in duplicatePaths) it.copy(isDuplicate = true) else it }
    }

    private fun hash(file: WaFile): String? = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        RandomAccessFile(file.path, "r").use { raf ->
            val buffer = ByteArray(CHUNK)
            val offsets = if (file.sizeBytes <= FULL_HASH_LIMIT) {
                generateSequence(0L) { it + CHUNK }.takeWhile { it < file.sizeBytes }.toList()
            } else {
                listOf(0L, file.sizeBytes / 2, file.sizeBytes - CHUNK)
            }
            offsets.forEach { offset ->
                raf.seek(offset)
                val read = raf.read(buffer)
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    private fun decodeSampled(file: File, size: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= size) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun videoFrame(file: File, size: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.path)
            retriever.getFrameAtTime(0)?.let { Bitmap.createScaledBitmap(it, size, size, true) }
        } finally {
            retriever.release()
        }
    }

    private companion object {
        const val EMIT_INTERVAL_MS = 120L
        const val CHUNK = 1024 * 1024
        const val FULL_HASH_LIMIT = 32L * 1024 * 1024
    }
}
