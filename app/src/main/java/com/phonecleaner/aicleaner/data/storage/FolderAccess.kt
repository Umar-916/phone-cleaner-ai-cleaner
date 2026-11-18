package com.phonecleaner.aicleaner.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.net.toUri

object FolderAccess {

    private const val AUTHORITY = "com.android.externalstorage.documents"

    fun roots(context: Context): List<Uri> =
        context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission && it.isWritePermission && DocumentsContract.isTreeUri(it.uri) }
            .map { it.uri }

    fun whatsAppRoot(context: Context): Uri? = roots(context).firstOrNull { isWhatsApp(it) }

    fun mediaRoots(context: Context): List<Uri> = roots(context).filterNot { isWhatsApp(it) }

    fun persist(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    fun release(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    fun label(uri: Uri): String {
        val path = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrDefault("").substringAfter(':', "")
        return path.ifBlank { "Internal storage" }
    }

    fun mediaHint(): Uri? = hint("DCIM")

    fun whatsAppHint(): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) hint("Android/media/com.whatsapp/WhatsApp/Media") else hint("WhatsApp/Media")

    private fun isWhatsApp(uri: Uri): Boolean = label(uri).contains("whatsapp", ignoreCase = true)

    private fun hint(path: String): Uri? =
        runCatching { DocumentsContract.buildDocumentUri(AUTHORITY, "primary:$path") }.getOrNull()
            ?: "content://$AUTHORITY/document/primary%3A${Uri.encode(path)}".toUri()
}
