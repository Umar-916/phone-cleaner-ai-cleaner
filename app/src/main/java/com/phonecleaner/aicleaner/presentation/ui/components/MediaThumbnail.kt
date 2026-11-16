package com.phonecleaner.aicleaner.presentation.ui.components

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.data.media.MediaThumbs
import com.phonecleaner.aicleaner.domain.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val thumbCache = LruCache<Long, ImageBitmap>(120)

/** Square thumbnail of a MediaStore photo or video, decoded off the main thread and cached. */
@Composable
fun MediaThumbnail(file: MediaFile, modifier: Modifier = Modifier, sizePx: Int = 320) {
    val context = LocalContext.current
    val key = file.id * 2 + if (file.isVideo) 1 else 0
    val bitmap by produceState(initialValue = thumbCache[key], key) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                MediaThumbs.load(context, file, sizePx)?.asImageBitmap()
            }?.also { thumbCache.put(key, it) }
        }
    }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (file.isVideo) {
            Icon(
                Icons.Rounded.PlayCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(30.dp)
            )
        }
    }
}
