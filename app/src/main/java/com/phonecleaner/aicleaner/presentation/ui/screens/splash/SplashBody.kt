package com.phonecleaner.aicleaner.presentation.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center as intSizeCenter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.presentation.ui.theme.SplashInk
import com.phonecleaner.aicleaner.presentation.ui.theme.SplashPurple
import kotlin.math.abs

// Source geometry of splash_robot.jpg / splash_brush.png (pixels of the reference artwork).
private const val ART_W = 836f
private const val ART_H = 760f
private val BRUSH_ORIGIN = Offset(598f, 236f)
private val BRUSH_PIVOT = Offset(628f, 392f)

/**
 * Splash content: the mascot waving her cleaning brush over a soft lavender backdrop, the app name in deep
 * purple, and the loader with its status text at the bottom. The brush is a separate layer that sways
 * around the hand, the mascot floats gently and a few sparkles twinkle around the brush.
 */
@Composable
fun SplashBody(
    logoScale: () -> Float = { 1f },
    onLogoPositioned: (Offset) -> Unit = {}
) {
    val robot = ImageBitmap.imageResource(R.drawable.splash_robot)
    val brush = ImageBitmap.imageResource(R.drawable.splash_brush)

    val transition = rememberInfiniteTransition(label = "splash")
    val float by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val sway by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway")
    val twinkle by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "twinkle")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SplashPurple.top, SplashPurple.bottom)))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val artWidth = minOf(maxWidth, 420.dp)
                val artHeight = artWidth * (ART_H / ART_W)
                Canvas(
                    modifier = Modifier
                        .size(artWidth, artHeight + 28.dp)
                        .onGloballyPositioned { onLogoPositioned(it.positionInWindow() + it.size.intSizeCenter.toOffset()) }
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                ) {
                    val k = size.width / ART_W
                    val bob = float * 5.dp.toPx()

                    // Floor shadow that shrinks a little while the mascot floats up.
                    val shadowW = size.width * (0.40f - 0.03f * float)
                    val shadowY = size.height - 14.dp.toPx()
                    drawOval(
                        brush = Brush.radialGradient(
                            listOf(Color(0x558A4DE0), Color.Transparent),
                            center = Offset(size.center.x, shadowY),
                            radius = shadowW / 2f
                        ),
                        topLeft = Offset(size.center.x - shadowW / 2f, shadowY - 9.dp.toPx()),
                        size = Size(shadowW, 18.dp.toPx())
                    )

                    scale(logoScale(), pivot = Offset(size.center.x, ART_H * k / 2f)) {
                        drawArtwork(robot, brush, k, bob, sway, twinkle)
                    }
                }
            }

            Text(
                text = stringResource(R.string.app_name_short),
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
                color = SplashInk,
                style = TextStyle(fontSize = 31.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-34).dp)
        ) {
            Text(
                text = stringResource(R.string.starting_up),
                color = Brand.Blue.copy(alpha = 0.75f),
                style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)), fontWeight = FontWeight.Medium)
            )
            LottieSplashLoader()
        }
    }
}

/** Draws the mascot with soft edges, then the swaying brush on top and a few twinkling sparkles. */
private fun DrawScope.drawArtwork(
    robot: ImageBitmap,
    brush: ImageBitmap,
    k: Float,
    bob: Float,
    sway: Float,
    twinkle: Float
) {
    val w = ART_W * k
    val h = ART_H * k
    val dx = (size.width - w) / 2f

    translate(left = dx, top = bob) {
        // Body: draw the picture, then fade its borders into the backdrop (alpha mask).
        drawImage(robot, dstSize = IntSize(w.toInt(), h.toInt()))
        val fade = 0.10f
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent, fade to Color.Black, (1f - fade) to Color.Black, 1f to Color.Transparent,
                startX = 0f, endX = w
            ),
            size = Size(w, h), blendMode = BlendMode.DstIn
        )
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent, fade to Color.Black, (1f - fade) to Color.Black, 1f to Color.Transparent,
                startY = 0f, endY = h
            ),
            size = Size(w, h), blendMode = BlendMode.DstIn
        )

        // Brush: sways around the grip of the hand.
        val angle = -9f + 12f * sway
        rotate(degrees = angle, pivot = BRUSH_PIVOT * k) {
            drawImage(
                brush,
                dstOffset = IntOffset((BRUSH_ORIGIN.x * k).toInt(), (BRUSH_ORIGIN.y * k).toInt()),
                dstSize = IntSize((brush.width * k).toInt(), (brush.height * k).toInt())
            )
        }

        // Sparkles that flash around the brush, out of phase with each other.
        fun sparkle(cx: Float, cy: Float, r: Float, phase: Float) {
            val t = (twinkle + phase) % 1f
            val a = (1f - abs(2f * t - 1f)).coerceIn(0f, 1f)
            val rr = r * (0.55f + 0.45f * a) * k
            val p = Path().apply {
                moveTo(cx * k, cy * k - rr)
                quadraticTo(cx * k, cy * k, cx * k + rr, cy * k)
                quadraticTo(cx * k, cy * k, cx * k, cy * k + rr)
                quadraticTo(cx * k, cy * k, cx * k - rr, cy * k)
                quadraticTo(cx * k, cy * k, cx * k, cy * k - rr)
                close()
            }
            drawPath(p, Color.White.copy(alpha = 0.35f + 0.65f * a))
        }
        sparkle(600f, 168f, 26f, 0f)
        sparkle(645f, 195f, 14f, 0.45f)
        sparkle(762f, 392f, 24f, 0.7f)
    }
}
