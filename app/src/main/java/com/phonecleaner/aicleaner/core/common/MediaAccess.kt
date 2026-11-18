package com.phonecleaner.aicleaner.core.common

import android.content.Context
import com.phonecleaner.aicleaner.domain.model.FileCategory

object MediaAccess {

    fun permissionsFor(category: FileCategory?): Array<String>? = null

    fun isGranted(context: Context, @Suppress("UNUSED_PARAMETER") category: FileCategory?): Boolean =
        StoragePermission.isGranted(context)
}
