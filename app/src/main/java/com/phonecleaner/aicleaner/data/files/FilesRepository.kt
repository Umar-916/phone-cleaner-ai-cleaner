package com.phonecleaner.aicleaner.data.files

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.StatFs
import com.phonecleaner.aicleaner.data.storage.DocEntry
import com.phonecleaner.aicleaner.data.storage.DocumentWalker
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.domain.StorageInfo
import com.phonecleaner.aicleaner.domain.model.BrowserItem
import com.phonecleaner.aicleaner.domain.model.CategorySummary
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class FilesRepository(private val context: Context) {

    private val lock = Mutex()
    private var cache: Cache? = null

    private class Cache(val key: String, val docs: List<DocEntry>)

    suspend fun list(category: FileCategory, showHidden: Boolean): List<FileEntry> =
        all(showHidden).filter { matches(category, it) }.map { it.toEntry(category) }.sortedByDescending { it.modifiedMs }

    suspend fun summaries(showHidden: Boolean): Map<FileCategory, CategorySummary> {
        val docs = all(showHidden)
        return FileCategory.entries.associateWith { category ->
            val files = docs.filter { matches(category, it) }
            CategorySummary(files.size, files.sumOf { it.sizeBytes })
        }
    }

    suspend fun delete(entries: List<FileEntry>): List<FileEntry> = withContext(Dispatchers.IO) {
        val removed = entries.filter { DocumentWalker.delete(context, it.uri) }
        if (removed.isNotEmpty()) {
            cache = null
            MediaScannerConnection.scanFile(context, removed.mapNotNull { it.uri.path }.toTypedArray(), null, null)
        }
        removed
    }

    fun refresh() {
        cache = null
    }

    suspend fun browse(directory: File, showHidden: Boolean): List<BrowserItem> = withContext(Dispatchers.IO) {
        directory.listFiles().orEmpty()
            .filter { showHidden || !it.name.startsWith(".") }
            .map { file ->
                BrowserItem(
                    path = file.absolutePath,
                    name = file.name,
                    isDirectory = file.isDirectory,
                    sizeBytes = if (file.isDirectory) 0L else file.length(),
                    modifiedMs = file.lastModified(),
                    childCount = if (file.isDirectory) file.list()?.size ?: 0 else 0
                )
            }
            .sortedWith(compareByDescending<BrowserItem> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    suspend fun deletePaths(paths: List<String>): Int = withContext(Dispatchers.IO) {
        val removed = paths.filter { path ->
            runCatching { File(path).let { if (it.isDirectory) it.deleteRecursively() else it.delete() } }.getOrDefault(false)
        }
        if (removed.isNotEmpty()) {
            cache = null
            MediaScannerConnection.scanFile(context, removed.toTypedArray(), null, null)
        }
        removed.size
    }

    fun rootDirectory(): File = Environment.getExternalStorageDirectory()

    suspend fun createFolder(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val target = File(parent, name)
        runCatching { !target.exists() && target.mkdirs() }.getOrDefault(false)
    }

    fun storage(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        return StorageInfo(stat.totalBytes, stat.totalBytes - stat.availableBytes, stat.availableBytes)
    }

    private suspend fun all(showHidden: Boolean): List<DocEntry> = lock.withLock {
        val roots = FolderAccess.mediaRoots(context)
        val key = roots.joinToString("|") + showHidden
        cache?.takeIf { it.key == key }?.let { return@withLock it.docs }
        val found = ArrayList<DocEntry>()
        withContext(Dispatchers.IO) {
            roots.forEach { root -> DocumentWalker.walk(context, root, showHidden) { found += it } }
        }
        val docs = found.distinctBy { it.uri }
        cache = Cache(key, docs)
        docs
    }

    private fun matches(category: FileCategory, doc: DocEntry): Boolean {
        val ext = doc.name.substringAfterLast('.', "").lowercase()
        return when (category) {
            FileCategory.IMAGES -> doc.mime.startsWith("image/")
            FileCategory.VIDEO -> doc.mime.startsWith("video/")
            FileCategory.AUDIO -> doc.mime.startsWith("audio/")
            FileCategory.APK -> ext == "apk"
            FileCategory.DOWNLOAD -> doc.path.contains("/Download/", ignoreCase = true) || doc.path.contains("/Downloads/", ignoreCase = true)
            FileCategory.DOCUMENTS -> ext in DOCUMENT_EXTENSIONS
        }
    }

    private fun DocEntry.toEntry(category: FileCategory) =
        FileEntry(uri, id, path, name, sizeBytes, modifiedMs, mime, category)

    private companion object {
        val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "odt", "rtf", "txt", "xls", "xlsx", "csv", "ods", "ppt", "pptx", "odp")
    }
}
