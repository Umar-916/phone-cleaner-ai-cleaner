package com.phonecleaner.aicleaner.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.data.media.MediaThumbs
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

enum class AiKind { SCREENSHOTS, LARGE_VIDEOS, BLURRY, OLD_DOWNLOADS }

data class AiGroup(val kind: AiKind, val files: List<FileEntry>) {
    val bytes: Long get() = files.sumOf { it.sizeBytes }
}

enum class AiPhase { COLLECTING, ANALYZING }

sealed interface AiEvent {
    data class Progress(val phase: AiPhase, val done: Int, val total: Int) : AiEvent
    data class Finished(val groups: List<AiGroup>) : AiEvent
}

/**
 * On-device suggestions based on simple rules (age, size, folder) and an image sharpness measure.
 * No data leaves the phone and no cloud model is involved.
 */
class AiCleanupScanner(private val context: Context, private val files: FilesRepository) {

    fun analyze(): Flow<AiEvent> = channelFlow {
        send(AiEvent.Progress(AiPhase.COLLECTING, 0, 0))
        val now = System.currentTimeMillis()
        val images = files.list(FileCategory.IMAGES, showHidden = false)
        val videos = files.list(FileCategory.VIDEO, showHidden = false)
        val downloads = files.list(FileCategory.DOWNLOAD, showHidden = false)

        val screenshots = images.filter { it.path.contains("/Screenshots/", ignoreCase = true) && now - it.modifiedMs > SCREENSHOT_AGE_MS }
        val largeVideos = videos.filter { it.sizeBytes >= LARGE_VIDEO_BYTES }.sortedByDescending { it.sizeBytes }
        val oldDownloads = downloads.filter { now - it.modifiedMs > DOWNLOAD_AGE_MS }.sortedByDescending { it.sizeBytes }

        val candidates = images
            .filter { it.path.contains("/DCIM/", ignoreCase = true) && !it.path.contains("/Screenshots/", ignoreCase = true) }
            .take(MAX_BLUR_CANDIDATES)
        val blurry = ArrayList<FileEntry>()
        var done = 0
        candidates.chunked(BATCH).forEach { batch ->
            coroutineContext.ensureActive()
            val results = coroutineScope {
                batch.map { entry ->
                    async(Dispatchers.Default) {
                        val bitmap = MediaThumbs.load(context, MediaFile(entry.id, entry.uri, entry.name, entry.sizeBytes, entry.modifiedMs, false, 0, 0, 0L), BLUR_SIZE)
                        entry to (bitmap?.let { sharpness(it) } ?: Double.MAX_VALUE)
                    }
                }.awaitAll()
            }
            results.filter { it.second < BLUR_THRESHOLD }.forEach { blurry += it.first }
            done += batch.size
            send(AiEvent.Progress(AiPhase.ANALYZING, done, candidates.size))
        }

        send(
            AiEvent.Finished(
                listOf(
                    AiGroup(AiKind.SCREENSHOTS, screenshots),
                    AiGroup(AiKind.OLD_DOWNLOADS, oldDownloads),
                    AiGroup(AiKind.LARGE_VIDEOS, largeVideos),
                    AiGroup(AiKind.BLURRY, blurry)
                ).filter { it.files.isNotEmpty() }
            )
        )
    }.flowOn(Dispatchers.IO)

    /** Variance of the Laplacian of the grayscale image: low values mean few sharp edges. */
    private fun sharpness(source: Bitmap): Double {
        val w = source.width
        val h = source.height
        if (w < 3 || h < 3) return Double.MAX_VALUE
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        val gray = IntArray(w * h) { (Color.red(pixels[it]) * 299 + Color.green(pixels[it]) * 587 + Color.blue(pixels[it]) * 114) / 1000 }
        var sum = 0.0
        var sumSquares = 0.0
        var count = 0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val laplacian = 4 * gray[y * w + x] - gray[y * w + x - 1] - gray[y * w + x + 1] - gray[(y - 1) * w + x] - gray[(y + 1) * w + x]
                sum += laplacian
                sumSquares += laplacian.toDouble() * laplacian
                count++
            }
        }
        val mean = sum / count
        return sumSquares / count - mean * mean
    }

    private companion object {
        const val SCREENSHOT_AGE_MS = 30L * 24 * 60 * 60 * 1000
        const val DOWNLOAD_AGE_MS = 60L * 24 * 60 * 60 * 1000
        const val LARGE_VIDEO_BYTES = 100L * 1024 * 1024
        const val MAX_BLUR_CANDIDATES = 600
        const val BATCH = 16
        const val BLUR_SIZE = 128
        const val BLUR_THRESHOLD = 40.0
    }
}
