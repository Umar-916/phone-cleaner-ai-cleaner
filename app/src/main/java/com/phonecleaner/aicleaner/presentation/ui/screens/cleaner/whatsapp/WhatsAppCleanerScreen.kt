package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.whatsapp

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
import com.phonecleaner.aicleaner.presentation.ui.components.MediaPermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.StoragePermission
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

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
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
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }
    var confirm by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = StoragePermission.isGranted(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val legacyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = StoragePermission.isGranted(context)
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        granted = StoragePermission.isGranted(context)
    }

    LaunchedEffect(granted, state is WaUiState.Idle) {
        if (granted && state is WaUiState.Idle) {
            context.postAnalytic("wa_scan_start")
            viewModel.startScan()
        }
    }

    val data = (state as? WaUiState.Result)?.data
    val busy = state is WaUiState.Scanning || state is WaUiState.Cleaning
    BackHandler(enabled = busy || data?.openCategory != null) {
        if (!busy) viewModel.openCategory(null)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                WaUiState.Idle -> if (!granted) {
                    StoragePermissionPrompt(onDismiss = onBack, accent = Brand.WhatsApp, body = stringResource(R.string.wa_permission_body)) {
                        if (StoragePermission.usesSettingsScreen) {
                            settingsLauncher.launch(StoragePermission.settingsIntent(context))
                        } else {
                            legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
                        }
                    }
                }

                is WaUiState.Scanning -> FeatureProgress(
                    title = stringResource(R.string.wa_scanning),
                    subtitle = stringResource(R.string.junk_found_format, Formatter.formatFileSize(context, current.bytes), current.count),
                    icon = Icons.Rounded.DonutLarge,
                    accent = Brand.WhatsApp
                )
                WaUiState.NotFound -> NotFoundView(viewModel::reset)
                is WaUiState.Result -> ResultView(current.data, viewModel) { confirm = true }
                is WaUiState.Cleaning -> FeatureProgress(
                    title = stringResource(R.string.junk_cleaning),
                    progress = if (current.total == 0) 0f else current.done.toFloat() / current.total,
                    subtitle = "${current.done} / ${current.total}",
                    accent = Brand.WhatsApp
                )
                is WaUiState.Done -> {
                    val summary = stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, current.freedBytes), current.removedCount)
                    DoneView(current) {
                        viewModel.reset()
                        onTaskCompleted(summary)
                    }
                }
            }
        }
    }

    if (confirm && data != null) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.junk_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.junk_confirm_body,
                        data.selected.size,
                        Formatter.formatFileSize(context, data.selectedBytes)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    context.postAnalytic("wa_clean_confirm")
                    viewModel.cleanSelected()
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
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
            .clickable(onClick = onClick),
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
            if (data.openCategory == null) {
                Overview(data, viewModel)
            } else {
                CategoryDetail(data, viewModel)
            }
        }
        if (data.selected.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())
            ) {
                PrimaryButton(
                    stringResource(R.string.wa_delete_selected, data.selected.size, Formatter.formatFileSize(context, data.selectedBytes)),
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
    val categories = WaCategory.entries.filter { data.byCategory[it].orEmpty().isNotEmpty() }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))))
                    .padding(20.dp)
            ) {
                Text(
                    Formatter.formatFileSize(context, data.totalBytes),
                    color = Color.White,
                    style = TextStyle(fontSize = 30.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Text(
                    stringResource(R.string.wa_total_summary, data.files.size),
                    color = Color.White.copy(alpha = 0.9f),
                    style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                )
            }
        }
        items(categories, key = { it.name }) { category ->
            val files = data.byCategory[category].orEmpty()
            val selectedCount = files.count { it.path in data.selected }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { viewModel.openCategory(category) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(category.tint().copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(category.icon(), null, tint = category.tint(), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    AppCheckbox(
                        checked = selectedCount == files.size,
                        onCheckedChange = { viewModel.toggleCategory(category) },
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(category.label),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    stringResource(R.string.wa_category_summary, files.size, Formatter.formatShortFileSize(context, files.sumOf { it.sizeBytes })),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                )
            }
        }
    }
}

@Composable
private fun CategoryDetail(data: WaData, viewModel: WhatsAppViewModel) {
    val visible = data.visibleFiles
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WaFilter.entries.forEach { filter ->
                PillChip(stringResource(filter.label), data.filter == filter, { viewModel.setFilter(filter) })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.wa_files_count, visible.size),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 13.sp, fontFamily = medium)
            )
            if (visible.isNotEmpty()) {
                val allSelected = data.selected.containsAll(visible.map { it.path })
                TextButton(onClick = viewModel::toggleVisible) {
                    Text(
                        stringResource(if (allSelected) R.string.junk_deselect_all else R.string.junk_select_all),
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        if (visible.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.wa_no_files_filter),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 15.sp, fontFamily = medium)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visible, key = { it.path }) { file ->
                    FileCell(file, file.path in data.selected, viewModel) { viewModel.toggle(file) }
                }
            }
        }
    }
}

@Composable
private fun FileCell(file: WaFile, selected: Boolean, viewModel: WhatsAppViewModel, onToggle: () -> Unit) {
    val context = LocalContext.current
    val thumb by produceState(initialValue = thumbCache[file.path], file.path) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { viewModel.thumbnail(file, 256)?.asImageBitmap() }
                ?.also { thumbCache.put(file.path, it) }
        }
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(file.category.tint().copy(alpha = 0.12f))
            .clickable(onClick = onToggle)
    ) {
        val image = thumb
        if (image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(file.category.icon(), null, tint = file.category.tint(), modifier = Modifier.size(30.dp))
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
        AppCheckbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.align(Alignment.TopStart).size(36.dp)
        )
        Text(
            Formatter.formatShortFileSize(context, file.sizeBytes),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 5.dp, vertical = 1.dp),
            color = Color.White,
            style = TextStyle(fontSize = 10.sp, fontFamily = semiBold)
        )
    }
}

@Composable
private fun DoneView(state: WaUiState.Done, onDone: () -> Unit) {
    val context = LocalContext.current
    CenteredColumn {
        Icon(Icons.Rounded.CheckCircle, null, tint = WhatsAppGreen, modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.junk_done_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, state.freedBytes), state.removedCount),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 15.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(32.dp))
        PrimaryButton(stringResource(R.string.junk_done_action), onDone)
    }
}

@Composable
private fun NotFoundView(onRescan: () -> Unit) {
    CenteredColumn {
        Icon(Icons.Rounded.SearchOff, null, tint = WhatsAppGreen, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.wa_not_found_title),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.wa_not_found_body),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(stringResource(R.string.junk_rescan), onRescan)
    }
}
