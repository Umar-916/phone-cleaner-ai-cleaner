package com.phonecleaner.aicleaner.data.whatsapp

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.phonecleaner.aicleaner.data.media.MediaThumbs
import com.phonecleaner.aicleaner.data.storage.DocumentWalker
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.domain.model.CleanResult
import com.phonecleaner.aicleaner.domain.model.MediaFile
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
import com.phonecleaner.aicleaner.data.media.FileHasher

class WhatsAppScanner(private val context: Context) {

    fun scan(): Flow<WaScanEvent> = flow {
        val roots = FolderAccess.whatsAppRoots(context)
        val files = ArrayList<WaFile>()
        var bytes = 0L
        var lastEmit = 0L

        roots.forEach { root ->
            DocumentWalker.walk(context, root, showHidden = true) { doc ->
                val category = doc.path.split('/').firstNotNullOfOrNull { categoryFor(it) } ?: return@walk
                files += WaFile(doc.uri, doc.path, doc.name, doc.sizeBytes, doc.modifiedMs, category, false)
                bytes += doc.sizeBytes
                val now = SystemClock.elapsedRealtime()
                if (now - lastEmit >= EMIT_INTERVAL_MS) {
                    lastEmit = now
                    emit(WaScanEvent.Progress(files.size, bytes))
                }
            }
        }
        emit(WaScanEvent.Progress(files.size, bytes))
        emit(WaScanEvent.Finished(markDuplicates(files), roots.isNotEmpty()))
    }.flowOn(Dispatchers.IO)

    suspend fun delete(files: List<WaFile>, onProgress: (Int) -> Unit): CleanResult = withContext(Dispatchers.IO) {
        var freed = 0L
        val removed = ArrayList<String>()
        files.forEachIndexed { index, file ->
            ensureActive()
            if (DocumentWalker.delete(context, file.uri)) {
                freed += file.sizeBytes
                removed += file.path
            }
            onProgress(index + 1)
        }
        CleanResult(freed, removed.size, removed)
    }

    fun thumbnail(file: WaFile, sizePx: Int): Bitmap? = when (file.previewKind) {
        WaPreviewKind.NONE -> null
        else -> MediaThumbs.load(
            context,
            MediaFile(0L, file.uri, file.name, file.sizeBytes, file.modifiedMs, file.previewKind == WaPreviewKind.VIDEO, 0, 0, 0L),
            sizePx
        )
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

    private fun hash(file: WaFile): String? = FileHasher.sha256(context.contentResolver, file.uri, file.sizeBytes)

    private companion object {
        const val EMIT_INTERVAL_MS = 120L
    }
}
