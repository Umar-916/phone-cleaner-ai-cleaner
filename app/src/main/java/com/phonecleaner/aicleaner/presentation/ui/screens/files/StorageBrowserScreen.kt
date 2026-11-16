package com.phonecleaner.aicleaner.presentation.ui.screens.files

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.MoreVert
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
import com.phonecleaner.aicleaner.domain.StorageBrowserViewModel
import com.phonecleaner.aicleaner.domain.model.BrowserItem
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

/** Folder-by-folder view of the internal storage. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StorageBrowserScreen(
    onBack: () -> Unit,
    viewModel: StorageBrowserViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }
    var sheetItem by remember { mutableStateOf<BrowserItem?>(null) }
    var deleteItem by remember { mutableStateOf<BrowserItem?>(null) }

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
    BackHandler(enabled = granted && !state.isRoot) { viewModel.up() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).combinedClickable(onClick = { if (!viewModel.up()) onBack() }),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.ArrowBack, null, tint = MaterialTheme.colorScheme.tertiary) }
            Column(modifier = Modifier.padding(start = 6.dp)) {
                Text(
                    if (state.isRoot) stringResource(R.string.files_internal_storage) else state.directory.substringAfterLast('/'),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Text(
                    state.directory,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 11.sp, fontFamily = medium)
                )
            }
        }

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

                else -> LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())) {
                    items(state.items, key = { it.path }) { item ->
                        BrowserRow(
                            item = item,
                            onClick = { if (item.isDirectory) viewModel.open(item.path) else openPath(context, item.path) },
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

    sheetItem?.let { item ->
        FileItemSheet(
            file = SheetFile(item.name, item.path, item.sizeBytes, item.modifiedMs, "", canOpen = !item.isDirectory, canShare = !item.isDirectory),
            onDismiss = { sheetItem = null },
            onOpen = { openPath(context, item.path) },
            onShare = { sharePath(context, item.path) },
            onDelete = { deleteItem = item }
        )
    }

    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text(stringResource(R.string.junk_confirm_title)) },
            text = { Text(item.name) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item)
                    deleteItem = null
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteItem = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BrowserRow(item: BrowserItem, onClick: () -> Unit, onMore: () -> Unit) {
    val context = LocalContext.current
    val tint = if (item.isDirectory) Brand.Gold else Brand.Blue
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).combinedClickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) { Icon(if (item.isDirectory) Icons.Rounded.Folder else Icons.Rounded.InsertDriveFile, null, tint = tint, modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis, color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            Text(
                if (item.isDirectory) stringResource(R.string.files_items_count, item.childCount) else Formatter.formatShortFileSize(context, item.sizeBytes) + " · " + formatDay(context, item.modifiedMs),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, fontFamily = medium)
            )
        }
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).combinedClickable(onClick = onMore),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.MoreVert, null, tint = MaterialTheme.colorScheme.surface) }
    }
}
