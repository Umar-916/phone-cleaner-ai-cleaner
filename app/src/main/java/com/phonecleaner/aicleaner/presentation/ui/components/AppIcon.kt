package com.phonecleaner.aicleaner.presentation.ui.components

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val iconCache = LruCache<String, ImageBitmap>(200)

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState(initialValue = iconCache[packageName], packageName) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    context.packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
                }
            }.getOrNull()?.also { iconCache.put(packageName, it) }
        }
    }
    val icon = bitmap
    if (icon != null) {
        Image(bitmap = icon, contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)))
    }
}
