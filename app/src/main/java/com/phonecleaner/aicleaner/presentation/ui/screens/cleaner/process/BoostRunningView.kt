package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold

@Composable
fun BoostRunningView(percent: Int, label: String, @DrawableRes badge: Int? = null, valueText: String = "") {
    com.phonecleaner.aicleaner.presentation.ui.theme.ForceLightTheme { BoostRunningContent(percent, label, badge, valueText) }
}

@Composable
private fun BoostRunningContent(percent: Int, label: String, @DrawableRes badge: Int?, valueText: String) {
    val t = rememberInfiniteTransition(label = "boostRipple")
    val ripple by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "ripple")
    val bob by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val shown by animateIntAsState(percent, tween(250), label = "pct")
    val rippleColor = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingPalette.top, OnboardingPalette.bottom))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(0.8f).aspectRatio(1.25f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height * 0.80f)
                listOf(0f, 0.33f, 0.66f).forEach { off ->
                    val p = (ripple + off) % 1f
                    val rw = size.width * (0.25f + 0.40f * p)
                    drawOval(
                        color = rippleColor.copy(alpha = 0.30f * (1f - p)),
                        topLeft = Offset(c.x - rw, c.y - rw * 0.28f),
                        size = Size(rw * 2f, rw * 0.56f)
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.infocenter_art),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .graphicsLayer { translationY = bob * 8.dp.toPx() - 14.dp.toPx() }
                    .featheredEdges(0.24f)
            )
            if (badge != null) {
                Crossfade(
                    targetState = badge,
                    label = "badge",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 34.dp)
                ) { res ->
                    Image(
                        painterResource(res), null,
                        modifier = Modifier.size(48.dp).graphicsLayer {
                            translationY = bob * -6.dp.toPx()
                            scaleX = 1f + 0.08f * ripple
                            scaleY = 1f + 0.08f * ripple
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "boostLabel"
        ) { text ->
            Text(
                text,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                color = ink,
                style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
        if (valueText.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(valueText, color = MaterialTheme.colorScheme.primary, style = TextStyle(fontSize = 28.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.weight(1f))
        Text("$shown %", color = ink, style = TextStyle(fontSize = 34.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(72.dp))
    }
}
