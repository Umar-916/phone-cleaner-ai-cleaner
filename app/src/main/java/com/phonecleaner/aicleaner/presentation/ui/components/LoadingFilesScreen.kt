package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette
import kotlinx.coroutines.delay

private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))

/**
 * Full-screen "Loading files" step: a folder-and-magnifier picture that sways gently, a percentage counting
 * up to 100 and a caption that flips to "loaded" at the end, after which [onFinished] runs once.
 */
@Composable
fun LoadingFilesScreen(
    @StringRes loadingLabel: Int = R.string.loading_files,
    onFinished: () -> Unit
) {
    var percent by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    BackHandler {}

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(2600, easing = FastOutSlowInEasing)) { percent = (value * 100).toInt() }
        percent = 100
        delay(450)
        onFinished()
    }

    val transition = rememberInfiniteTransition(label = "loadingSway")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingPalette.top, OnboardingPalette.bottom))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.9f))
        Image(
            painter = painterResource(R.drawable.loading_art),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(500f / 440f)
                .graphicsLayer {
                    translationX = sway * 14.dp.toPx()
                    rotationZ = sway * 2f
                }
                .featheredEdges(0.14f)
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
            Text(
                "$percent",
                color = OnboardingPalette.ink,
                style = TextStyle(fontSize = 52.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Text(
                "%",
                modifier = Modifier.padding(start = 2.dp, top = 8.dp),
                color = OnboardingPalette.ink,
                style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
        }
        Text(
            stringResource(if (percent >= 100) R.string.files_loaded else loadingLabel),
            color = OnboardingPalette.muted,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(120.dp))
    }
}
