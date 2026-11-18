package com.phonecleaner.aicleaner.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private class Confetti(val angle: Float, val radius: Float, val kind: Int, val color: Color, val spin: Float)

private val pieces = listOf(
    Confetti(-100f, 63f, 0, Color(0xFFA8F0C8), 20f), Confetti(-60f, 70f, 2, Color(0xFFFFE27A), 40f),
    Confetti(-15f, 63f, 1, Color(0xFFFFB3C9), 0f), Confetti(20f, 68f, 3, Color(0xFFFF9FB5), 60f),
    Confetti(55f, 65f, 2, Color(0xFFFFE27A), 15f), Confetti(95f, 68f, 3, Color(0xFF8FEFB0), 30f),
    Confetti(130f, 62f, 0, Color(0xFF9FE9F4), 70f), Confetti(170f, 68f, 2, Color(0xFFFFE27A), 5f),
    Confetti(-135f, 67f, 0, Color(0xFF8FEFB0), 80f), Confetti(-170f, 63f, 1, Color(0xFFFFB3C9), 0f),
    Confetti(-80f, 76f, 1, Color(0xFFC8F5D6), 0f), Confetti(150f, 74f, 3, Color(0xFFFF9FB5), 50f)
)

@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, centerY: Dp = (-40).dp, onFinished: () -> Unit = {}) {
    val fly = remember { Animatable(0f) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        fly.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
        delay(700)
        fade.animateTo(0f, tween(650, easing = LinearEasing))
        onFinished()
    }
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f + centerY.toPx())
        pieces.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dir = Offset(cos(rad).toFloat(), sin(rad).toFloat() * 0.8f)
            val far = size.maxDimension * 0.9f
            val r = far + (p.radius.dp.toPx() - far) * fly.value
            val pos = Offset(c.x + dir.x * r, c.y + dir.y * r)
            val a = fade.value * (0.35f + 0.65f * fly.value)
            val s = 5.dp.toPx()
            rotate(p.spin + 220f * (1f - fly.value), pivot = pos) {
                when (p.kind) {
                    0 -> drawRoundRect(p.color.copy(alpha = a), Offset(pos.x - s * 1.2f, pos.y - s * 0.4f), Size(s * 2.4f, s * 0.8f), CornerRadius(s * 0.4f))
                    1 -> drawCircle(p.color.copy(alpha = a), s * 0.7f, pos)
                    2 -> drawCircle(p.color.copy(alpha = a), s * 0.9f, pos)
                    else -> drawArc(p.color.copy(alpha = a), 200f, 250f, false, Offset(pos.x - s, pos.y - s), Size(s * 2f, s * 2f), style = Stroke(s * 0.45f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
fun doneLabelFor(main: com.phonecleaner.aicleaner.domain.MainTask? = null, extra: com.phonecleaner.aicleaner.domain.ExtraTask? = null): String =
    stringResource(if (com.phonecleaner.aicleaner.domain.TaskTracker.completesAll(main, extra)) R.string.congrats_text else R.string.done_text)

@Composable
fun TaskDoneCheck(label: String = stringResource(R.string.done_text), caption: String? = null, onDone: () -> Unit) {
    val pop = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    var shimmer by remember { androidx.compose.runtime.mutableStateOf(false) }
    val base = MaterialTheme.colorScheme.tertiary
    val glow = MaterialTheme.colorScheme.primary
    val sweep by androidx.compose.animation.core.rememberInfiniteTransition(label = "doneShimmer").animateFloat(
        0f, 1f, androidx.compose.animation.core.infiniteRepeatable(tween(1900, easing = LinearEasing)), label = "sweep"
    )
    LaunchedEffect(Unit) {
        delay(750 + 700 + 650 + 1500)
        onDone()
    }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ConfettiBurst(Modifier.fillMaxSize(), onFinished = { shimmer = true })
        Box(
            modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp).size(116.dp).scale(pop.value).clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFF3CC074), Color(0xFF1FAD5E)))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(70.dp)) }
        if (caption != null) {
            Text(
                caption,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).offset(y = 62.dp).padding(horizontal = 40.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
        }
        if (caption != null) {
            Text(
                caption,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).offset(y = 62.dp).padding(horizontal = 40.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_medium)))
            )
        }
        Text(
            label,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp, start = 32.dp, end = 32.dp),
            style = TextStyle(
                brush = if (shimmer) {
                    val x = -200f + sweep * 1400f
                    Brush.linearGradient(listOf(base, glow, base), start = Offset(x, 0f), end = Offset(x + 260f, 0f))
                } else SolidColor(base),
                fontSize = 22.sp, fontFamily = FontFamily(Font(R.font.google_sans_flex_bold)), fontWeight = FontWeight.Bold
            )
        )
    }
}
