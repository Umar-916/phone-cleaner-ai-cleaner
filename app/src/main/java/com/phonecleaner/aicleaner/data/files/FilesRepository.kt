package com.phonecleaner.aicleaner.data.files

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.phonecleaner.aicleaner.domain.StorageInfo
import com.phonecleaner.aicleaner.domain.model.BrowserItem
import com.phonecleaner.aicleaner.domain.model.CategorySummary
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FilesRepository(private val context: Context) {

    private val resolver = context.contentResolver

    suspend fun list(category: FileCategory, showHidden: Boolean): List<FileEntry> = withContext(Dispatchers.IO) {
        val source = sourceFor(category)
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE,
            DATA
        )
        val result = ArrayList<FileEntry>()
        runCatching {
            resolver.query(source.collection, projection, source.selection, source.args, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dataCol = cursor.getColumnIndexOrThrow(DATA)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol).orEmpty()
                    val size = cursor.getLong(sizeCol)
                    if (path.isEmpty() || size <= 0L) continue
                    if (!showHidden && isHidden(path)) continue
                    val id = cursor.getLong(idCol)
                    result += FileEntry(
                        uri = ContentUris.withAppendedId(source.collection, id),
                        id = id,
                        path = path,
                        name = cursor.getString(nameCol) ?: path.substringAfterLast('/'),
                        sizeBytes = size,
                        modifiedMs = cursor.getLong(dateCol) * 1000L,
                        mime = cursor.getString(mimeCol).orEmpty(),
                        category = category
                    )
                }
            }
        }
        result
    }

    suspend fun summaries(showHidden: Boolean): Map<FileCategory, CategorySummary> = withContext(Dispatchers.IO) {
        FileCategory.entries.associateWith { category ->
            val files = list(category, showHidden)
            CategorySummary(files.size, files.sumOf { it.sizeBytes })
        }
    }

    suspend fun delete(entries: List<FileEntry>): List<FileEntry> = withContext(Dispatchers.IO) {
        val removed = entries.filter { runCatching { File(it.path).delete() }.getOrDefault(false) }
        if (removed.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, removed.map { it.path }.toTypedArray(), null, null)
        }
        removed
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
        if (removed.isNotEmpty()) MediaScannerConnection.scanFile(context, removed.toTypedArray(), null, null)
        removed.size
    }

    fun rootDirectory(): File = Environment.getExternalStorageDirectory()

    fun storage(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        return StorageInfo(stat.totalBytes, stat.totalBytes - stat.availableBytes, stat.availableBytes)
    }

    private fun isHidden(path: String): Boolean = path.contains("/.")

    private class Source(val collection: Uri, val selection: String?, val args: Array<String>?)

    private fun sourceFor(category: FileCategory): Source {
        val files = MediaStore.Files.getContentUri("external")
        return when (category) {
            FileCategory.IMAGES -> Source(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null)
            FileCategory.VIDEO -> Source(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null)
            FileCategory.AUDIO -> Source(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null)
            FileCategory.APK -> Source(files, "LOWER($DATA) LIKE ?", arrayOf("%.apk"))
            FileCategory.DOWNLOAD -> Source(files, "$DATA LIKE ?", arrayOf("%/${Environment.DIRECTORY_DOWNLOADS}/%"))
            FileCategory.DOCUMENTS -> {
                val extensions = DOCUMENT_EXTENSIONS
                Source(files, extensions.joinToString(" OR ") { "LOWER($DATA) LIKE ?" }, extensions.map { "%.$it" }.toTypedArray())
            }
        }
    }

    private companion object {
        @Suppress("DEPRECATION")
        const val DATA = MediaStore.MediaColumns.DATA
        val DOCUMENT_EXTENSIONS = listOf("pdf", "doc", "docx", "odt", "rtf", "txt", "xls", "xlsx", "csv", "ods", "ppt", "pptx", "odp")
    }
}
