package com.phonecleaner.aicleaner.data.scanner

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.SystemClock
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
        val root = Environment.getExternalStorageDirectory()
        val items = ArrayList<JunkItem>()
        var foundBytes = 0L
        var visited = 0
        val categoryBytes = HashMap<JunkCategory, Long>()
        var lastEmit = 0L
        val stack = ArrayDeque<File>()
        stack.addLast(root)

        while (stack.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val dir = stack.removeLast()
            visited++
            val children = dir.listFiles() ?: continue

            if (children.isEmpty() && dir != root && !isProtectedDirectory(dir)) {
                items += JunkItem(dir.absolutePath, dir.name, 0L, dir.lastModified(), JunkCategory.EMPTY_FOLDERS, true)
                continue
            }

            for (child in children) {
                if (child.isDirectory) {
                    if (dir == root && child.name.equals("Android", ignoreCase = true)) continue
                    stack.addLast(child)
                } else {
                    classify(child)?.let {
                        items += it
                        foundBytes += it.sizeBytes
                        categoryBytes[it.category] = (categoryBytes[it.category] ?: 0L) + it.sizeBytes
                    }
                }
            }

            val now = SystemClock.elapsedRealtime()
            if (now - lastEmit >= EMIT_INTERVAL_MS) {
                lastEmit = now
                emit(
                    ScanEvent.Progress(
                        ScanProgress(dir.absolutePath, foundBytes, items.size, visited.toFloat() / (visited + stack.size), HashMap(categoryBytes))
                    )
                )
            }
        }
        emit(ScanEvent.Finished(items))
    }.flowOn(Dispatchers.IO)

    suspend fun delete(items: List<JunkItem>, onProgress: (Int) -> Unit): CleanResult =
        withContext(Dispatchers.IO) {
            var freed = 0L
            val removedPaths = ArrayList<String>()
            val removedFiles = ArrayList<String>()
            items.forEachIndexed { index, item ->
                ensureActive()
                val file = File(item.path)
                if (file.exists() && file.delete()) {
                    freed += item.sizeBytes
                    removedPaths += item.path
                    if (!item.isDirectory) removedFiles += item.path
                }
                onProgress(index + 1)
            }
            if (removedFiles.isNotEmpty()) {
                MediaScannerConnection.scanFile(context, removedFiles.toTypedArray(), null, null)
            }
            CleanResult(freed, removedPaths.size, removedPaths)
        }

    private fun classify(file: File): JunkItem? {
        val lower = file.name.lowercase()
        val ext = lower.substringAfterLast('.', "")
        val path = file.absolutePath
        val size = file.length()
        val modified = file.lastModified()

        val category = when {
            lower.startsWith(".trashed-") || lower.contains(".trashed") || path.contains("/.trash", ignoreCase = true) -> JunkCategory.TRASH
            ext == "apk" -> JunkCategory.APK
            lower.startsWith(".thumbdata") || path.contains("/.thumbnails/") -> JunkCategory.THUMBNAILS
            ext in TEMP_EXTENSIONS -> JunkCategory.TEMP
            isInDownloads(path) && System.currentTimeMillis() - modified > OLD_DOWNLOAD_AGE_MS -> JunkCategory.OLD_DOWNLOADS
            size >= LARGE_FILE_BYTES -> JunkCategory.LARGE_FILES
            else -> return null
        }
        if (size <= 0L && category != JunkCategory.TEMP) return null
        return JunkItem(path, file.name, size, modified, category)
    }

    private fun isInDownloads(path: String): Boolean =
        path.contains("/Download/") || path.contains("/Downloads/")

    private fun isProtectedDirectory(dir: File): Boolean =
        dir.name.startsWith(".") || dir.parentFile == Environment.getExternalStorageDirectory()

    private companion object {
        const val EMIT_INTERVAL_MS = 120L
        const val LARGE_FILE_BYTES = 100L * 1024 * 1024
        const val OLD_DOWNLOAD_AGE_MS = 30L * 24 * 60 * 60 * 1000
        val TEMP_EXTENSIONS = setOf("tmp", "temp", "log", "bak", "old", "cache", "dmp", "chk", "crdownload", "part")
    }
}
