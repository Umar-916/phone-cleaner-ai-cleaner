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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R

@Composable
fun FeatureProgress(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    detail: String? = null,
    progress: Float? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "featureProgress")
    val orbit by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "orbit")
    val dashSpin by transition.animateFloat(360f, 0f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "dashSpin")
    val pulse by transition.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val animatedProgress by animateFloatAsState(progress ?: 0f, tween(400), label = "progressValue")
    val tail = accent.copy(alpha = 0.15f)
    val head = Color(0xFF43B0FF)

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(190.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize().scale(pulse)) {
                drawCircle(accent.copy(alpha = 0.07f))
                drawCircle(accent.copy(alpha = 0.07f), radius = size.minDimension / 2 - 14.dp.toPx())
            }
            Canvas(modifier = Modifier.size(150.dp)) {
                val stroke = 7.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                val sweepBrush = Brush.sweepGradient(listOf(tail, accent, head, tail), center)
                drawArc(accent.copy(alpha = 0.14f), 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                if (progress != null) {
                    rotate(-90f) {
                        drawArc(sweepBrush, 0f, 360f * animatedProgress, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                } else {
                    rotate(orbit) {
                        drawArc(Brush.sweepGradient(listOf(Color.Transparent, accent, head), center), 0f, 110f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                val inset = 16.dp.toPx()
                val dashSize = Size(size.width - inset * 2, size.height - inset * 2)
                rotate(dashSpin) {
                    drawArc(
                        color = accent.copy(alpha = 0.45f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = dashSize,
                        style = Stroke(
                            2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 9.dp.toPx()))
                        )
                    )
                }
            }
            if (progress != null) {
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 30.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
                )
            } else if (icon != null) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(42.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            title,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 19.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
        )
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                color = accent,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
            )
        }
        if (detail != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                detail,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
        }
    }
}
