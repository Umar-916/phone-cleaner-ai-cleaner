package com.phonecleaner.aicleaner.presentation.ui.screens.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.model.FileEntry
import java.io.File
import java.util.Date

fun mimeFor(name: String, fallback: String = ""): String =
    fallback.ifBlank {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase()) ?: "*/*"
    }

fun openEntry(context: Context, entry: FileEntry) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(entry.uri, mimeFor(entry.name, entry.mime))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    tryCatch { context.startActivity(Intent.createChooser(intent, null)) }
}

fun openPath(context: Context, path: String) {
    val uri = fileUri(context, path) ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeFor(path.substringAfterLast('/')))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    tryCatch { context.startActivity(Intent.createChooser(intent, null)) }
}

fun shareEntries(context: Context, entries: List<FileEntry>) {
    if (entries.isEmpty()) return
    shareUris(context, entries.map { it.uri }, entries.first().let { mimeFor(it.name, it.mime) }.takeIf { entries.size == 1 } ?: "*/*")
}

fun sharePath(context: Context, path: String) {
    val uri = fileUri(context, path) ?: return
    shareUris(context, listOf(uri), mimeFor(path.substringAfterLast('/')))
}

private fun shareUris(context: Context, uris: List<Uri>, mime: String) {
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_STREAM, uris.first()) }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply { putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) }
    }.apply {
        type = mime
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    tryCatch { context.startActivity(Intent.createChooser(intent, null)) }
}

private fun fileUri(context: Context, path: String): Uri? = runCatching {
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
}.getOrNull()

fun formatDay(context: Context, millis: Long): String = DateFormat.getMediumDateFormat(context).format(Date(millis))

fun formatDateTime(context: Context, millis: Long): String =
    DateFormat.getMediumDateFormat(context).format(Date(millis)) + " " + DateFormat.getTimeFormat(context).format(Date(millis))

fun List<FileEntry>.groupedByDay(context: Context): List<Pair<String, List<FileEntry>>> =
    groupBy { formatDay(context, it.modifiedMs) }.toList()

fun openUri(context: Context, uri: Uri, mime: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    tryCatch { context.startActivity(Intent.createChooser(intent, null)) }
}
