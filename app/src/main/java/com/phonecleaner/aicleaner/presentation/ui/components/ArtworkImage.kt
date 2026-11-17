package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.annotation.DrawableRes
import com.phonecleaner.aicleaner.R
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.SubcomposeAsyncImage
import network.chaintech.sdpcomposemultiplatform.sdp

@Composable
fun ArtworkImage(
    url: String?,
    @DrawableRes fallback: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    showShimmerOnLoading: Boolean = false
) {
    val fallbackPainter = painterResource(fallback)

    val isUserPhoto = url?.startsWith("file://") == true
    val photoModifier = if (isUserPhoto) modifier.padding(7.dp).clip(RoundedCornerShape(14.dp)) else modifier
    val photoScale = if (isUserPhoto) ContentScale.Crop else contentScale
    val isOnlinePlaceholder = showShimmerOnLoading || fallback == R.drawable.app_logo

    val isGenericFallback = fallback == R.drawable.app_logo

    if (url.isNullOrBlank()) {
        if (isGenericFallback) {
            Box(modifier = modifier)
        } else {
            Image(
                painter = fallbackPainter,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
                alignment = alignment
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            modifier = photoModifier,
            contentScale = photoScale,
            alignment = alignment,
            loading = {
                if (isOnlinePlaceholder) {
                    ShimmerLoaderBox(modifier = Modifier.fillMaxSize())
                } else {
                    Image(
                        painter = fallbackPainter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale,
                        alignment = alignment
                    )
                }
            },
            error = {
                if (isGenericFallback) {
                    Box(modifier = Modifier.fillMaxSize())
                } else {
                    Image(
                        painter = fallbackPainter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale, 
                        alignment = alignment
                    )
                }
            }
        )
    }
}

@Composable
fun ShimmerLoaderBox(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmerBox")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnimation"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.7f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .background(brush, RoundedCornerShape(12.sdp))
    )
}

