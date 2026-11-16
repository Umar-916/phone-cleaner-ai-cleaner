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
import androidx.compose.ui.graphics.graphicsLayer
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
 * Hero control of the Home screen: a glossy disc with the Clean tab. The disc floats gently over a soft
 * shadow, one slow arc travels around it, and the outer ring shows how full the storage is.
 */
@Composable
fun HeroOrb(
    usedFraction: Float,
    onClean: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "heroOrb")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "spin")
    val hover by transition.animateFloat(-4f, 4f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "hover")
    val progress by animateFloatAsState(usedFraction.coerceIn(0f, 1f), tween(1000, easing = FastOutSlowInEasing), label = "usedProgress")

    Box(modifier = modifier.size(176.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val discRadius = 62.dp.toPx()

            drawCircle(Color.White.copy(alpha = 0.10f), radius = size.minDimension / 2)
            val glowCenter = Offset(center.x, center.y + hover.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55081F66), Color(0x22081F66), Color.Transparent),
                    center = glowCenter,
                    radius = 92.dp.toPx()
                ),
                radius = 92.dp.toPx(),
                center = glowCenter
            )

            // Shadow stays on the ground while the disc floats: it softens as the disc rises.
            val lift = (hover + 4f) / 8f
            val shadowCenter = Offset(center.x, center.y + 74.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.26f - 0.10f * lift), Color.Transparent),
                    center = shadowCenter,
                    radius = (46.dp.toPx() * (1f - 0.10f * lift))
                ),
                radius = 46.dp.toPx(),
                center = shadowCenter
            )

            val floatCenter = Offset(center.x, center.y + hover.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF59B4FF), Color(0xFF1B66E8)),
                    center = Offset(floatCenter.x - 14.dp.toPx(), floatCenter.y - 18.dp.toPx()),
                    radius = discRadius * 1.5f
                ),
                radius = discRadius,
                center = floatCenter
            )
            drawCircle(Color.White.copy(alpha = 0.85f), radius = discRadius, center = floatCenter, style = Stroke(3.dp.toPx()))

            val ringRadius = 72.dp.toPx()
            val topLeft = Offset(floatCenter.x - ringRadius, floatCenter.y - ringRadius)
            val ringSize = Size(ringRadius * 2, ringRadius * 2)
            val dashed = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx()))
            val innerRadius = 67.dp.toPx()
            val innerTopLeft = Offset(floatCenter.x - innerRadius, floatCenter.y - innerRadius)
            val innerSize = Size(innerRadius * 2, innerRadius * 2)
            drawArc(Color.White.copy(alpha = 0.16f), 0f, 360f, false, innerTopLeft, innerSize, style = Stroke(2.dp.toPx(), pathEffect = dashed))
            drawArc(Color.White.copy(alpha = 0.5f), -90f, 360f * progress, false, innerTopLeft, innerSize, style = Stroke(2.dp.toPx(), pathEffect = dashed))
            drawArc(Color.White.copy(alpha = 0.18f), 0f, 360f, false, topLeft, ringSize, style = Stroke(2.dp.toPx()))
            drawArc(Color.White.copy(alpha = 0.85f), spin, 60f, false, topLeft, ringSize, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }

        Column(
            modifier = Modifier.graphicsLayer { translationY = hover.dp.toPx() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.home_junk_files),
                color = Color.White,
                style = TextStyle(fontSize = 19.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(Color.White)
                    .clickable(onClick = onClean)
                    .padding(horizontal = 26.dp, vertical = 7.dp)
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
