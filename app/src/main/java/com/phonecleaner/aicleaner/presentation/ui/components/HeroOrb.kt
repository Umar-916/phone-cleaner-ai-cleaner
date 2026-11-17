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
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.draw.shadow
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
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "spin")
    val breathe by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val progress by animateFloatAsState(usedFraction.coerceIn(0f, 1f), tween(1000, easing = FastOutSlowInEasing), label = "usedProgress")

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val halo = 6.dp.toPx() * breathe

            // Two filled outer halos that breathe slowly (the "hover").
            drawCircle(Color.White.copy(alpha = 0.20f), radius = 92.dp.toPx() + halo, center = c)
            drawCircle(Color.White.copy(alpha = 0.30f), radius = 82.dp.toPx() + halo / 2, center = c)

            // Thick soft band between the halos and the disc, as in the design.
            drawCircle(Color.White.copy(alpha = 0.32f), radius = 77.dp.toPx(), center = c, style = Stroke(9.dp.toPx()))

            // Flat disc with one thin white ring.
            val ringR = 72.dp.toPx()
            drawCircle(Brush.radialGradient(listOf(Color(0xFFE9D4FB), Color(0xFFD2ADF7)), center = c, radius = ringR), radius = ringR, center = c)
            drawCircle(Color.White.copy(alpha = 0.92f), radius = ringR, center = c, style = Stroke(3.dp.toPx()))

            // Four short arcs travelling round just inside the ring.
            val arcR = 64.dp.toPx()
            // Four comet-like trails: a rounded bright head that thins out and fades into a long tail.
            val tail = 64f
            val pieces = 22
            repeat(4) { i ->
                for (s in 0 until pieces) {
                    val t = (s + 1) / pieces.toFloat()
                    drawArc(
                        color = androidx.compose.ui.graphics.lerp(Color(0xFFB98AF2), Color(0xFF6A1FC0), t).copy(alpha = 0.12f + 0.88f * t),
                        startAngle = spin + i * 90f - tail + s * (tail / pieces),
                        sweepAngle = tail / pieces + 1.5f,
                        useCenter = false,
                        topLeft = Offset(c.x - arcR, c.y - arcR),
                        size = Size(arcR * 2, arcR * 2),
                        style = Stroke((0.8f + 4.7f * t).dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.home_junk_files),
                color = Color(0xFF250B3B),
                style = TextStyle(fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 22.dp, bottomEnd = 22.dp), ambientColor = Color(0xFF4A1A8C), spotColor = Color(0xFF4A1A8C))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF8D3EDD), Color(0xFF6A1FC0))))
                    .clickable(onClick = onClean)
                    .padding(horizontal = 22.dp, vertical = 7.dp)
            ) {
                Text(
                    stringResource(R.string.home_clean),
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
