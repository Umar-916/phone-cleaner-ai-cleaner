package com.phonecleaner.aicleaner.data.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

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

    private val COLUMNS = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    suspend fun walk(
        context: Context,
        tree: Uri,
        showHidden: Boolean = false,
        onFile: suspend (DocEntry) -> Unit
    ) {
        val resolver = context.contentResolver
        val rootId = DocumentsContract.getTreeDocumentId(tree)
        val stack = ArrayDeque<String>()
        stack.addLast(rootId)
        while (stack.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val parentId = stack.removeLast()
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentId)
            runCatching {
                resolver.query(children, COLUMNS, null, null, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0) ?: continue
                        val name = cursor.getString(1).orEmpty()
                        if (name == ".nomedia" || (!showHidden && name.startsWith("."))) continue
                        val mime = cursor.getString(2).orEmpty()
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            stack.addLast(id)
                        } else {
                            val size = cursor.getLong(3)
                            if (size <= 0L) continue
                            onFile(
                                DocEntry(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(tree, id),
                                    path = "/" + id.substringAfter(':'),
                                    name = name,
                                    sizeBytes = size,
                                    modifiedMs = cursor.getLong(4),
                                    mime = mime
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun delete(context: Context, uri: Uri): Boolean =
        runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }.getOrDefault(false)
}
