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
import java.io.FileInputStream
import java.security.MessageDigest

class DuplicateScanner(private val context: Context, private val filesRepository: FilesRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scanDispatcher = Dispatchers.Default.limitedParallelism(2)

    private val resolver = context.contentResolver

    fun scan(): Flow<DuplicateScanEvent> = channelFlow {
        filesRepository.refresh()
        send(DuplicateScanEvent.Progress(DuplicateProgress(DuplicatePhase.READING, 0, 0)))
        val images = queryMedia(isVideo = false)
        val videos = queryMedia(isVideo = true)

        val exactImages = findExact(images, this, offset = 0, total = images.size + videos.size)
        val exactVideos = findExact(videos, this, offset = images.size, total = images.size + videos.size)
        val similar = findSimilar(images, exactImages, this)

        send(
            DuplicateScanEvent.Finished(
                DuplicateScanResult(
                    photoGroups = (exactImages + similar).sortedByDescending { it.recoverableBytes() },
                    videoGroups = exactVideos.sortedByDescending { it.recoverableBytes() }
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
                            hashes[index] = differenceHash(it)
                            valid[index] = true
                        }
                    }
                }.awaitAll()
            }
            done += batch.size
            scope.send(
                DuplicateScanEvent.Progress(DuplicateProgress(DuplicatePhase.ANALYZING, done, pool.size))
            )
        }

        val parent = IntArray(pool.size) { it }
        fun find(x: Int): Int {
            var root = x
            while (parent[root] != root) root = parent[root]
            var node = x
            while (parent[node] != root) {
                val next = parent[node]
                parent[node] = root
                node = next
            }
            return root
        }
        for (i in pool.indices) {
            if (!valid[i]) continue
            if (i % COMPARE_YIELD == 0) scope.coroutineContext.ensureActive()
            for (j in i + 1 until pool.size) {
                if (valid[j] && java.lang.Long.bitCount(hashes[i] xor hashes[j]) <= SIMILAR_DISTANCE) {
                    parent[find(i)] = find(j)
                }
            }
        }

        return pool.indices
            .filter { valid[it] }
            .groupBy { find(it) }
            .values
            .filter { it.size > 1 }
            .map { members ->
                val files = members.map { pool[it] }.sortedBy { it.addedAtMs }
                DuplicateGroup("similar:${files.first().id}", DuplicateKind.SIMILAR, false, files)
            }
    }

    private fun hash(file: MediaFile): String? = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        resolver.openFileDescriptor(file.uri, "r")?.use { pfd ->
            FileInputStream(pfd.fileDescriptor).use { stream ->
                val channel = stream.channel
                val buffer = java.nio.ByteBuffer.allocate(CHUNK)
                val offsets = if (file.sizeBytes <= FULL_HASH_LIMIT) {
                    generateSequence(0L) { it + CHUNK }.takeWhile { it < file.sizeBytes }.toList()
                } else {
                    listOf(0L, file.sizeBytes / 2, file.sizeBytes - CHUNK)
                }
                offsets.forEach { offset ->
                    buffer.clear()
                    channel.position(offset)
                    val read = channel.read(buffer)
                    if (read > 0) digest.update(buffer.array(), 0, read)
                }
            }
        } ?: return null
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

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
        const val CHUNK = 1024 * 1024
        const val FULL_HASH_LIMIT = 32L * 1024 * 1024
        const val PROGRESS_STEP = 5
        const val HASH_BATCH = 12
        const val HASH_THUMB_SIZE = 72
        const val SIMILAR_DISTANCE = 5
        const val COMPARE_YIELD = 256
    }
}

fun DuplicateGroup.recoverableBytes(): Long = files.drop(1).sumOf { it.sizeBytes }
