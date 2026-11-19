package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.whatsapp

import androidx.core.net.toUri
import com.phonecleaner.aicleaner.presentation.ui.components.OnResumeEffect
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.text.format.Formatter
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Gif
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phonecleaner.aicleaner.domain.model.WaTab
import com.phonecleaner.aicleaner.domain.model.hasSentTab
import com.phonecleaner.aicleaner.presentation.ui.screens.files.openUri
import com.phonecleaner.aicleaner.presentation.ui.screens.files.mimeFor
import com.phonecleaner.aicleaner.domain.model.WaPreviewKind
import com.phonecleaner.aicleaner.core.common.tapEvent
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.phonecleaner.aicleaner.presentation.ui.components.PillChip
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.presentation.ui.components.FolderAccessPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.WaData
import com.phonecleaner.aicleaner.domain.WaUiState
import com.phonecleaner.aicleaner.domain.WhatsAppViewModel
import com.phonecleaner.aicleaner.domain.model.WaCategory
import com.phonecleaner.aicleaner.domain.model.WaFile
import com.phonecleaner.aicleaner.domain.model.WaFilter
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold
private val WhatsAppGreen = Color(0xFF25D366)

private val thumbCache = LruCache<String, ImageBitmap>(150)

private fun WaCategory.icon(): ImageVector = when (this) {
    WaCategory.IMAGES -> Icons.Rounded.Image
    WaCategory.VIDEOS -> Icons.Rounded.Videocam
    WaCategory.AUDIO -> Icons.Rounded.MusicNote
    WaCategory.VOICE -> Icons.Rounded.Mic
    WaCategory.DOCUMENTS -> Icons.Rounded.Description
    WaCategory.STICKERS -> Icons.Rounded.EmojiEmotions
    WaCategory.GIFS -> Icons.Rounded.Gif
    WaCategory.STATUS -> Icons.Rounded.DonutLarge
}

private fun WaCategory.tint(): Color = when (this) {
    WaCategory.IMAGES -> Color(0xFF2F8BFF)
    WaCategory.VIDEOS -> Color(0xFFE5484D)
    WaCategory.AUDIO -> Color(0xFFFF8A1F)
    WaCategory.VOICE -> Color(0xFF7C5CFC)
    WaCategory.DOCUMENTS -> Color(0xFF0EA5A5)
    WaCategory.STICKERS -> Color(0xFFF5B301)
    WaCategory.GIFS -> Color(0xFFEC4899)
    WaCategory.STATUS -> WhatsAppGreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppCleanerScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: WhatsAppViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(FolderAccess.hasWhatsAppAccess(context)) }
    OnResumeEffect { granted = FolderAccess.hasWhatsAppAccess(context) }

    LaunchedEffect(granted, state is WaUiState.Idle) {
        if (granted && state is WaUiState.Idle) {
            context.postAnalytic("wa_scan_start")
            viewModel.startScan()
        }
    }

    var revealed by remember { mutableStateOf(false) }
    var doneReady by remember { mutableStateOf(false) }
    val scanAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(state) {
        if (state is WaUiState.Idle) { revealed = false; scanAnim.snapTo(0f) }
        if (state !is WaUiState.Done) doneReady = false
    }
    val data = (state as? WaUiState.Result)?.data
    val busy = state is WaUiState.Scanning || state is WaUiState.Cleaning
    var stopSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = busy || data?.openCategory != null) {
        when {
            busy -> stopSheet = true
            data?.isSelecting == true -> viewModel.clearSelection()
            else -> viewModel.openCategory(null)
        }
    }
    androidx.compose.runtime.LaunchedEffect(busy) { if (!busy) stopSheet = false }
    if (stopSheet) com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet(
        onCancel = { stopSheet = false; viewModel.reset(); onBack() },
        onDismiss = { stopSheet = false }
    )

    val loading = state is WaUiState.Scanning || state is WaUiState.Cleaning ||
        (state is WaUiState.Result && !revealed) || (state is WaUiState.Done && !doneReady)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!loading && data?.isSelecting == true) {
                androidx.compose.material3.TopAppBar(
                    title = { Text(stringResource(R.string.files_selected_count, data.selected.size), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        Box(Modifier.padding(start = 8.dp).size(44.dp).clip(CircleShape).tapClickable("wa_clear_selection", onClick = viewModel::clearSelection), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    },
                    actions = {
                        Box(Modifier.padding(end = 12.dp).size(44.dp).tapPlainClickable("wa_select_all", onClick = viewModel::toggleVisible), contentAlignment = Alignment.Center) {
                            com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck(data.selected.containsAll(data.visibleFiles.map { it.path }) && data.visibleFiles.isNotEmpty())
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            } else if (!loading) AppTopBar(
                title = {
                    AppTopBarTitle(
                        stringResource(data?.openCategory?.label ?: R.string.feature_whatsapp)
                    )
                },
                onBack = if (busy) null else {
                    { if (data?.openCategory != null) viewModel.openCategory(null) else onBack() }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(if (loading) PaddingValues(0.dp) else padding)) {
            when (val current = state) {
                WaUiState.Idle -> if (!granted) {
                    FolderAccessPrompt(onDismiss = onBack, onGranted = { granted = true }, accent = Brand.WhatsApp)
                }

                is WaUiState.Scanning -> com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(
                    progress = null,
                    finished = false,
                    label = stringResource(R.string.wa_scanning),
                    p = scanAnim
                )
                WaUiState.NotFound -> com.phonecleaner.aicleaner.presentation.ui.components.ResultEmptyState(Icons.Rounded.CheckCircle, WhatsAppGreen, stringResource(R.string.wa_none_title), stringResource(R.string.wa_none_body), stringResource(R.string.empty_done), onBack)
                is WaUiState.Result -> if (!revealed) {
                    com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(null, true, stringResource(R.string.wa_scanning), scanAnim) { revealed = true }
                } else ResultView(current.data, viewModel) {
                    context.postAnalytic("wa_clean_confirm")
                    viewModel.cleanSelected()
                }
                is WaUiState.Cleaning, is WaUiState.Done -> {
                    val done = current as? WaUiState.Done
                    val summary = if (done != null) stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, done.freedBytes), done.removedCount) else ""
                    if (done != null && doneReady) {
                        com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck(
                            label = com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor(extra = com.phonecleaner.aicleaner.domain.ExtraTask.WHATSAPP)
                        ) {
                            viewModel.reset()
                            onTaskCompleted(summary)
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
private fun CenteredColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFA66BF0), MaterialTheme.colorScheme.primary)))
            .tapClickable("primary_button_click", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun ResultView(data: WaData, viewModel: WhatsAppViewModel, onDelete: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (data.openCategory == null) Overview(data, viewModel) else CategoryDetail(data, viewModel)
        }
        if (data.selected.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 14.dp + getBannerOverlayHeight())
            ) {
                PrimaryButton(
                    stringResource(R.string.ai_delete_selected, data.selected.size, Formatter.formatShortFileSize(context, data.selectedBytes)),
                    onDelete
                )
            }
        } else {
            Spacer(Modifier.height(getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun Overview(data: WaData, viewModel: WhatsAppViewModel) {
    val context = LocalContext.current
    val totalStorage = remember { android.os.StatFs(android.os.Environment.getDataDirectory().path).totalBytes }
    val categories = WaCategory.entries.filter { data.byCategory[it].orEmpty().isNotEmpty() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.files_mascot),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 150.dp, height = 136.dp)
                )
                Spacer(Modifier.size(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    WaStat(stringResource(R.string.wa_stat_whatsapp), Formatter.formatFileSize(context, data.totalBytes), WhatsAppGreen)
                    WaStat(stringResource(R.string.ai_stat_total), Formatter.formatFileSize(context, totalStorage), MaterialTheme.colorScheme.surface)
                }
            }
        }
        items(categories, key = { it.name }) { category ->
            val files = data.byCategory[category].orEmpty()
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(category.icon(), null, tint = category.tint(), modifier = Modifier.size(26.dp))
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(category.label), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                        Text(Formatter.formatShortFileSize(context, files.sumOf { it.sizeBytes }), color = Color(0xFFE5484D), style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .tapClickable("wa_view_all_${category.name.lowercase()}") { viewModel.openCategory(category) }
                            .padding(horizontal = 20.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.ai_view_all), color = Color.White, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                    }
                }
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files.sortedByDescending { it.sizeBytes }.take(PREVIEW_COUNT), key = { it.path }) { file ->
                        WaThumb(file, viewModel, Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)).tapClickable("wa_open_preview") { openUri(context, file.uri, mimeFor(file.name)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WaStat(label: String, value: String, dot: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.size(8.dp))
            Text(label, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 14.sp, fontFamily = medium))
        }
        Text(value, modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CategoryDetail(data: WaData, viewModel: WhatsAppViewModel) {
    val context = LocalContext.current
    val category = data.openCategory ?: return
    val visible = data.visibleFiles
    Column(modifier = Modifier.fillMaxSize()) {
        if (category.hasSentTab) {
            com.phonecleaner.aicleaner.presentation.ui.components.SegmentedTabs(
                options = listOf(stringResource(R.string.wa_tab_received), stringResource(R.string.wa_tab_sent)),
                selectedIndex = if (data.tab == WaTab.RECEIVED) 0 else 1,
                onSelect = { viewModel.setTab(if (it == 0) WaTab.RECEIVED else WaTab.SENT) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        if (visible.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(bottom = 80.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Image(painterResource(R.drawable.files_mascot), null, contentScale = ContentScale.Fit, modifier = Modifier.size(width = 190.dp, height = 172.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.wa_no_file_found), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(visible, key = { it.path }) { file ->
                    val selected = file.path in data.selected
                    Column {
                        WaThumb(
                            file, viewModel,
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    onClick = { tapEvent("wa_file_open"); if (data.isSelecting) viewModel.toggle(file) else openUri(context, file.uri, mimeFor(file.name)) },
                                    onLongClick = { viewModel.toggle(file) }
                                )
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Formatter.formatShortFileSize(context, file.sizeBytes),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                            )
                            Box(Modifier.size(32.dp).tapPlainClickable("wa_file_select") { viewModel.toggle(file) }, contentAlignment = Alignment.Center) {
                                com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck(selected, size = 22.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaThumb(file: WaFile, viewModel: WhatsAppViewModel, modifier: Modifier) {
    val thumb by produceState(initialValue = thumbCache[file.path], file.path) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { viewModel.thumbnail(file, 224)?.asImageBitmap() }
                ?.also { thumbCache.put(file.path, it) }
        }
    }
    Box(modifier.background(file.category.tint().copy(alpha = 0.12f))) {
        val image = thumb
        if (image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(file.category.icon(), null, tint = file.category.tint(), modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    file.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 9.sp, lineHeight = 11.sp, fontFamily = medium)
                )
            }
        }
        if (file.previewKind == WaPreviewKind.VIDEO) {
            Box(
                Modifier.align(Alignment.Center).size(30.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}

private const val PREVIEW_COUNT = 8

