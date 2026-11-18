package com.phonecleaner.aicleaner.data.files

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.phonecleaner.aicleaner.data.storage.DocEntry
import com.phonecleaner.aicleaner.data.storage.DocumentWalker
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.domain.StorageInfo
import com.phonecleaner.aicleaner.domain.model.CategorySummary
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FilesRepository(private val context: Context) {

    private val lock = Mutex()
    private var cache: Cache? = null

    private class Cache(val key: String, val entries: List<FileEntry>)

    suspend fun list(category: FileCategory, showHidden: Boolean): List<FileEntry> =
        all(showHidden).filter { it.category == category }.sortedByDescending { it.modifiedMs }

    suspend fun summaries(showHidden: Boolean): Map<FileCategory, CategorySummary> {
        val entries = all(showHidden)
        return FileCategory.entries.associateWith { category ->
            val files = entries.filter { it.category == category }
            CategorySummary(files.size, files.sumOf { it.sizeBytes })
        }
    }

    suspend fun delete(entries: List<FileEntry>): List<FileEntry> = withContext(Dispatchers.IO) {
        val removed = entries.filter { DocumentWalker.delete(context, it.uri) }
        if (removed.isNotEmpty()) cache = null
        removed
    }

    fun refresh() {
        cache = null
    }

    fun storage(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        return StorageInfo(stat.totalBytes, stat.totalBytes - stat.availableBytes, stat.availableBytes)
    }

    private suspend fun all(showHidden: Boolean): List<FileEntry> = lock.withLock {
        val roots = FolderAccess.mediaRoots(context)
        val key = roots.joinToString("|") + showHidden
        cache?.takeIf { it.key == key }?.let { return@withLock it.entries }
        val found = ArrayList<FileEntry>()
        withContext(Dispatchers.IO) {
            roots.forEach { root ->
                DocumentWalker.walk(context, root, showHidden) { doc ->
                    categoryOf(doc)?.let { found += doc.toEntry(it) }
                }
            }
        }
        val entries = found.distinctBy { it.uri }
        cache = Cache(key, entries)
        entries
    }

    private fun categoryOf(doc: DocEntry): FileCategory? = when {
        doc.mime.startsWith("image/") -> FileCategory.IMAGES
        doc.mime.startsWith("video/") -> FileCategory.VIDEO
        doc.mime.startsWith("audio/") -> FileCategory.AUDIO
        else -> null
    }

    private fun DocEntry.toEntry(category: FileCategory) =
        FileEntry(uri, id, path, name, sizeBytes, modifiedMs, mime, category)
}
