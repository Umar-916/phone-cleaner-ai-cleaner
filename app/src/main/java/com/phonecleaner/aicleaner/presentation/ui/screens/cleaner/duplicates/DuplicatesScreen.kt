package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.duplicates

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.SegmentedTabs
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.presentation.ui.components.MediaPermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.data.media.recoverableBytes
import com.phonecleaner.aicleaner.domain.DuplicateTab
import com.phonecleaner.aicleaner.domain.DuplicatesData
import com.phonecleaner.aicleaner.domain.DuplicatesUiState
import com.phonecleaner.aicleaner.domain.DuplicatesViewModel
import com.phonecleaner.aicleaner.domain.model.DuplicateGroup
import com.phonecleaner.aicleaner.domain.model.DuplicateKind
import com.phonecleaner.aicleaner.domain.model.DuplicatePhase
import com.phonecleaner.aicleaner.domain.model.MediaFile
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.MediaThumbnail
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val Gold = Color(0xFFF5B301)

private fun requiredPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: DuplicatesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(hasPermissions(context)) }
    var requestedOnce by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = hasPermissions(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        requestedOnce = true
        granted = hasPermissions(context)
    }

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onDeleted()
    }

    LaunchedEffect(granted, state is DuplicatesUiState.Idle) {
        if (granted && state is DuplicatesUiState.Idle) {
            context.postAnalytic("duplicates_scan_start")
            viewModel.startScan()
        }
    }

    BackHandler(enabled = state is DuplicatesUiState.Scanning) {}

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_duplicates)) },
                onBack = if (state is DuplicatesUiState.Scanning) null else onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                DuplicatesUiState.Idle -> if (!granted) {
                    MediaPermissionPrompt(
                        showSettings = requestedOnce,
                        onDismiss = onBack,
                        onAllow = {
                            if (requestedOnce) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                                tryCatch { context.startActivity(intent) }
                            } else {
                                permissionLauncher.launch(requiredPermissions())
                            }
                        }
                    )
                }

                is DuplicatesUiState.Scanning -> FeatureProgress(
                    title = stringResource(
                        when (current.progress.phase) {
                            DuplicatePhase.READING -> R.string.dup_phase_reading
                            DuplicatePhase.COMPARING -> R.string.dup_phase_comparing
                            DuplicatePhase.ANALYZING -> R.string.dup_phase_analyzing
                        }
                    ),
                    progress = if (current.progress.total > 0) (current.progress.done.toFloat() / current.progress.total).coerceIn(0f, 1f) else null,
                    subtitle = if (current.progress.total > 0) "${current.progress.done} / ${current.progress.total}" else null,
                    icon = Icons.Rounded.ContentCopy,
                    accent = Brand.Duplicates
                )
                is DuplicatesUiState.Result -> {
                    val data = current.data
                    if (data.photoGroups.isEmpty() && data.videoGroups.isEmpty()) {
                        EmptyView(viewModel::reset)
                    } else {
                        ResultView(data, viewModel) { confirm = true }
                    }
                }

                is DuplicatesUiState.Done -> {
                    val summary = stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, current.freedBytes), current.removedCount)
                    DoneView(current) {
                        viewModel.reset()
                        onTaskCompleted(summary)
                    }
                }
            }
        }
    }

    val data = (state as? DuplicatesUiState.Result)?.data
    if (confirm && data != null) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.dup_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dup_confirm_body,
                        data.selected.size,
                        Formatter.formatFileSize(context, data.selectedBytes)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    context.postAnalytic("duplicates_delete_confirm")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val uris: List<Uri> = data.selectedFiles.map { it.uri }
                        val sender = MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
                        deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
                    } else {
                        viewModel.deleteLegacy()
                    }
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private fun hasPermissions(context: android.content.Context): Boolean =
    requiredPermissions().all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

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
private fun ResultView(data: DuplicatesData, viewModel: DuplicatesViewModel, onDelete: () -> Unit) {
    val context = LocalContext.current
    val groups = data.activeGroups

    Column(modifier = Modifier.fillMaxSize()) {
        SegmentedTabs(
            options = listOf(stringResource(R.string.dup_tab_photos, data.photoGroups.size), stringResource(R.string.dup_tab_videos, data.videoGroups.size)),
            selectedIndex = if (data.tab == DuplicateTab.PHOTOS) 0 else 1,
            onSelect = { viewModel.setTab(if (it == 0) DuplicateTab.PHOTOS else DuplicateTab.VIDEOS) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dup_original_hint),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontFamily = medium)
            )
            TextButton(onClick = { if (data.selected.isEmpty()) viewModel.selectAllButKeepers() else viewModel.deselectAll() }) {
                Text(
                    stringResource(if (data.selected.isEmpty()) R.string.dup_select_all_but_one else R.string.junk_deselect_all),
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
        }

        if (groups.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.dup_none_in_tab),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 15.sp, fontFamily = medium)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    GroupCard(group, data, viewModel)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())
        ) {
            val label = if (data.selected.isEmpty()) {
                stringResource(R.string.dup_nothing_selected)
            } else {
                stringResource(R.string.dup_delete_selected, data.selected.size, Formatter.formatFileSize(context, data.selectedBytes))
            }
            PrimaryButton(label, onClick = { if (data.selected.isNotEmpty()) onDelete() })
        }
    }
}


@Composable
private fun GroupCard(group: DuplicateGroup, data: DuplicatesData, viewModel: DuplicatesViewModel) {
    val context = LocalContext.current
    val keeper = data.keeperOf(group)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(vertical = 12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        if (group.kind == DuplicateKind.EXACT) R.string.dup_kind_exact else R.string.dup_kind_similar,
                        group.files.size
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    stringResource(R.string.dup_group_savings, Formatter.formatFileSize(context, group.recoverableBytes())),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            group.files.forEach { file ->
                MediaCell(
                    file = file,
                    isKeeper = file.id == keeper,
                    isSelected = file.id in data.selected,
                    onToggle = { viewModel.toggle(file) },
                    onKeep = { viewModel.setKeeper(group, file) }
                )
            }
        }
    }
}

@Composable
private fun MediaCell(
    file: MediaFile,
    isKeeper: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onKeep: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.width(112.dp)) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggle)
        ) {
            MediaThumbnail(file, Modifier.fillMaxSize())
            if (isKeeper) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Gold)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(
                        stringResource(R.string.dup_keep),
                        color = Color.White,
                        style = TextStyle(fontSize = 10.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                AppCheckbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.align(Alignment.TopStart).size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            file.name,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 11.sp, fontFamily = medium)
        )
        Text(
            Formatter.formatShortFileSize(context, file.sizeBytes) + " · " +
                DateFormat.getMediumDateFormat(context).format(Date(file.addedAtMs)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 10.sp, fontFamily = medium)
        )
        if (!isKeeper) {
            Text(
                stringResource(R.string.dup_make_original),
                modifier = Modifier.clickable(onClick = onKeep).padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 11.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun DoneView(state: DuplicatesUiState.Done, onDone: () -> Unit) {
    val context = LocalContext.current
    CenteredColumn {
        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(96.dp))
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
private fun EmptyView(onRescan: () -> Unit) {
    CenteredColumn {
        Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.dup_empty_title),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.dup_empty_body),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(stringResource(R.string.junk_rescan), onRescan)
    }
}
