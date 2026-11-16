package com.phonecleaner.aicleaner.presentation.ui.screens.other_screens.app_language.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import network.chaintech.sdpcomposemultiplatform.sdp

@Composable
fun LanguageFlagIcon(
    flagCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flagBytes = remember(flagCode) {
        runCatching {
            context.assets.open("flags/$flagCode.svg").use { it.readBytes() }
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(32.sdp)
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
            .background(Color(0xFFE8E8E8))
    ) {
        if (flagBytes != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(flagBytes)
                    .memoryCacheKey("flag_$flagCode")
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
