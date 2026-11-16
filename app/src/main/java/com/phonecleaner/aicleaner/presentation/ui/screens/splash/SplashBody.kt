package com.phonecleaner.aicleaner.presentation.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import kotlin.math.min

/**
 * Splash content: the logo with the app name and tagline, a soft background that hints at cleaning
 * (faint bubbles and sparkles), and the loader with its status text at the bottom.
 */
@Composable
fun SplashBody(
    logoScale: () -> Float = { 1f },
    onLogoPositioned: (Offset) -> Unit = {}
) {
    val background = MaterialTheme.colorScheme.background
    val dark = background.red < 0.3f
    val tintTop = if (dark) Color(0xFF0E2A55) else Color(0xFFDCEBFF)

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(background, tintTop)))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val bubble = Brand.Blue.copy(alpha = if (dark) 0.10f else 0.07f)
            drawCircle(bubble, radius = w * 0.34f, center = Offset(w * 0.04f, h * 0.88f))
            drawCircle(bubble, radius = w * 0.24f, center = Offset(w * 0.98f, h * 0.18f))
            drawCircle(bubble, radius = w * 0.12f, center = Offset(w * 0.86f, h * 0.76f))
            drawCircle(bubble, radius = w * 0.07f, center = Offset(w * 0.12f, h * 0.22f))

            fun sparkle(cx: Float, cy: Float, r: Float, color: Color) {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    quadraticTo(cx, cy, cx + r, cy)
                    quadraticTo(cx, cy, cx, cy + r)
                    quadraticTo(cx, cy, cx - r, cy)
                    quadraticTo(cx, cy, cx, cy - r)
                    close()
                }
                drawPath(path, color)
            }
            val sparkleColor = Brand.Blue.copy(alpha = if (dark) 0.28f else 0.20f)
            sparkle(w * 0.20f, h * 0.34f, min(w, h) * 0.030f, sparkleColor)
            sparkle(w * 0.80f, h * 0.44f, min(w, h) * 0.022f, sparkleColor)
            sparkle(w * 0.70f, h * 0.30f, min(w, h) * 0.016f, Brand.Gold.copy(alpha = 0.45f))
            sparkle(w * 0.14f, h * 0.52f, min(w, h) * 0.018f, Brand.Gold.copy(alpha = 0.40f))
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pulse = rememberInfiniteTransition(label = "splashPulse")
            val ripple by pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "ripple")
            val sweep by pulse.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "sweep")
            val breathe by pulse.animateFloat(1f, 1.05f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
            Box(modifier = Modifier.size(230.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2, size.height / 2)
                    // Two soft ripples that expand from the logo and fade out.
                    listOf(ripple, (ripple + 0.5f) % 1f).forEach { t ->
                        drawCircle(
                            Brand.Blue.copy(alpha = 0.22f * (1f - t)),
                            radius = 78.dp.toPx() + 36.dp.toPx() * t,
                            center = c,
                            style = Stroke(3.dp.toPx())
                        )
                    }
                    // A gradient arc that circles the logo.
                    val r = 88.dp.toPx()
                    rotate(sweep, c) {
                        drawArc(
                            brush = Brush.sweepGradient(0f to Color.Transparent, 0.9f to Brand.BlueLight, 1f to Brand.Blue, center = c),
                            startAngle = 0f, sweepAngle = 300f, useCenter = false,
                            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(148.dp)
                        .onGloballyPositioned { onLogoPositioned(it.positionInWindow() + it.size.center.toOffset()) }
                        .graphicsLayer {
                            val s = logoScale() * breathe
                            scaleX = s
                            scaleY = s
                        }
                )
            }
            Text(
                text = stringResource(R.string.app_name_short),
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(Brand.BlueLight, Brand.Blue, Brand.BlueDark)),
                    fontSize = 30.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                modifier = Modifier.padding(top = 4.dp),
                color = Brand.Blue.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-34).dp)
        ) {
            Text(
                text = stringResource(R.string.starting_up),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
            )
            LottieSplashLoader()
        }
    }
}
