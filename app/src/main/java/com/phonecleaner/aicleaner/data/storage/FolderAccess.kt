package com.phonecleaner.aicleaner.data.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.phonecleaner.aicleaner.core.common.StoragePermission
import java.io.File

object FolderAccess {

    fun hasAllFiles(context: Context): Boolean = StoragePermission.isGranted(context)

    fun mediaRoots(context: Context): List<Uri> =
        if (hasAllFiles(context)) listOf(Uri.fromFile(Environment.getExternalStorageDirectory())) else emptyList()

    fun whatsAppRoots(context: Context): List<Uri> {
        if (!hasAllFiles(context)) return emptyList()
        val external = Environment.getExternalStorageDirectory()
        return listOf(
            "Android/media/com.whatsapp/WhatsApp/Media",
            "WhatsApp/Media",
            "Android/media/com.whatsapp.w4b/WhatsApp Business/Media",
            "WhatsApp Business/Media"
        ).map { File(external, it) }.filter { it.isDirectory }.map { Uri.fromFile(it) }
    }

    fun hasWhatsAppAccess(context: Context): Boolean = hasAllFiles(context)

    fun isWhatsAppInstalled(context: Context): Boolean =
        listOf("com.whatsapp", "com.whatsapp.w4b").any { pkg -> runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess }
}
