package com.phonecleaner.aicleaner.data.media

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.data.storage.DocumentWalker
import com.phonecleaner.aicleaner.domain.model.FileCategory
import android.provider.MediaStore
import com.phonecleaner.aicleaner.domain.model.DuplicateGroup
import com.phonecleaner.aicleaner.domain.model.DuplicateKind
import com.phonecleaner.aicleaner.domain.model.DuplicatePhase
import com.phonecleaner.aicleaner.domain.model.DuplicateProgress
import com.phonecleaner.aicleaner.domain.model.DuplicateScanEvent
import com.phonecleaner.aicleaner.domain.model.DuplicateScanResult
import com.phonecleaner.aicleaner.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DuplicateScanner(private val context: Context, private val filesRepository: FilesRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scanDispatcher = Dispatchers.Default.limitedParallelism(2)

    private val resolver = context.contentResolver

    fun scan(): Flow<DuplicateScanEvent> = channelFlow {
        filesRepository.refresh()
        send(DuplicateScanEvent.Progress(DuplicateProgress(DuplicatePhase.READING, 0, 0)))
        val images = queryMedia(isVideo = false)
        val videos = queryMedia(isVideo = true)
        val others = queryOtherFiles()
        val total = images.size + videos.size + others.size

        val exactImages = findExact(images, this, offset = 0, total = total)
        val exactVideos = findExact(videos, this, offset = images.size, total = total)
        val exactFiles = findExact(others, this, offset = images.size + videos.size, total = total)
        val similar = findSimilar(images, exactImages, this)

        send(
            DuplicateScanEvent.Finished(
                DuplicateScanResult(
                    photoGroups = (exactImages + similar).sortedByDescending { it.recoverableBytes() },
                    videoGroups = exactVideos.sortedByDescending { it.recoverableBytes() },
                    fileGroups = exactFiles.sortedByDescending { it.recoverableBytes() }
                )
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun delete(files: List<MediaFile>): List<Long> = withContext(Dispatchers.IO) {
        files.filter { DocumentWalker.delete(context, it.uri) }.map { it.id }.also { if (it.isNotEmpty()) filesRepository.refresh() }
    }

    private suspend fun queryMedia(isVideo: Boolean): List<MediaFile> =
        filesRepository.list(if (isVideo) FileCategory.VIDEO else FileCategory.IMAGES, showHidden = false).map {
            MediaFile(it.id, it.uri, it.name, it.sizeBytes, it.modifiedMs, isVideo, 0, 0, 0L)
        }

    private suspend fun queryOtherFiles(): List<MediaFile> =
        filesRepository.listNonMedia(showHidden = false)
            .filter { it.sizeBytes > 0 }
            .map { MediaFile(it.id, it.uri, it.name, it.sizeBytes, it.modifiedMs, false, 0, 0, 0L, isFile = true, mime = it.mime) }

    private suspend fun findExact(
        files: List<MediaFile>,
        scope: ProducerScope<DuplicateScanEvent>,
        offset: Int,
        total: Int
    ): List<DuplicateGroup> {
        val candidates = files.groupBy { it.sizeBytes }.values.filter { it.size > 1 }.flatten()
        val hashed = HashMap<String, MutableList<MediaFile>>()
        candidates.forEachIndexed { index, file ->
            scope.coroutineContext.ensureActive()
            hash(file)?.let { hashed.getOrPut("${file.sizeBytes}:$it") { ArrayList() } += file }
            if (index % PROGRESS_STEP == 0) {
                scope.send(
                    DuplicateScanEvent.Progress(
                        DuplicateProgress(DuplicatePhase.COMPARING, offset + index, total)
                    )
                )
            }
        }
        return hashed.entries
            .filter { it.value.size > 1 }
            .map { (key, group) ->
                DuplicateGroup(
                    id = "exact:$key",
                    kind = DuplicateKind.EXACT,
                    isVideo = group.first().isVideo,
                    isFile = group.first().isFile,
                    files = group.sortedBy { it.addedAtMs }
                )
            }
    }

    private suspend fun findSimilar(
        images: List<MediaFile>,
        exactGroups: List<DuplicateGroup>,
        scope: ProducerScope<DuplicateScanEvent>
    ): List<DuplicateGroup> {
        val inExact = exactGroups.flatMap { group -> group.files.drop(1) }.map { it.id }.toSet()
        val pool = images.filter { it.id !in inExact }
        if (pool.size < 2) return emptyList()

        val hashes = LongArray(pool.size)
        val valid = BooleanArray(pool.size)
        var done = 0
        pool.indices.chunked(HASH_BATCH).forEach { batch ->
            scope.coroutineContext.ensureActive()
            coroutineScope {
                batch.map { index ->
                    async(scanDispatcher) {
                        MediaThumbs.load(context, pool[index], HASH_THUMB_SIZE)?.let {
                            val hash = differenceHash(it)
                            hashes[index] = hash
                            // Near-flat images (solid colour, blank screenshots) all hash alike and are not real duplicates.
                            valid[index] = java.lang.Long.bitCount(hash) in MIN_HASH_BITS..(64 - MIN_HASH_BITS)
                        }
                    }
                }.awaitAll()
            }
            done += batch.size
            scope.send(
                DuplicateScanEvent.Progress(DuplicateProgress(DuplicatePhase.ANALYZING, done, pool.size))
            )
        }

        // Each image joins at most one group and must be close to that group's first image. Chaining A~B~C (union-find)
        // could merge unrelated photos into one group that is then pre-selected for deletion.
        val assigned = BooleanArray(pool.size)
        val result = ArrayList<DuplicateGroup>()
        for (i in pool.indices) {
            if (!valid[i] || assigned[i]) continue
            if (i % COMPARE_YIELD == 0) scope.coroutineContext.ensureActive()
            val members = arrayListOf(i)
            for (j in i + 1 until pool.size) {
                if (valid[j] && !assigned[j] && java.lang.Long.bitCount(hashes[i] xor hashes[j]) <= SIMILAR_DISTANCE) {
                    members += j
                }
            }
            if (members.size > 1) {
                members.forEach { assigned[it] = true }
                val files = members.map { pool[it] }.sortedBy { it.addedAtMs }
                result += DuplicateGroup("similar:${files.first().id}", DuplicateKind.SIMILAR, false, files)
            }
        }
        return result
    }

    private fun hash(file: MediaFile): String? = FileHasher.sha256(resolver, file.uri, file.sizeBytes)

    private fun differenceHash(source: Bitmap): Long {
        val small = Bitmap.createScaledBitmap(source, 9, 8, true)
        var bits = 0L
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                bits = (bits shl 1) or if (luminance(small.getPixel(x, y)) > luminance(small.getPixel(x + 1, y))) 1L else 0L
            }
        }
        if (small !== source) small.recycle()
        return bits
    }

    private fun luminance(pixel: Int): Int =
        (Color.red(pixel) * 299 + Color.green(pixel) * 587 + Color.blue(pixel) * 114) / 1000

    private companion object {
        const val PROGRESS_STEP = 5
        const val HASH_BATCH = 12
        const val HASH_THUMB_SIZE = 72
        const val SIMILAR_DISTANCE = 5
        const val MIN_HASH_BITS = 8
        const val COMPARE_YIELD = 256
    }
}

fun DuplicateGroup.recoverableBytes(): Long = files.drop(1).sumOf { it.sizeBytes }
