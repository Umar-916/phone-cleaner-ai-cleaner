package com.phonecleaner.aicleaner.data.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File

data class DocEntry(
    val uri: Uri,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val mime: String
) {
    val id: Long get() = uri.toString().fold(1125899906842597L) { hash, c -> 31 * hash + c.code }
}

object DocumentWalker {

    suspend fun walk(
        context: Context,
        tree: Uri,
        showHidden: Boolean = false,
        onFile: suspend (DocEntry) -> Unit
    ) {
        walkFiles(File(tree.path.orEmpty()), showHidden, onFile)
    }

    private suspend fun walkFiles(root: File, showHidden: Boolean, onFile: suspend (DocEntry) -> Unit) {
        val external = Environment.getExternalStorageDirectory().path
        val stack = ArrayDeque<File>()
        stack.addLast(root)
        while (stack.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val dir = stack.removeLast()
            val children = dir.listFiles() ?: continue
            for (child in children) {
                val name = child.name
                if (name == ".nomedia" || (!showHidden && name.startsWith("."))) continue
                if (child.isDirectory) {
                    val isAndroidRoot = root.path == external && child.parentFile?.path == external && name.equals("Android", ignoreCase = true)
                    if (!isAndroidRoot) stack.addLast(child)
                } else {
                    val size = child.length()
                    if (size <= 0L) continue
                    val ext = name.substringAfterLast('.', "").lowercase()
                    onFile(
                        DocEntry(
                            uri = Uri.fromFile(child),
                            path = child.absolutePath.removePrefix(external),
                            name = name,
                            sizeBytes = size,
                            modifiedMs = child.lastModified(),
                            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext).orEmpty()
                        )
                    )
                }
            }
        }
    }

    fun delete(@Suppress("UNUSED_PARAMETER") context: Context, uri: Uri): Boolean =
        runCatching { File(uri.path.orEmpty()).delete() }.getOrDefault(false)
}
