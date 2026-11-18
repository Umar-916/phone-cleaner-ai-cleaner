package com.phonecleaner.aicleaner.data.scanner

import android.content.Context
import android.os.SystemClock
import com.phonecleaner.aicleaner.data.storage.DocumentWalker
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.domain.model.CleanResult
import com.phonecleaner.aicleaner.domain.model.JunkCategory
import com.phonecleaner.aicleaner.domain.model.JunkItem
import com.phonecleaner.aicleaner.domain.model.ScanEvent
import com.phonecleaner.aicleaner.domain.model.ScanProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class JunkScanner(private val context: Context) {

    fun scan(): Flow<ScanEvent> = flow {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
        val items = ArrayList<JunkItem>()
        var foundBytes = 0L
        var visited = 0
        val categoryBytes = HashMap<JunkCategory, Long>()
        var lastEmit = 0L
        val roots = FolderAccess.mediaRoots(context)

        suspend fun add(item: JunkItem) {
            items += item
            foundBytes += item.sizeBytes
            categoryBytes[item.category] = (categoryBytes[item.category] ?: 0L) + item.sizeBytes
            val now = SystemClock.elapsedRealtime()
            if (now - lastEmit >= EMIT_INTERVAL_MS) {
                lastEmit = now
                emit(ScanEvent.Progress(ScanProgress(item.path, foundBytes, items.size, 0.95f * visited / (visited + 400f), HashMap(categoryBytes))))
            }
        }


        roots.forEach { root ->
            DocumentWalker.walk(context, root, showHidden = true) { doc ->
                visited++
                classify(doc.path, doc.name, doc.sizeBytes, doc.modifiedMs)?.let { category ->
                    add(JunkItem(doc.uri, doc.path, doc.name, doc.sizeBytes, doc.modifiedMs, category))
                }
            }
        }
        appCacheFiles().forEach { file ->
            currentCoroutineContext().ensureActive()
            add(JunkItem(null, file.absolutePath, file.name, file.length(), file.lastModified(), JunkCategory.TEMP))
        }
        emit(ScanEvent.Finished(items))
    }.flowOn(Dispatchers.IO)

    suspend fun delete(items: List<JunkItem>, onProgress: (Int) -> Unit): CleanResult =
        withContext(Dispatchers.IO) {
            var freed = 0L
            val removedPaths = ArrayList<String>()
            items.forEachIndexed { index, item ->
                ensureActive()
                val ok = item.uri?.let { DocumentWalker.delete(context, it) } ?: File(item.path).delete()
                if (ok) {
                    freed += item.sizeBytes
                    removedPaths += item.path
                }
                onProgress(index + 1)
            }
            CleanResult(freed, removedPaths.size, removedPaths)
        }

    private fun appCacheFiles(): List<File> =
        listOfNotNull(context.cacheDir, context.externalCacheDir).flatMap { dir ->
            dir.walkTopDown().filter { it.isFile && it.length() > 0 }.toList()
        }

    private fun classify(path: String, name: String, size: Long, modified: Long): JunkCategory? {
        val lower = name.lowercase()
        val ext = lower.substringAfterLast('.', "")
        return when {
            lower.startsWith(".trashed-") || lower.contains(".trashed") || path.contains("/.trash", ignoreCase = true) -> JunkCategory.TRASH
            ext == "apk" -> JunkCategory.APK
            lower.startsWith(".thumbdata") || path.contains("/.thumbnails/") -> JunkCategory.THUMBNAILS
            ext in TEMP_EXTENSIONS -> JunkCategory.TEMP
            size >= LARGE_FILE_BYTES -> JunkCategory.LARGE_FILES
            else -> null
        }
    }

    private companion object {
        const val EMIT_INTERVAL_MS = 200L
        const val LARGE_FILE_BYTES = 100L * 1024 * 1024
        val TEMP_EXTENSIONS = setOf("tmp", "temp", "log", "bak", "old", "cache", "dmp", "chk", "crdownload", "part")
    }
}
