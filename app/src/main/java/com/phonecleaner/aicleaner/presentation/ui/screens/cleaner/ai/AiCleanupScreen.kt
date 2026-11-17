package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.core.common.StoragePermission
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.data.ai.AiGroup
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.data.ai.AiPhase
import com.phonecleaner.aicleaner.domain.AiCleanupViewModel
import com.phonecleaner.aicleaner.domain.AiResult
import com.phonecleaner.aicleaner.domain.AiUiState
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.domain.model.MediaFile
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.presentation.ui.components.GradientButton
import com.phonecleaner.aicleaner.presentation.ui.components.MediaThumbnail
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

private fun AiKind.icon(): ImageVector = when (this) {
    AiKind.SCREENSHOTS -> Icons.Rounded.Screenshot
    AiKind.LARGE_VIDEOS -> Icons.Rounded.Videocam
    AiKind.BLURRY -> Icons.Rounded.BlurOn
    AiKind.OLD_DOWNLOADS -> Icons.Rounded.Download
}

private fun AiKind.tint(): Color = when (this) {
    AiKind.SCREENSHOTS -> Brand.Duplicates
    AiKind.LARGE_VIDEOS -> Brand.Process
    AiKind.BLURRY -> Brand.Photo
    AiKind.OLD_DOWNLOADS -> Brand.Junk
}

private fun AiKind.title(): Int = when (this) {
    AiKind.SCREENSHOTS -> R.string.ai_kind_screenshots
    AiKind.LARGE_VIDEOS -> R.string.ai_kind_videos
    AiKind.BLURRY -> R.string.ai_kind_blurry
    AiKind.OLD_DOWNLOADS -> R.string.ai_kind_downloads
}

private fun AiKind.reason(): Int = when (this) {
    AiKind.SCREENSHOTS -> R.string.ai_reason_screenshots
    AiKind.LARGE_VIDEOS -> R.string.ai_reason_videos
    AiKind.BLURRY -> R.string.ai_reason_blurry
    AiKind.OLD_DOWNLOADS -> R.string.ai_reason_downloads
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCleanupScreen(
    onBack: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onFinished: (String) -> Unit,
    viewModel: AiCleanupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = StoragePermission.isGranted(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val legacyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = StoragePermission.isGranted(context) }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { granted = StoragePermission.isGranted(context) }

    LaunchedEffect(granted, state is AiUiState.Idle) {
        if (granted && state is AiUiState.Idle) {
            context.postAnalytic("ai_cleanup_start")
            viewModel.start()
        }
    }

    var revealed by remember { mutableStateOf(false) }
    val scanAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    var doneReady by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (state is AiUiState.Idle) { revealed = false; scanAnim.snapTo(0f) }
        if (state !is AiUiState.Done) doneReady = false
    }
    val busy = state is AiUiState.Analyzing || state is AiUiState.Cleaning
    var stopSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = busy) { stopSheet = true }
    androidx.compose.runtime.LaunchedEffect(busy) { if (!busy) stopSheet = false }
    if (stopSheet) com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet(
        onCancel = { stopSheet = false; viewModel.reset(); onBack() },
        onDismiss = { stopSheet = false }
    )

    val loading = state is AiUiState.Analyzing || state is AiUiState.Cleaning ||
        (state is AiUiState.Result && !revealed) || (state is AiUiState.Done && !doneReady)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { if (!loading) AppTopBar(title = { AppTopBarTitle(stringResource(R.string.feature_ai_cleanup)) }, onBack = if (busy) null else onBack) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(if (loading) PaddingValues(0.dp) else padding)) {
            when (val current = state) {
                AiUiState.Idle -> if (!granted) {
                    StoragePermissionPrompt(onDismiss = onBack, accent = Brand.Ai) {
                        if (StoragePermission.usesSettingsScreen) settingsLauncher.launch(StoragePermission.settingsIntent(context))
                        else legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
                    }
                }

                is AiUiState.Analyzing -> com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(
                    progress = if (current.total > 0) current.done.toFloat() / current.total else null,
                    finished = false,
                    label = stringResource(if (current.phase == AiPhase.COLLECTING) R.string.ai_collecting else R.string.ai_analyzing),
                    p = scanAnim
                )

                is AiUiState.Result -> if (!revealed) {
                    com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(null, true, stringResource(R.string.ai_analyzing), scanAnim) { revealed = true }
                } else if (current.data.groups.isEmpty()) {
                    EmptyView(onDuplicates = onOpenDuplicates, onRescan = viewModel::reset)
                } else {
                    ResultView(current.data, viewModel, onOpenDuplicates) {
                        context.postAnalytic("ai_cleanup_confirm")
                        viewModel.cleanSelected()
                    }
                }

                is AiUiState.Cleaning, is AiUiState.Done -> {
                    val done = current as? AiUiState.Done
                    val summary = if (done != null) stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, done.freedBytes), done.removed) else ""
                    if (done != null && doneReady) {
                        com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck(
                            label = com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor(extra = com.phonecleaner.aicleaner.domain.ExtraTask.AI)
                        ) {
                            viewModel.reset()
                            onFinished(summary)
                        }
                    } else {
                        com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(null, done != null, stringResource(R.string.junk_cleaning)) { doneReady = true }
                    }
                }
            }
        }
    }

}

@Composable
private fun ResultView(data: AiResult, viewModel: AiCleanupViewModel, onOpenDuplicates: () -> Unit, onClean: () -> Unit) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(setOf<AiKind>()) }
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush(Brand.Ai))
                .padding(18.dp)
        ) {
            Text(
                stringResource(R.string.ai_savings, Formatter.formatFileSize(context, data.totalBytes)),
                color = Color.White,
                style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Text(
                stringResource(R.string.ai_savings_sub),
                color = Color.White.copy(alpha = 0.9f),
                style = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontFamily = medium)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(data.groups, key = { it.kind.name }) { group ->
                GroupCard(
                    group = group,
                    selected = data.selected,
                    expanded = group.kind in expanded.value,
                    onExpand = { expanded.value = if (group.kind in expanded.value) expanded.value - group.kind else expanded.value + group.kind },
                    onToggleGroup = { viewModel.toggleGroup(group) },
                    onToggle = viewModel::toggle
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.secondary).tapClickable("result_view_click", onClick = onOpenDuplicates).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.ai_duplicates_title), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.ai_duplicates_body), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, fontFamily = medium))
                    }
                    Text(stringResource(R.string.ai_review), color = MaterialTheme.colorScheme.primary, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                }
            }
        }

        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())) {
            GradientButton(
                text = if (data.selected.isEmpty()) stringResource(R.string.dup_nothing_selected) else stringResource(R.string.junk_clean_selected, Formatter.formatFileSize(context, data.selectedBytes)),
                onClick = onClean.tapped("result_view_tap_2"),
                enabled = data.selected.isNotEmpty()
            )
        }
    }
}

private fun Brush(color: Color) = androidx.compose.ui.graphics.Brush.linearGradient(listOf(color, color.copy(alpha = 0.75f), Brand.Blue))

@Composable
private fun GroupCard(
    group: AiGroup,
    selected: Set<String>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onToggleGroup: () -> Unit,
    onToggle: (FileEntry) -> Unit
) {
    val context = LocalContext.current
    val tint = group.kind.tint()
    val allSelected = selected.containsAll(group.files.map { it.path })
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.secondary)) {
        Row(Modifier.fillMaxWidth().tapClickable("group_card_click", onClick = onExpand).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(group.kind.icon(), null, tint = tint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(group.kind.title()), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                Text(stringResource(group.kind.reason()), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = medium))
                Text(
                    stringResource(R.string.wa_category_summary, group.files.size, Formatter.formatShortFileSize(context, group.bytes)),
                    color = tint,
                    modifier = Modifier.padding(top = 2.dp),
                    style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
            AppCheckbox(checked = allSelected, onCheckedChange = { onToggleGroup() })
            Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.surface)
        }
        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.files.take(MAX_PREVIEW).forEach { entry ->
                    val isSelected = entry.path in selected
                    Column(Modifier.size(width = 96.dp, height = 132.dp)) {
                        Box(Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)).tapClickable("group_card_click_2") { onToggle(entry) }) {
                            if (entry.isMedia) {
                                MediaThumbnail(MediaFile(entry.id, entry.uri, entry.name, entry.sizeBytes, entry.modifiedMs, entry.category == com.phonecleaner.aicleaner.domain.model.FileCategory.VIDEO, 0, 0, 0L), Modifier.fillMaxSize(), 200)
                            } else {
                                Box(Modifier.fillMaxSize().background(tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                    Icon(group.kind.icon(), null, tint = tint, modifier = Modifier.size(34.dp))
                                }
                            }
                            AppCheckbox(checked = isSelected, onCheckedChange = { onToggle(entry) }, modifier = Modifier.align(Alignment.TopStart).size(36.dp))
                        }
                        Text(entry.name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
                        Text(Formatter.formatShortFileSize(context, entry.sizeBytes), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 10.sp, fontFamily = medium))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyView(onDuplicates: () -> Unit, onRescan: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Rounded.CheckCircle, null, tint = Brand.Good, modifier = Modifier.size(84.dp))
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.ai_empty_title), color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center, style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.ai_empty_body), color = MaterialTheme.colorScheme.surface, textAlign = TextAlign.Center, style = TextStyle(fontSize = 14.sp, fontFamily = medium))
        Spacer(Modifier.height(24.dp))
        GradientButton(stringResource(R.string.ai_duplicates_title), onDuplicates)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRescan.tapped("empty_view_tap")) { Text(stringResource(R.string.junk_rescan)) }
    }
}

private const val MAX_PREVIEW = 40
