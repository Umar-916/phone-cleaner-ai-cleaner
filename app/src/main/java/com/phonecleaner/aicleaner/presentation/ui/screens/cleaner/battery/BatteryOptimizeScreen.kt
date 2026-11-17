package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.imageResource
import androidx.core.net.toUri
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.data.battery.BatteryRepository
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.TaskTracker
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.PermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet
import com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process.BoostRunningView
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.sin

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val Plum = Color(0xFF2A0B40)

private class Item(
    val step: BatteryRepository.SaverStep,
    @StringRes val title: Int,
    @StringRes val sub: Int,
    @StringRes val running: Int,
    @DrawableRes val icon: Int
)

private val items = listOf(
    Item(BatteryRepository.SaverStep.TIMEOUT, R.string.bm_timeout, R.string.bm_timeout_sub, R.string.bm_run_timeout, R.drawable.bat_timer),
    Item(BatteryRepository.SaverStep.SOUND, R.string.bm_sound, R.string.bm_sound_sub, R.string.bm_run_sound, R.drawable.bat_sound),
    Item(BatteryRepository.SaverStep.BRIGHTNESS, R.string.bm_brightness, R.string.bm_brightness_sub, R.string.bm_run_brightness, R.drawable.bat_bright),
    Item(BatteryRepository.SaverStep.VIBRATION, R.string.bm_vibration, R.string.bm_vibration_sub, R.string.bm_run_vibration, R.drawable.bat_vib)
)

private sealed interface Phase {
    data object Idle : Phase
    data class Running(val index: Int, val percent: Int) : Phase
    data object Done : Phase
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BatteryOptimizeScreen(
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    repository: BatteryRepository = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf<Phase>(Phase.Idle) }
    var job by remember { mutableStateOf<Job?>(null) }
    var needsPermission by remember { mutableStateOf(false) }
    var stopSheet by remember { mutableStateOf(false) }
    val summary = stringResource(R.string.bm_done_summary)

    fun start() {
        if (!repository.canWriteSettings()) {
            needsPermission = true
            return
        }
        context.postAnalytic("battery_optimize_start")
        job = scope.launch {
            items.forEachIndexed { index, item ->
                phase = Phase.Running(index, index * 100 / items.size)
                delay(900)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { repository.applySaver(item.step) }
                phase = Phase.Running(index, (index + 1) * 100 / items.size)
                delay(700)
            }
            TaskTracker.mark(MainTask.BATTERY)
            phase = Phase.Done
        }
    }

    val running = phase is Phase.Running
    BackHandler(enabled = running) { stopSheet = true }
    LaunchedEffect(running) { if (!running) stopSheet = false }
    if (stopSheet) StopProcessSheet(
        onCancel = { stopSheet = false; job?.cancel(); onBack() },
        onDismiss = { stopSheet = false }
    )
    if (needsPermission) {
        PermissionPrompt(
            icon = Icons.Rounded.Settings,
            title = stringResource(R.string.battery_perm_title),
            body = stringResource(R.string.battery_perm_body),
            bullets = listOf(stringResource(R.string.battery_perm_b1), stringResource(R.string.battery_perm_b2)),
            allowLabel = stringResource(R.string.perm_allow),
            notNowLabel = stringResource(R.string.perm_not_now),
            accent = Brand.Battery,
            onAllow = {
                needsPermission = false
                tryCatch { context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:${context.packageName}".toUri())) }
            },
            onDismiss = { needsPermission = false }
        )
    }

    when (val p = phase) {
        is Phase.Running -> {
            val item = items[p.index]
            BoostRunningView(percent = p.percent, label = stringResource(item.running), badge = item.icon)
        }
        Phase.Done -> TaskDoneCheck(label = com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor(main = com.phonecleaner.aicleaner.domain.MainTask.BATTERY)) { onFinished(summary) }
        Phase.Idle -> com.phonecleaner.aicleaner.presentation.ui.theme.ForceLightTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { AppTopBar(title = { AppTopBarTitle(stringResource(R.string.feature_battery)) }, onBack = onBack) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BatteryArt()
                    Spacer(Modifier.height(18.dp))
                    items.forEachIndexed { index, item -> ListItem(item, index) }
                }
                Box(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    Box(
                        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFF7425CF)).tapClickable("start_click") { start() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.bm_continue), color = Color.White, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ListItem(item: Item, index: Int) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(220L + index * 140L)
        enter.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .graphicsLayer { alpha = enter.value; translationY = (1f - enter.value) * 28.dp.toPx() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(item.icon), null, modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(item.title), color = Plum, style = TextStyle(fontSize = 15.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            Text(stringResource(item.sub), color = Color(0xFF8B6BC0), style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = medium))
        }
    }
}

@Composable
private fun BatteryArt() {
    val base = androidx.compose.ui.graphics.ImageBitmap.Companion.imageResource(R.drawable.bat_art)
    val sound = androidx.compose.ui.graphics.ImageBitmap.Companion.imageResource(R.drawable.bat_sound)
    val bright = androidx.compose.ui.graphics.ImageBitmap.Companion.imageResource(R.drawable.bat_bright)
    val timer = androidx.compose.ui.graphics.ImageBitmap.Companion.imageResource(R.drawable.bat_timer)
    val vib = androidx.compose.ui.graphics.ImageBitmap.Companion.imageResource(R.drawable.bat_vib)
    val t = rememberInfiniteTransition(label = "batArt")
    val float by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val orbit by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "orbit")
    val aw = 550f
    val ah = 480f
    Canvas(
        Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(aw / ah)
            .graphicsLayer { translationY = float * 5.dp.toPx() }
            .featheredEdges(0.06f)
    ) {
        val k = size.width / aw
        drawImage(base, dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))
        fun androidx.compose.ui.graphics.drawscope.DrawScope.bubble(bmp: androidx.compose.ui.graphics.ImageBitmap, x: Float, y: Float, phase: Float) {
            val s = sin(2f * PI.toFloat() * (orbit + phase))
            val c = sin(2f * PI.toFloat() * (orbit + phase + 0.25f))
            val breathe = 1f + 0.05f * s
            val dx = s * 6f * k
            val dy = c * 7f * k
            translate(dx, dy) {
            scale(breathe, breathe, pivot = Offset((x + bmp.width / 2f) * k, (y + bmp.height / 2f) * k)) {
                drawImage(
                    bmp,
                    dstOffset = androidx.compose.ui.unit.IntOffset((x * k).toInt(), (y * k).toInt()),
                    dstSize = androidx.compose.ui.unit.IntSize((bmp.width * k).toInt(), (bmp.height * k).toInt())
                )
            }
            }
        }
        bubble(sound, 12f, 102f, 0f)
        bubble(bright, 438f, 35f, 0.3f)
        bubble(timer, 413f, 235f, 0.6f)
        bubble(vib, 43f, 330f, 0.85f)
    }
}
