package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.duplicates

import com.phonecleaner.aicleaner.presentation.ui.components.OnResumeEffect
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapEvent
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.app.Activity
import android.content.Intent
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.SegmentedTabs
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.presentation.ui.components.FolderAccessPrompt
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck
import com.phonecleaner.aicleaner.presentation.ui.screens.files.openUri
import androidx.compose.material.icons.rounded.PlayCircle
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.presentation.ui.components.FolderAccessPrompt
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

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold
private val Gold = Color(0xFFF5B301)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: DuplicatesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(FolderAccess.mediaRoots(context).isNotEmpty()) }
    OnResumeEffect { granted = FolderAccess.mediaRoots(context).isNotEmpty() }
    var revealed by remember { mutableStateOf(false) }
    val scanAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(state) { if (state is DuplicatesUiState.Idle) { revealed = false; scanAnim.snapTo(0f) } }


    LaunchedEffect(granted, state is DuplicatesUiState.Idle) {
        if (granted && state is DuplicatesUiState.Idle) {
            context.postAnalytic("duplicates_scan_start")
            viewModel.startScan()
        }
    }

    var stopSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = state is DuplicatesUiState.Scanning) { stopSheet = true }
    androidx.compose.runtime.LaunchedEffect(state is DuplicatesUiState.Scanning) { if (state !is DuplicatesUiState.Scanning) stopSheet = false }
    if (stopSheet) com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet(
        onCancel = { stopSheet = false; viewModel.reset(); onBack() },
        onDismiss = { stopSheet = false }
    )

    val current0 = state
    val loading = current0 is DuplicatesUiState.Scanning || (current0 is DuplicatesUiState.Result && !revealed)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!loading) AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_duplicates)) },
                onBack = if (state is DuplicatesUiState.Scanning) null else onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(if (loading) PaddingValues(0.dp) else padding)) {
            when (val current = state) {
                DuplicatesUiState.Idle -> if (!granted) {
                    FolderAccessPrompt(onDismiss = onBack, onGranted = { granted = true }, accent = Brand.Duplicates)
                }

                is DuplicatesUiState.Scanning -> com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(
                    progress = if (current.progress.total > 0) (current.progress.done.toFloat() / current.progress.total).coerceIn(0f, 1f) else null,
                    finished = false,
                    label = stringResource(
                        when (current.progress.phase) {
                            DuplicatePhase.READING -> R.string.dup_phase_reading
                            DuplicatePhase.COMPARING -> R.string.dup_phase_comparing
                            DuplicatePhase.ANALYZING -> R.string.dup_phase_analyzing
                        }
                    ),
                    p = scanAnim
                )
                is DuplicatesUiState.Result -> {
                    val data = current.data
                    if (!revealed) {
                        com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView(null, true, stringResource(R.string.dup_phase_analyzing), scanAnim) { revealed = true }
                    } else if (data.photoGroups.isEmpty() && data.videoGroups.isEmpty()) {
                        com.phonecleaner.aicleaner.presentation.ui.components.ResultEmptyState(androidx.compose.material.icons.Icons.Rounded.CheckCircle, Brand.Good, stringResource(R.string.dup_none_title), stringResource(R.string.dup_none_body), stringResource(R.string.empty_done), onBack)
                    } else {
                        ResultView(data, viewModel) {
                            context.postAnalytic("duplicates_delete_confirm")
                            viewModel.deleteSelected()
                        }
                    }
                }

                is DuplicatesUiState.Done -> {
                    val summary = stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, current.freedBytes), current.removedCount)
                    com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck(label = com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor(extra = com.phonecleaner.aicleaner.domain.ExtraTask.DUPLICATES)) {
                        viewModel.reset()
                        onTaskCompleted(summary)
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

        val bestShots = data.selected.isNotEmpty()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .tapClickable("duplicates_keep_best") { if (bestShots) viewModel.deselectAll() else viewModel.selectAllButKeepers() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.dup_keep_best),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            RoundCheck(bestShots)
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    GroupSection(group, data, viewModel)
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
            PrimaryButton(label, onClick = { tapEvent("duplicates_delete"); if (data.selected.isNotEmpty()) onDelete() })
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun GroupSection(group: DuplicateGroup, data: DuplicatesData, viewModel: DuplicatesViewModel) {
    val context = LocalContext.current
    val keeper = data.keeperOf(group)
    Column(Modifier.fillMaxWidth()) {
        Text(
            group.files.firstOrNull()?.name.orEmpty(),
            maxLines = 2,
            overflow = TextOverflow.MiddleEllipsis,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        Text(
            stringResource(R.string.dup_group_savings, Formatter.formatFileSize(context, group.recoverableBytes())),
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(10.dp))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
    Column(modifier = Modifier.width(104.dp)) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(14.dp))
                .tapClickable("duplicates_open_item") { openUri(context, file.uri, if (file.isVideo) "video/*" else "image/*") }
        ) {
            MediaThumbnail(file, Modifier.fillMaxSize())
            if (file.isVideo) {
                Icon(
                    Icons.Rounded.PlayCircle, null, tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(30.dp)
                )
            }
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
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Formatter.formatShortFileSize(context, file.sizeBytes),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            if (!isKeeper) {
                Box(Modifier.size(30.dp).tapPlainClickable("duplicates_select_item", onClick = onToggle), contentAlignment = Alignment.Center) {
                    RoundCheck(isSelected, size = 22.dp)
                }
            }
        }
        if (!isKeeper) {
            Text(
                stringResource(R.string.dup_make_original),
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).tapClickable("duplicates_make_original", onClick = onKeep).padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 11.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

