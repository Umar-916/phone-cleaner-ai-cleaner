package com.phonecleaner.aicleaner.presentation.ui.screens.files

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapEvent
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.StoragePermission
import com.phonecleaner.aicleaner.domain.BrowserSort
import com.phonecleaner.aicleaner.domain.StorageBrowserViewModel
import com.phonecleaner.aicleaner.domain.model.BrowserItem
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.FloatingSheet
import com.phonecleaner.aicleaner.presentation.ui.components.MenuAction
import com.phonecleaner.aicleaner.presentation.ui.components.MenuSheet
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StorageBrowserScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit = {},
    viewModel: StorageBrowserViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }
    var sheetItem by remember { mutableStateOf<BrowserItem?>(null) }
    var deleteItem by remember { mutableStateOf<BrowserItem?>(null) }
    var deleteSelection by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    var folderOpen by remember { mutableStateOf(false) }

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

    LaunchedEffect(granted) { if (granted) viewModel.open() }
    BackHandler(enabled = granted && (state.isSelecting || !state.isRoot)) {
        if (state.isSelecting) viewModel.clearSelection() else viewModel.up()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BarIcon(if (state.isSelecting) Icons.Rounded.Close else Icons.Rounded.ArrowBack) {
                if (state.isSelecting) viewModel.clearSelection() else if (!viewModel.up()) onBack()
            }
            Text(
                when {
                    state.isSelecting -> stringResource(R.string.files_selected_count, state.selected.size)
                    state.isRoot -> stringResource(R.string.files_manager_title)
                    else -> state.directory.substringAfterLast('/')
                },
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            if (state.isSelecting) {
                BarIcon(Icons.Rounded.Share) {
                    val files = state.items.filter { it.path in state.selected && !it.isDirectory }
                    files.firstOrNull()?.let { sharePath(context, it.path) }
                }
                BarIcon(Icons.Rounded.DeleteOutline, tint = Brand.Danger) { deleteSelection = true }
            } else {
                BarIcon(Icons.Rounded.Search, onClick = onSearch.tapped("storage_browser_screen_tap"))
                BarIcon(Icons.Rounded.MoreVert) { menuOpen = true }
            }
        }
        Text(
            if (state.isRoot) stringResource(R.string.files_internal_shared) else state.directory,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                !granted -> Box(Modifier.fillMaxSize())
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                state.items.isEmpty() -> Text(
                    stringResource(R.string.files_folder_empty),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 15.sp, fontFamily = medium)
                )

                else -> LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + getBannerOverlayHeight())) {
                    items(state.items, key = { it.path }) { item ->
                        BrowserRow(
                            item = item,
                            selected = item.path in state.selected,
                            selecting = state.isSelecting,
                            onClick = { tapEvent("storage_browser_screen_tap_2");
                                when {
                                    state.isSelecting -> viewModel.toggleSelect(item)
                                    item.isDirectory -> viewModel.open(item.path)
                                    else -> openPath(context, item.path)
                                }
                            },
                            onLongClick = { viewModel.toggleSelect(item) },
                            onMore = { sheetItem = item }
                        )
                    }
                }
            }
        }
    }

    if (!granted) {
        StoragePermissionPrompt(onDismiss = onBack, accent = Brand.Blue) {
            if (StoragePermission.usesSettingsScreen) settingsLauncher.launch(StoragePermission.settingsIntent(context))
            else legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
        }
    }

    if (menuOpen) {
        MenuSheet(
            actions = listOf(
                MenuAction(Icons.Outlined.CheckBox, stringResource(R.string.junk_select_all)) { viewModel.selectAll() },
                MenuAction(Icons.Outlined.Sort, stringResource(R.string.files_sort)) { sortOpen = true },
                MenuAction(
                    if (state.showHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    stringResource(if (state.showHidden) R.string.files_hide_hidden else R.string.files_show_hidden)
                ) { viewModel.toggleHidden() },
                MenuAction(Icons.Outlined.CreateNewFolder, stringResource(R.string.files_create_folder)) { folderOpen = true }
            ),
            onDismiss = { menuOpen = false }
        )
    }

    if (sortOpen) {
        MenuSheet(
            actions = listOf(
                MenuAction(if (state.sort == BrowserSort.NAME) Icons.Rounded.Check else Icons.Outlined.Sort, stringResource(R.string.files_sort_name)) { viewModel.setSort(BrowserSort.NAME) },
                MenuAction(if (state.sort == BrowserSort.SIZE) Icons.Rounded.Check else Icons.Outlined.Sort, stringResource(R.string.files_sort_size)) { viewModel.setSort(BrowserSort.SIZE) },
                MenuAction(if (state.sort == BrowserSort.DATE) Icons.Rounded.Check else Icons.Outlined.Sort, stringResource(R.string.files_sort_date)) { viewModel.setSort(BrowserSort.DATE) }
            ),
            onDismiss = { sortOpen = false }
        )
    }

    if (folderOpen) {
        CreateFolderSheet(
            onDismiss = { folderOpen = false },
            onCreate = { name, onResult -> viewModel.createFolder(name, onResult) }
        )
    }

    sheetItem?.let { item ->
        FileItemSheet(
            file = SheetFile(item.name, item.path, item.sizeBytes, item.modifiedMs, "", canOpen = !item.isDirectory, canShare = !item.isDirectory),
            onDismiss = { sheetItem = null },
            onOpen = { openPath(context, item.path) },
            onShare = { sharePath(context, item.path) },
            onDelete = { deleteItem = item }
        )
    }

    if (deleteItem != null || deleteSelection) {
        val single = deleteItem
        AlertDialog(
            onDismissRequest = { deleteItem = null; deleteSelection = false },
            title = { Text(stringResource(R.string.junk_confirm_title)) },
            text = { Text(single?.name ?: stringResource(R.string.files_selected_count, state.selected.size)) },
            confirmButton = {
                TextButton(onClick = { tapEvent("storage_browser_screen_tap_3");
                    if (single != null) viewModel.delete(single) else viewModel.deleteSelected()
                    deleteItem = null
                    deleteSelection = false
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { tapEvent("storage_browser_screen_tap_4"); deleteItem = null; deleteSelection = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun BarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color = MaterialTheme.colorScheme.tertiary, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).combinedClickableSafe(onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint) }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableSafe(onClick: () -> Unit): Modifier = this.combinedClickable(onClick = onClick)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BrowserRow(
    item: BrowserItem,
    selected: Boolean,
    selecting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMore: () -> Unit
) {
    val context = LocalContext.current
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        tween(180), label = "browserRow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (item.isDirectory) Icons.Outlined.Folder else Icons.Outlined.InsertDriveFile,
            null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = medium, fontWeight = FontWeight.Medium)
            )
            if (!item.isDirectory) {
                Text(
                    Formatter.formatShortFileSize(context, item.sizeBytes) + " · " + formatDay(context, item.modifiedMs),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                )
            }
        }
        if (selecting) {
            com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck(selected, Modifier.padding(end = 8.dp), size = 22.dp)
        } else {
            Box(Modifier.size(40.dp).clip(CircleShape).combinedClickable(onClick = onMore.tapped("browser_row_tap")), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.MoreVert, null, tint = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

@Composable
private fun CreateFolderSheet(onDismiss: () -> Unit, onCreate: (String, (Boolean) -> Unit) -> Unit) {
    var name by remember { mutableStateOf("") }
    var clash by remember { mutableStateOf(false) }
    FloatingSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
            Text(
                stringResource(R.string.files_create_folder),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it; clash = false },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 16.sp, fontFamily = medium, color = MaterialTheme.colorScheme.tertiary),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (name.isEmpty()) Text(stringResource(R.string.files_folder_name), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 16.sp, fontFamily = medium))
                        inner()
                    }
                )
            }
            if (clash) {
                Text(
                    stringResource(R.string.files_folder_exists),
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                )
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)).background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(onClick = { tapEvent("create_folder_sheet_tap");
                        onCreate(name) { ok -> if (ok) onDismiss() else clash = name.isNotBlank() }
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.files_create), color = Color.White, style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
