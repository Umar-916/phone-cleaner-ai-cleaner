package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand

/**
 * Hero control of the Home screen. A glossy disc sits inside a thick white ring and two faint halo rings,
 * all concentric around one centre, over a soft shadow. One short arc travels round the outer halo as a
 * true circle, the halos breathe slowly, and a faint dashed ring shows how full the storage is.
 */
@Composable
fun HeroOrb(
    usedFraction: Float,
    onClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "heroOrb")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "spin")
    val breathe by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val progress by animateFloatAsState(usedFraction.coerceIn(0f, 1f), tween(1000, easing = FastOutSlowInEasing), label = "usedProgress")

    Box(modifier = modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val halo = 3.dp.toPx() * breathe

            // Soft shadow only outside the rings, so the centre of the disc stays clean.
            drawCircle(
                brush = Brush.radialGradient(
                    0.70f to Color.Transparent,
                    0.84f to Color(0x2E051A5E),
                    1.0f to Color.Transparent,
                    center = Offset(c.x, c.y + 6.dp.toPx()),
                    radius = 100.dp.toPx()
                ),
                radius = 100.dp.toPx(),
                center = Offset(c.x, c.y + 6.dp.toPx())
            )

            // Two outer rings with a clear gap before the thin white ring; they breathe slowly.
            val outerR = 84.dp.toPx() + halo
            drawCircle(Color.White.copy(alpha = 0.16f), radius = outerR, center = c, style = Stroke(9.dp.toPx()))
            val midR = 72.dp.toPx() + halo / 2
            drawCircle(Color.White.copy(alpha = 0.24f), radius = midR, center = c, style = Stroke(7.dp.toPx()))

            // A snake-like arc travelling round the outer ring: thin tail growing into a bright head.
            val segments = 28
            val snakeSweep = 150f
            val segSweep = snakeSweep / segments
            val snakeTopLeft = Offset(c.x - outerR, c.y - outerR)
            val snakeSize = Size(outerR * 2, outerR * 2)
            for (i in 0 until segments) {
                val t = (i + 1) / segments.toFloat()
                drawArc(
                    color = Color.White.copy(alpha = 0.15f + 0.80f * t),
                    startAngle = spin - snakeSweep + i * segSweep,
                    sweepAngle = segSweep + 1.2f,
                    useCenter = false,
                    topLeft = snakeTopLeft,
                    size = snakeSize,
                    style = Stroke((1.5f + 3.5f * t).dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Thin white ring and the glossy disc.
            drawCircle(Color.White.copy(alpha = 0.95f), radius = 61.dp.toPx(), center = c, style = Stroke(4.dp.toPx()))
            val disc = 57.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4FA9FF), Color(0xFF1B66E8)),
                    center = Offset(c.x - 10.dp.toPx(), c.y - 14.dp.toPx()),
                    radius = disc * 1.6f
                ),
                radius = disc,
                center = c
            )

            // Faint dashed storage ring inside the white ring.
            val inner = 50.dp.toPx()
            val dashed = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx()))
            val innerTopLeft = Offset(c.x - inner, c.y - inner)
            val innerSize = Size(inner * 2, inner * 2)
            drawArc(Color.White.copy(alpha = 0.14f), 0f, 360f, false, innerTopLeft, innerSize, style = Stroke(1.5.dp.toPx(), pathEffect = dashed))
            drawArc(Color.White.copy(alpha = 0.55f), -90f, 360f * progress, false, innerTopLeft, innerSize, style = Stroke(1.5.dp.toPx(), pathEffect = dashed))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.home_junk_files),
                color = Color.White,
                style = TextStyle(fontSize = 17.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(Color.White)
                    .clickable(onClick = onClean)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.home_clean),
                    color = Brand.BlueDark,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
