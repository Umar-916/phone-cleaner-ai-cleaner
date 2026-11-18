package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.domain.model.MediaFile
import com.phonecleaner.aicleaner.presentation.ui.components.MediaThumbnail
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

fun AiKind.icon(): ImageVector = when (this) {
    AiKind.VIDEOS -> Icons.Rounded.Videocam
    AiKind.AUDIO -> Icons.Rounded.AudioFile
    AiKind.LARGE_FILES -> Icons.Rounded.Description
    AiKind.SCREENSHOTS -> Icons.Rounded.Screenshot
    AiKind.OLD_DOWNLOADS -> Icons.Rounded.Download
    AiKind.BLURRY -> Icons.Rounded.BlurOn
}

fun AiKind.tint(): Color = when (this) {
    AiKind.VIDEOS -> Brand.Duplicates
    AiKind.AUDIO -> Brand.Process
    AiKind.LARGE_FILES -> Brand.Junk
    AiKind.SCREENSHOTS -> Brand.Photo
    AiKind.OLD_DOWNLOADS -> Brand.Junk
    AiKind.BLURRY -> Brand.Ai
}

fun AiKind.titleRes(): Int = when (this) {
    AiKind.VIDEOS -> R.string.ai_kind_videos
    AiKind.AUDIO -> R.string.ai_kind_audio
    AiKind.LARGE_FILES -> R.string.ai_kind_large_files
    AiKind.SCREENSHOTS -> R.string.ai_kind_screenshots
    AiKind.OLD_DOWNLOADS -> R.string.ai_kind_downloads
    AiKind.BLURRY -> R.string.ai_kind_blurry
}

private fun FileEntry.asMediaFile() =
    MediaFile(id, uri, name, sizeBytes, modifiedMs, category == FileCategory.VIDEO, 0, 0, 0L)

@Composable
fun AiEntryThumb(entry: FileEntry, kind: AiKind, modifier: Modifier = Modifier) {
    Box(modifier) {
        if (entry.isMedia) {
            MediaThumbnail(entry.asMediaFile(), Modifier.fillMaxSize(), 240)
        } else {
            val tint = kind.tint()
            Box(Modifier.fillMaxSize().background(tint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(kind.icon(), null, tint = tint, modifier = Modifier.size(34.dp))
            }
        }
        if (entry.category == FileCategory.VIDEO) {
            Box(
                Modifier.align(Alignment.Center).size(30.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).padding(3.dp),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}
