package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import android.content.Intent
import android.provider.Settings
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tapClickable
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.BoostState
import com.phonecleaner.aicleaner.domain.MainTask
import com.phonecleaner.aicleaner.domain.ProcessViewModel
import com.phonecleaner.aicleaner.domain.model.RecentApp
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppIcon
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet
import com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck
import com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.presentation.ui.theme.softCardColor
import org.koin.compose.viewmodel.koinViewModel

private const val MAX_RECENT = 8
private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium

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
    var started by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.startPolling()
                    viewModel.refreshUsage()
                }
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

    val start = {
        context.postAnalytic("process_boost_click")
        viewModel.boost()
    }
    LaunchedEffect(Unit) {
        if (autoStart && !started) {
            started = true
            start()
        }
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
        BoostState.Idle -> if (autoStart && !started) {
            BoostRunningView(0, "")
        } else {
            MemoryContent(
                info = info,
                onInfo = { info = !info },
                onBack = { if (info) info = false else onBack() },
                usedPercent = (state.memory.usedFraction * 100).toInt(),
                freeText = Formatter.formatFileSize(context, state.memory.availableBytes),
                hasUsageAccess = state.hasUsageAccess,
                recentLoaded = state.recentLoaded,
                recentApps = state.recentApps,
                onBoost = start
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun MemoryContent(
    info: Boolean,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    usedPercent: Int,
    freeText: String,
    hasUsageAccess: Boolean,
    recentLoaded: Boolean,
    recentApps: List<RecentApp>,
    onBoost: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_process_manager)) },
                onBack = onBack,
                actions = {
                    Box(
                        Modifier.padding(end = 12.dp).size(40.dp).clip(CircleShape).tapClickable("process_info_icon", onClick = onInfo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (info) Icons.Rounded.Info else Icons.Outlined.Info,
                            stringResource(R.string.device_info_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pad by animateDpAsState(if (info) 64.dp else 8.dp, tween(500, easing = FastOutSlowInEasing), label = "artPad")
            ProcessArt(excited = info, modifier = Modifier.padding(horizontal = pad))
            if (info) {
                Spacer(Modifier.height(12.dp))
                ProcessInfoChat()
                Spacer(Modifier.height(24.dp + getBannerOverlayHeight()))
                return@Column
            }
            MemoryHeadline(usedPercent, freeText, stringResource(R.string.pm_caption))
            Spacer(Modifier.height(20.dp))
            BoostButton(onBoost)
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.process_recent_title),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(10.dp))
            when {
                !hasUsageAccess && recentLoaded -> UsageAccessCard {
                    tryCatch { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                }
                recentApps.isEmpty() && recentLoaded -> Text(
                    stringResource(R.string.process_recent_empty),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentApps.take(MAX_RECENT).forEach { RecentAppRow(it) }
                }
            }
            Spacer(Modifier.height(24.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun BoostButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFA66BF0), Color(0xFF7425CF))))
            .tapClickable("process_free_up", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.process_boost_action),
            color = Color.White,
            style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun UsageAccessCard(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(softCardColor()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Insights, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.process_usage_body),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(MaterialTheme.colorScheme.primary)
                .tapClickable("process_usage_access", onClick = onGrant),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.process_usage_grant), color = Color.White, style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun RecentAppRow(app: RecentApp) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(softCardColor()).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, Modifier.size(44.dp))
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            Text(
                stringResource(
                    R.string.process_recent_detail,
                    DateUtils.getRelativeTimeSpanString(app.lastUsedAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                    DateUtils.formatElapsedTime(app.foregroundMs / 1000)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = medium)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .tapClickable("process_recent_manage") {
                    tryCatch {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${app.packageName}".toUri()))
                    }
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.process_manage), color = MaterialTheme.colorScheme.primary, style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        }
    }
}
