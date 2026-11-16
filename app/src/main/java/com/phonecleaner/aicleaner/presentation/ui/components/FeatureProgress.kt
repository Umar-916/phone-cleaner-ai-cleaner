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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

/**
 * One progress presentation for every scan, clean and compress step: pulsing halo, orbiting arcs,
 * and either a determinate ring with a percentage or an icon while the total is not known yet.
 */
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
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "orbit"
    )
    val counterOrbit by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "counterOrbit"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(400),
        label = "progressValue"
    )

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(230.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize().scale(pulse)) {
                drawCircle(accent.copy(alpha = 0.08f))
                drawCircle(accent.copy(alpha = 0.10f), radius = size.minDimension / 2 - 18.dp.toPx())
            }
            Canvas(modifier = Modifier.size(180.dp)) {
                val outer = 10.dp.toPx()
                val outerSize = Size(size.width - outer, size.height - outer)
                val outerTopLeft = Offset(outer / 2, outer / 2)
                drawArc(accent.copy(alpha = 0.18f), 0f, 360f, false, outerTopLeft, outerSize, style = Stroke(outer))
                if (progress != null) {
                    drawArc(accent, -90f, 360f * animatedProgress, false, outerTopLeft, outerSize, style = Stroke(outer, cap = StrokeCap.Round))
                } else {
                    drawArc(accent, orbit, 100f, false, outerTopLeft, outerSize, style = Stroke(outer, cap = StrokeCap.Round))
                }
                val inset = 22.dp.toPx()
                val innerStroke = 4.dp.toPx()
                val innerSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(accent.copy(alpha = 0.55f), counterOrbit, 70f, false, Offset(inset, inset), innerSize, style = Stroke(innerStroke, cap = StrokeCap.Round))
                drawArc(accent.copy(alpha = 0.35f), counterOrbit + 180f, 40f, false, Offset(inset, inset), innerSize, style = Stroke(innerStroke, cap = StrokeCap.Round))
            }
            if (progress != null) {
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 38.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
                )
            } else if (icon != null) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(56.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold)
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                color = accent,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 15.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_semi)), fontWeight = FontWeight.SemiBold)
            )
        }
        if (detail != null) {
            Spacer(Modifier.height(8.dp))
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
