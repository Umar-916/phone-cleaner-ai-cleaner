package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium

private const val ART_W = 590f
private const val ART_H = 470f

@Composable
fun ProcessArt(excited: Boolean, modifier: Modifier = Modifier) {
    val base = ImageBitmap.imageResource(R.drawable.pm_art)
    val green = ImageBitmap.imageResource(R.drawable.pm_planet_g)
    val yellow = ImageBitmap.imageResource(R.drawable.pm_planet_y)
    val t = rememberInfiniteTransition(label = "pmArt")
    val float by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val orbit by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "orbit")
    val twinkle by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse), label = "twinkle")
    val burst = remember { Animatable(0f) }
    LaunchedEffect(excited) {
        if (excited) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
    }
    val pop = if (excited) sin(PI.toFloat() * burst.value) else 0f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ART_W / ART_H)
            .graphicsLayer {
                translationY = float * 6.dp.toPx() - pop * 10.dp.toPx()
                rotationZ = if (excited) sin(2f * PI.toFloat() * burst.value) * 3f else float * 0.8f
            }
            .featheredEdges(0.08f)
    ) {
        val k = size.width / ART_W
        scale(1f + 0.05f * pop, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawImage(base, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
            drawPlanet(green, 18f, 135f, k, sin(2f * PI.toFloat() * orbit) * 5f, sin(2f * PI.toFloat() * (orbit + 0.25f)) * 4f)
            drawPlanet(yellow, 389f, 330f, k, sin(2f * PI.toFloat() * (orbit + 0.5f)) * 4f, sin(2f * PI.toFloat() * (orbit + 0.75f)) * 5f)
            sparkle(448f, 78f, 11f, k, twinkle)
            sparkle(140f, 242f, 9f, k, 1f - twinkle)
            sparkle(493f, 270f, 10f, k, twinkle)
            sparkle(526f, 130f, 8f, k, 1f - twinkle)
        }
    }
}

private fun DrawScope.drawPlanet(bmp: ImageBitmap, x: Float, y: Float, k: Float, dx: Float, dy: Float) {
    translate(left = dx * k, top = dy * k) {
        drawImage(bmp, dstOffset = IntOffset((x * k).toInt(), (y * k).toInt()), dstSize = IntSize((bmp.width * k).toInt(), (bmp.height * k).toInt()))
    }
}

private fun DrawScope.sparkle(cx: Float, cy: Float, r: Float, k: Float, phase: Float) {
    val a = (1f - abs(2f * phase - 1f)).coerceIn(0f, 1f)
    val rr = r * k * (0.5f + 0.7f * a)
    val c = Offset(cx * k, cy * k)
    val p = Path().apply {
        moveTo(c.x, c.y - rr)
        quadraticTo(c.x, c.y, c.x + rr, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + rr)
        quadraticTo(c.x, c.y, c.x - rr, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - rr)
        close()
    }
    drawPath(p, Color.White.copy(alpha = 0.25f + 0.75f * a))
}

@Composable
fun MemoryHeadline(usedPercent: Int, freeText: String, note: String) {
    val (freeNumber, freeUnit) = freeText.split(" ").let { it.getOrElse(0) { freeText } to it.getOrElse(1) { "" } }
    val accent = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.tertiary
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.process_used_memory), color = accent, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text("$usedPercent%", color = accent, style = TextStyle(fontSize = 30.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.process_free_memory), color = ink, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(freeNumber, color = ink, style = TextStyle(fontSize = 30.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                    Text(freeUnit, modifier = Modifier.padding(start = 4.dp, bottom = 5.dp), color = ink, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(note, color = MaterialTheme.colorScheme.surface, textAlign = TextAlign.Center, style = TextStyle(fontSize = 13.sp, fontFamily = medium))
    }
}

private class Topic(@StringRes val question: Int, @StringRes val answer: Int)

private val topics = listOf(
    Topic(R.string.pm_q_what, R.string.pm_a_what),
    Topic(R.string.pm_q_how, R.string.pm_a_how),
    Topic(R.string.pm_q_when, R.string.pm_a_when),
    Topic(R.string.pm_q_data, R.string.pm_a_data)
)

@Composable
fun ProcessInfoChat(modifier: Modifier = Modifier) {
    val greeting = stringResource(R.string.pm_greeting)
    var typedGreeting by remember { mutableIntStateOf(0) }
    var showTopics by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(-1) }
    var typedAnswer by remember { mutableIntStateOf(0) }
    val answers = topics.map { stringResource(it.answer) }

    LaunchedEffect(Unit) {
        delay(250)
        for (i in 1..greeting.length) { typedGreeting = i; delay(22) }
        delay(200)
        showTopics = true
    }
    LaunchedEffect(selected) {
        if (selected < 0) return@LaunchedEffect
        typedAnswer = 0
        val text = answers[selected]
        for (i in 1..text.length) { typedAnswer = i; delay(16) }
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        BotRow {
            Text(
                greeting.take(typedGreeting),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(Modifier.height(14.dp))
        val alpha by animateFloatAsState(if (showTopics) 1f else 0f, tween(450), label = "topicsAlpha")
        Column(Modifier.graphicsLayer { this.alpha = alpha; translationY = (1f - alpha) * 16.dp.toPx() }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            topics.chunked(2).forEachIndexed { row, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEachIndexed { col, topic ->
                        val index = row * 2 + col
                        TopicChip(stringResource(topic.question), selected == index, Modifier.weight(1f), index) {
                            if (showTopics) selected = index
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (selected >= 0) {
            BotRow {
                Text(
                    answers[selected].take(typedAnswer),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontFamily = medium)
                )
            }
        }
    }
}

@Composable
private fun BotRow(content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.SmartToy, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(10.dp))
        content()
    }
}

@Composable
private fun TopicChip(text: String, selected: Boolean, modifier: Modifier, index: Int, onClick: () -> Unit) {
    val container by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, tween(250), label = "chipBg")
    val content by animateColorAsState(if (selected) Color.White else MaterialTheme.colorScheme.tertiary, tween(250), label = "chipFg")
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(0.94f)
            scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        }
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .tapPlainClickable("process_info_topic_${index + 1}", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = content, textAlign = TextAlign.Center, maxLines = 1, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
    }
}
