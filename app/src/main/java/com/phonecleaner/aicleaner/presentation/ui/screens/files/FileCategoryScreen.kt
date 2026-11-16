package com.phonecleaner.aicleaner.presentation.ui.screens.files

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.phonecleaner.aicleaner.domain.FileCategoryViewModel
import com.phonecleaner.aicleaner.domain.FileViewMode
import com.phonecleaner.aicleaner.domain.model.DocumentType
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.domain.model.MediaFile
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.MediaThumbnail
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

private fun FileEntry.asMediaFile() = MediaFile(id, uri, name, sizeBytes, modifiedMs, category == FileCategory.VIDEO, 0, 0, 0L)

/** One category of the file manager (or the global search when [category] is null). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileCategoryScreen(
    category: FileCategory?,
    onBack: () -> Unit,
    viewModel: FileCategoryViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }
    var sheetEntry by remember { mutableStateOf<FileEntry?>(null) }
    var deleteTargets by remember { mutableStateOf<List<FileEntry>>(emptyList()) }

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

    LaunchedEffect(category, granted) {
        if (granted) {
            if (category == null) viewModel.loadAll() else viewModel.load(category)
        }
    }

    val inAlbum = state.openAlbum != null
    BackHandler(enabled = state.isSelecting || inAlbum || (state.searching && !state.global)) {
        when {
            state.isSelecting -> viewModel.clearSelection()
            inAlbum -> viewModel.openAlbum(null)
            else -> viewModel.setSearching(false)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Header(
            title = if (state.global) stringResource(R.string.files_search) else stringResource((category ?: FileCategory.IMAGES).label),
            state = state,
            onBack = {
                when {
                    state.isSelecting -> viewModel.clearSelection()
                    inAlbum -> viewModel.openAlbum(null)
                    else -> onBack()
                }
            },
            onSearchToggle = { viewModel.setSearching(!state.searching) },
            onQuery = viewModel::setQuery,
            onShare = { shareEntries(context, state.selectedEntries) },
            onDelete = { deleteTargets = state.selectedEntries },
            onSelectAll = viewModel::selectAllVisible
        )

        if (!state.global && category == FileCategory.DOCUMENTS) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentType.entries.forEach { type ->
                    ChoiceChip(state.documentType == type, stringResource(type.label)) { viewModel.setDocumentType(type) }
                }
            }
        }
        if (!state.global && (category == FileCategory.IMAGES || category == FileCategory.VIDEO) && !inAlbum) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(state.viewMode == FileViewMode.ALL, stringResource(R.string.files_tab_all)) { viewModel.setViewMode(FileViewMode.ALL) }
                ChoiceChip(state.viewMode == FileViewMode.ALBUM, stringResource(R.string.files_tab_album)) { viewModel.setViewMode(FileViewMode.ALBUM) }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val visible = state.visible
            when {
                !granted -> Box(Modifier.fillMaxSize())
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                visible.isEmpty() && !(state.viewMode == FileViewMode.ALBUM && !inAlbum) -> EmptyState(state.query.isNotBlank())
                state.viewMode == FileViewMode.ALBUM && !inAlbum && !state.global -> AlbumGrid(state.albums, viewModel::openAlbum)
                (category == FileCategory.IMAGES || category == FileCategory.VIDEO) && !state.global ->
                    MediaGrid(visible, state.selected, state.isSelecting, viewModel::toggleSelect)
                else -> EntryList(visible, state.selected, state.isSelecting, viewModel::toggleSelect) { sheetEntry = it }
            }
        }
    }

    if (!granted) {
        StoragePermissionPrompt(
            onDismiss = onBack,
            accent = (category ?: FileCategory.IMAGES).tint()
        ) {
            if (StoragePermission.usesSettingsScreen) settingsLauncher.launch(StoragePermission.settingsIntent(context))
            else legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
        }
    }

    sheetEntry?.let { entry ->
        FileItemSheet(
            file = SheetFile(entry.name, entry.path, entry.sizeBytes, entry.modifiedMs, entry.mime, entry.category != FileCategory.APK),
            onDismiss = { sheetEntry = null },
            onOpen = { openEntry(context, entry) },
            onShare = { shareEntries(context, listOf(entry)) },
            onDelete = { deleteTargets = listOf(entry) }
        )
    }

    if (deleteTargets.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { deleteTargets = emptyList() },
            title = { Text(stringResource(R.string.junk_confirm_title)) },
            text = { Text(stringResource(R.string.junk_confirm_body, deleteTargets.size, Formatter.formatFileSize(context, deleteTargets.sumOf { it.sizeBytes }))) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(deleteTargets)
                    deleteTargets = emptyList()
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTargets = emptyList() }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun Header(
    title: String,
    state: com.phonecleaner.aicleaner.domain.FileCategoryState,
    onBack: () -> Unit,
    onSearchToggle: () -> Unit,
    onQuery: (String) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconAction(if (state.isSelecting) Icons.Rounded.Close else Icons.Rounded.ArrowBack, onBack)
        when {
            state.isSelecting -> {
                Text(
                    stringResource(R.string.files_selected_count, state.selected.size),
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 19.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Text(
                    stringResource(R.string.junk_select_all),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).combinedClickable(onClick = onSelectAll).padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
                IconAction(Icons.Rounded.Share, onShare)
                IconAction(Icons.Rounded.DeleteOutline, onDelete, tint = Brand.Danger)
            }

            state.searching -> {
                Row(
                    modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.secondary).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = state.query,
                        onValueChange = onQuery,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 15.sp, fontFamily = medium, color = MaterialTheme.colorScheme.tertiary),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            if (state.query.isEmpty()) {
                                Text(stringResource(R.string.files_search_hint), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 15.sp, fontFamily = medium))
                            }
                            inner()
                        }
                    )
                }
                if (!state.global) IconAction(Icons.Rounded.Close, onSearchToggle)
            }

            else -> {
                Text(
                    title,
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                IconAction(Icons.Rounded.Search, onSearchToggle)
            }
        }
    }
}

@Composable
private fun IconAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.tertiary) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint) }
}

@Composable
private fun ChoiceChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1, softWrap = false) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.tertiary,
            selectedContainerColor = primary.copy(alpha = 0.15f),
            selectedLabelColor = primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            selectedBorderColor = primary
        )
    )
}

@Composable
private fun EmptyState(searching: Boolean) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Rounded.SearchOff, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(if (searching) R.string.files_no_results else R.string.files_empty),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaGrid(entries: List<FileEntry>, selected: Set<String>, selecting: Boolean, onToggle: (FileEntry) -> Unit) {
    val context = LocalContext.current
    val groups = remember(entries) { entries.groupedByDay(context) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp + getBannerOverlayHeight()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groups.forEach { (day, items) ->
            item(span = { GridItemSpan(3) }, key = "header_$day") { DayHeader(day) }
            items(items, key = { it.path }) { entry ->
                val isSelected = entry.path in selected
                val sheetContext = LocalContext.current
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            onClick = { if (selecting) onToggle(entry) else openEntry(sheetContext, entry) },
                            onLongClick = { onToggle(entry) }
                        )
                ) {
                    MediaThumbnail(entry.asMediaFile(), Modifier.fillMaxSize())
                    if (selecting) {
                        AppCheckbox(checked = isSelected, onCheckedChange = { onToggle(entry) }, modifier = Modifier.align(Alignment.TopStart).size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: String) {
    Text(
        day,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp, start = 4.dp),
        color = MaterialTheme.colorScheme.tertiary,
        style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun AlbumGrid(albums: List<Pair<String, List<FileEntry>>>, onOpen: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums, key = { it.first }) { (name, items) ->
            Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).combinedClickable(onClick = { onOpen(name) })) {
                MediaThumbnail(items.first().asMediaFile(), Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)))
                Text(name.ifBlank { stringResource(R.string.files_internal_storage) }, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp, start = 2.dp),
                    color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                Text("${items.size}", color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(start = 2.dp), style = TextStyle(fontSize = 12.sp, fontFamily = medium))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryList(
    entries: List<FileEntry>,
    selected: Set<String>,
    selecting: Boolean,
    onToggle: (FileEntry) -> Unit,
    onMore: (FileEntry) -> Unit
) {
    val context = LocalContext.current
    val groups = remember(entries) { entries.groupedByDay(context) }
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())) {
        groups.forEach { (day, items) ->
            item(key = "header_$day") { DayHeader(day) }
            items(items.size, key = { items[it].path }) { index ->
                val entry = items[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .combinedClickable(
                            onClick = { if (selecting) onToggle(entry) else openEntry(context, entry) },
                            onLongClick = { onToggle(entry) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selecting) AppCheckbox(checked = entry.path in selected, onCheckedChange = { onToggle(entry) }, modifier = Modifier.size(40.dp))
                    EntryLeading(entry)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(entry.name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis, color = MaterialTheme.colorScheme.tertiary,
                            style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                        Text(Formatter.formatShortFileSize(context, entry.sizeBytes) + " · " + entry.folderName.ifBlank { stringResource(R.string.files_internal_storage) }, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, fontFamily = medium))
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).combinedClickable(onClick = { onMore(entry) }),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.MoreVert, null, tint = MaterialTheme.colorScheme.surface) }
                }
            }
        }
    }
}

@Composable
private fun EntryLeading(entry: FileEntry) {
    if (entry.isMedia) {
        MediaThumbnail(entry.asMediaFile(), Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)), sizePx = 160)
    } else {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(entry.category.tint().copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(entry.category.icon(), null, tint = entry.category.tint(), modifier = Modifier.size(26.dp))
        }
    }
}
