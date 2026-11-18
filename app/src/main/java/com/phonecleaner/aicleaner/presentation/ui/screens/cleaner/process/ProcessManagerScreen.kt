package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tapClickable
import com.phonecleaner.aicleaner.domain.BoostState
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.ProcessViewModel
import com.phonecleaner.aicleaner.domain.model.MemoryStats
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet
import com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck
import com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.presentation.ui.theme.HeatRed
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette
import com.phonecleaner.aicleaner.core.utils.AppBackButton
import org.koin.compose.viewmodel.koinViewModel

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val Ink = Color(0xFF250B3B)

@Composable
fun ProcessManagerScreen(
    onBack: () -> Unit,
    autoStart: Boolean = false,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: ProcessViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var info by remember { mutableStateOf(false) }
    var stopSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startPolling()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopPolling()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopPolling()
        }
    }

    val kill = {
        context.postAnalytic("process_boost_click")
        viewModel.boost()
    }

    val boost = state.boost
    val running = boost is BoostState.Running
    BackHandler(enabled = running) { stopSheet = true }
    BackHandler(enabled = info && !running) { info = false }
    LaunchedEffect(running) { if (!running) stopSheet = false }
    if (stopSheet) {
        StopProcessSheet(
            onCancel = { stopSheet = false; viewModel.resetBoost(); onBack() },
            onDismiss = { stopSheet = false }
        )
    }

    when (boost) {
        is BoostState.Running -> BoostRunningView(boost.percent, boost.label)
        is BoostState.Done -> {
            val freed = boost.result
            val summary = if (freed.freedBytes > 0) {
                stringResource(R.string.process_done_summary, Formatter.formatFileSize(context, freed.freedBytes), freed.appsCount)
            } else {
                stringResource(R.string.process_done_none, freed.appsCount)
            }
            TaskDoneCheck(label = doneLabelFor(main = MainTask.PROCESS)) {
                onTaskCompleted(summary)
                viewModel.resetBoost()
            }
        }
        BoostState.Idle -> if (info) {
            InfoContent(onInfo = { info = false }, onBack = { info = false })
        } else {
            RamContent(
                memory = state.memory,
                autoKill = autoStart,
                onInfo = { info = true },
                onBack = onBack,
                onKill = kill
            )
        }
    }
}

@Composable
private fun RamContent(
    memory: MemoryStats,
    autoKill: Boolean,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    onKill: () -> Unit
) {
    val context = LocalContext.current
    val target = remember { memory.usedFraction }
    val progress = remember { Animatable(0f) }
    var counted by remember { mutableStateOf(false) }
    var autoDone by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        progress.animateTo(target, tween(3400, easing = FastOutSlowInEasing))
        counted = true
    }
    LaunchedEffect(counted) {
        if (counted && autoKill && !autoDone) {
            autoDone = true
            onKill()
        }
    }

    val usedColor = lerp(MaterialTheme.colorScheme.primary, HeatRed, (progress.value * 1.7f).coerceIn(0f, 1f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingPalette.top, OnboardingPalette.bottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Text(
                stringResource(R.string.feature_process_manager),
                modifier = Modifier.weight(1f).padding(start = 8.3.dp),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Box(Modifier.size(40.dp).clip(CircleShape).tapClickable("process_info_icon", onClick = onInfo), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Info, stringResource(R.string.device_info_title), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        ProcessArt(excited = false, modifier = Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.weight(1f))
        MemoryHeadline(
            usedPercent = (progress.value * 100).toInt(),
            freeText = Formatter.formatFileSize(context, memory.availableBytes),
            note = stringResource(R.string.pm_caption),
            usedColor = usedColor,
            modifier = Modifier.offset(y = (-10).dp)
        )
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 20.dp)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = counted && !autoKill,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                TaskKillerButton(onKill)
            }
        }
        Spacer(Modifier.height(24.dp + getBannerOverlayHeight()))
    }
}

@Composable
private fun TaskKillerButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFF0605F), HeatRed)))
            .tapClickable("process_task_killer", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.process_task_killer),
            color = Color.White,
            style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoContent(onInfo: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.info_center_title)) },
                onBack = onBack,
                actions = {
                    Box(Modifier.padding(end = 12.dp).size(40.dp).clip(CircleShape).tapClickable("process_info_icon_close", onClick = onInfo), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Info, stringResource(R.string.device_info_title), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pad by animateDpAsState(64.dp, tween(500, easing = FastOutSlowInEasing), label = "artPad")
            ProcessArt(excited = true, modifier = Modifier.padding(horizontal = pad))
            Spacer(Modifier.height(40.dp))
            Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(1.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)))
            Spacer(Modifier.height(30.dp))
            ProcessInfoChat()
            Spacer(Modifier.height(24.dp + getBannerOverlayHeight()))
        }
    }
}
