package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.process

import android.content.Intent
import android.provider.Settings
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.BoostState
import com.phonecleaner.aicleaner.domain.ProcessViewModel
import com.phonecleaner.aicleaner.domain.model.BoostResult
import com.phonecleaner.aicleaner.domain.model.MemoryStats
import com.phonecleaner.aicleaner.domain.model.RecentApp
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppIcon
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

private val Warning = Color(0xFFE5484D)
private val Good = Color(0xFF22B573)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessManagerScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: ProcessViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_process_manager)) },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MemoryGauge(state.memory)
            Spacer(Modifier.height(20.dp))

            when (val boost = state.boost) {
                is BoostState.Done -> BoostResultCard(boost.result, onDone = {
                    viewModel.resetBoost()
                    onTaskCompleted("")
                })
                else -> {
                    Text(
                        stringResource(R.string.process_boost_note),
                        color = MaterialTheme.colorScheme.surface,
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontFamily = medium)
                    )
                    Spacer(Modifier.height(14.dp))
                    BoostButton(running = boost == BoostState.Running) {
                        context.postAnalytic("process_boost_click")
                        viewModel.boost()
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle(stringResource(R.string.process_recent_title))
            Spacer(Modifier.height(10.dp))

            when {
                !state.hasUsageAccess && state.recentLoaded -> UsageAccessCard {
                    tryCatch { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                }

                state.recentApps.isEmpty() && state.recentLoaded -> Text(
                    stringResource(R.string.process_recent_empty),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                )

                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.recentApps.take(MAX_RECENT).forEach { app ->
                        RecentAppRow(app) {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${app.packageName}".toUri()
                            )
                            tryCatch { context.startActivity(intent) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary,
        style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun MemoryGauge(memory: MemoryStats) {
    val context = LocalContext.current
    val progress by animateFloatAsState(
        targetValue = memory.usedFraction,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "memoryProgress"
    )
    val percent = (progress * 100).toInt()
    val accent = if (progress >= HIGH_USAGE) Warning else MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.padding(top = 4.dp).size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(track, 135f, 270f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                rotate(135f) {
                    drawArc(Brush.sweepGradient(0f to accent.copy(alpha = 0.6f), 0.75f to Color(0xFF43B0FF), 1f to Color(0xFF43B0FF), center = center), 0f, 270f * progress, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$percent%",
                    color = accent,
                    style = TextStyle(fontSize = 36.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Text(
                    stringResource(R.string.process_used_memory),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MemoryStat(stringResource(R.string.process_free_memory), Formatter.formatFileSize(context, memory.availableBytes), Good)
            MemoryStat(stringResource(R.string.process_used_label), Formatter.formatFileSize(context, memory.usedBytes), accent)
            MemoryStat(stringResource(R.string.process_total_memory), Formatter.formatFileSize(context, memory.totalBytes), MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun MemoryStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = valueColor,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
    }
}

@Composable
private fun BoostButton(running: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFA66BF0), MaterialTheme.colorScheme.primary)))
            .clickable(enabled = !running, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (running) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(12.dp))
                Text(
                    stringResource(R.string.process_boosting),
                    color = Color.White,
                    style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
            }
        } else {
            Text(
                stringResource(R.string.process_boost_action),
                color = Color.White,
                style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun BoostResultCard(result: BoostResult, onDone: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = Good, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.process_boost_done_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (result.freedBytes > 0) {
                stringResource(R.string.process_boost_done_freed, Formatter.formatFileSize(context, result.freedBytes), result.appsCount)
            } else {
                stringResource(R.string.process_boost_done_none, result.appsCount)
            },
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .clickable(onClick = onDone)
                .padding(horizontal = 28.dp, vertical = 10.dp)
        ) {
            Text(
                stringResource(R.string.process_boost_again),
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun UsageAccessCard(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Insights, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.process_usage_body),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onGrant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.process_usage_grant),
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun RecentAppRow(app: RecentApp, onManage: () -> Unit) {
    val context = LocalContext.current
    val lastUsed = DateUtils.getRelativeTimeSpanString(
        app.lastUsedAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
    ).toString()
    val foreground = DateUtils.formatElapsedTime(app.foregroundMs / 1000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = onManage)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, Modifier.size(44.dp))
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            Text(
                stringResource(R.string.process_recent_detail, lastUsed, foreground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = medium)
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(
            stringResource(R.string.process_manage),
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

private const val HIGH_USAGE = 0.8f
private const val MAX_RECENT = 30
