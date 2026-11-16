package com.phonecleaner.aicleaner.core.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.phonecleaner.aicleaner.domain.model.FileCategory

/**
 * Photos, videos and audio can be listed with the light media permissions; only documents, APKs,
 * downloads and the folder browser need the broader file access.
 */
object MediaAccess {

    fun permissionsFor(category: FileCategory?): Array<String>? {
        val tiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        return when (category) {
            FileCategory.IMAGES -> arrayOf(if (tiramisu) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE)
            FileCategory.VIDEO -> arrayOf(if (tiramisu) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_EXTERNAL_STORAGE)
            FileCategory.AUDIO -> arrayOf(if (tiramisu) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> null
        }
    }

    fun isGranted(context: Context, category: FileCategory?): Boolean {
        if (StoragePermission.isGranted(context)) return true
        val needed = permissionsFor(category) ?: return false
        return needed.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }
}
