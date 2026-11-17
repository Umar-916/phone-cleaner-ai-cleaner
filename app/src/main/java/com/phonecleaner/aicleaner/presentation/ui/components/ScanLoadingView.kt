package com.phonecleaner.aicleaner.presentation.ui.components

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import kotlinx.coroutines.delay

private val bold = AppFonts.Bold
private val medium = AppFonts.Medium

@Composable
fun ScanLoadingView(
    progress: Float?,
    finished: Boolean,
    label: String,
    p: Animatable<Float, androidx.compose.animation.core.AnimationVector1D> = remember { Animatable(0f) },
    onFull: () -> Unit = {}
) {
    com.phonecleaner.aicleaner.presentation.ui.theme.ForceLightTheme { ScanLoadingContent(progress, finished, label, p, onFull) }
}

@Composable
private fun ScanLoadingContent(
    progress: Float?,
    finished: Boolean,
    label: String,
    p: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    onFull: () -> Unit
) {
    val percent = (p.value * 100).toInt()

    LaunchedEffect(finished, progress) {
        when {
            finished -> {
                p.animateTo(1f, tween(((1f - p.value) * 1800).toInt().coerceAtLeast(900), easing = FastOutSlowInEasing))
                delay(350)
                onFull()
            }
            progress != null -> p.animateTo(progress.coerceIn(0f, 0.97f), tween(450, easing = FastOutSlowInEasing))
            else -> p.animateTo(0.95f, tween(12000, easing = LinearOutSlowInEasing))
        }
    }

    val t = rememberInfiniteTransition(label = "scanArt")
    val sway by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway")
    val bob by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val breathe by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")

    Column(
        Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette.top, com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette.bottom))).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.8f))
        Image(
            painter = painterResource(R.drawable.loading_art),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(500f / 440f)
                .graphicsLayer {
                    translationX = sway * 12.dp.toPx()
                    translationY = bob * 8.dp.toPx()
                    rotationZ = sway * 2f
                    val s = 1f + breathe * 0.03f
                    scaleX = s; scaleY = s
                }
                .featheredEdges(0.14f)
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
            Text(
                "$percent",
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 52.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Text(
                "%",
                modifier = Modifier.padding(start = 2.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
        }
        Text(
            label,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(120.dp))
    }
}
