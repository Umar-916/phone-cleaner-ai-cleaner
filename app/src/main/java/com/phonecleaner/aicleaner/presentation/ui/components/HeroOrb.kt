package com.phonecleaner.aicleaner.presentation.ui.components

import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
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

@Composable
fun HeroOrb(
    usedFraction: Float,
    onClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "heroOrb")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(3600, easing = LinearEasing)), label = "spin")
    val breathe by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val progress by animateFloatAsState(usedFraction.coerceIn(0f, 1f), tween(1000, easing = FastOutSlowInEasing), label = "usedProgress")

    Box(modifier = modifier.size(200.dp).clip(CircleShape).tapClickable("hero_orb_click", onClick = onClean), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val halo = 6.dp.toPx() * breathe

            drawCircle(Color(0xFFB98AF2).copy(alpha = 0.38f), radius = 84.dp.toPx() + halo, center = c, style = Stroke(10.dp.toPx()))
            drawCircle(Color(0xFFC9A3F7).copy(alpha = 0.55f), radius = 75.dp.toPx() + halo / 2, center = c, style = Stroke(6.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.95f), radius = 71.dp.toPx(), center = c, style = Stroke(2.dp.toPx()))
            drawCircle(Color(0xFFB98AF2).copy(alpha = 0.40f), radius = 65.dp.toPx(), center = c, style = Stroke(6.dp.toPx()))

            val arcR = 65.dp.toPx()
            val span = 46f
            val pieces = 16
            repeat(4) { i ->
                for (s in 0 until pieces) {
                    val t = (s + 1) / pieces.toFloat()
                    drawArc(
                        color = androidx.compose.ui.graphics.lerp(Color(0xFFB98AF2), Color(0xFF7424C4), t).copy(alpha = 0.25f + 0.75f * t),
                        startAngle = spin + i * 90f - span + s * (span / pieces),
                        sweepAngle = span / pieces + 1.5f,
                        useCenter = false,
                        topLeft = Offset(c.x - arcR, c.y - arcR),
                        size = Size(arcR * 2, arcR * 2),
                        style = Stroke(7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        val clean = GenericShape { s, _ ->
            val r = 18f.coerceAtMost(s.height / 3)
            val inset = s.width * 0.05f
            moveTo(r, 0f); lineTo(s.width - r, 0f); quadraticTo(s.width, 0f, s.width, r)
            lineTo(s.width - inset, s.height - r); quadraticTo(s.width - inset - r, s.height, s.width - inset - r * 2, s.height)
            lineTo(inset + r * 2, s.height); quadraticTo(inset + r, s.height, inset, s.height - r)
            lineTo(0f, r); quadraticTo(0f, 0f, r, 0f); close()
        }
        Box(contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.home_junk_files),
                color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.offset(y = (-24).dp),
                style = TextStyle(fontSize = 21.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Box(
                modifier = Modifier
                    .offset(y = 24.dp)
                    .shadow(5.dp, clean, ambientColor = Color(0xFF4A1A8C), spotColor = Color(0xFF4A1A8C))
                    .clip(clean)
                    .background(Brush.verticalGradient(listOf(Color(0xFF8D3EDD), Color(0xFF6A1FC0))))
                    .tapClickable("hero_orb_click_2", onClick = onClean)
                    .padding(horizontal = 26.dp, vertical = 7.dp)
            ) {
                Text(
                    stringResource(R.string.home_clean),
                    maxLines = 1,
                    softWrap = false,
                    color = Color.White,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
