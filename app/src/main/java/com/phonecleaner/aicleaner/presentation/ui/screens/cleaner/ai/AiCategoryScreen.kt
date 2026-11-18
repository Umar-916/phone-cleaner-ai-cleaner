package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.tapClickable
import com.phonecleaner.aicleaner.core.common.tapEvent
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.domain.AiCategoryPhase
import com.phonecleaner.aicleaner.domain.AiCategoryViewModel
import com.phonecleaner.aicleaner.domain.ExtraTask
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck
import com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView
import com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck
import com.phonecleaner.aicleaner.presentation.ui.components.doneLabelFor
import com.phonecleaner.aicleaner.presentation.ui.screens.files.openEntry
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AiCategoryScreen(
    kind: AiKind,
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    viewModel: AiCategoryViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var doneReady by remember { mutableStateOf(false) }

    LaunchedEffect(kind) { viewModel.load(kind) }
    BackHandler(enabled = state.isSelecting && state.phase == AiCategoryPhase.Browsing) { viewModel.clearSelection() }

    when (val phase = state.phase) {
        AiCategoryPhase.Cleaning -> ScanLoadingView(null, false, stringResource(R.string.junk_cleaning))
        is AiCategoryPhase.Done -> {
            if (!doneReady) {
                ScanLoadingView(null, true, stringResource(R.string.junk_cleaning)) { doneReady = true }
            } else {
                val summary = stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, phase.freedBytes), phase.removed)
                TaskDoneCheck(label = doneLabelFor(extra = ExtraTask.AI)) { onFinished(summary) }
            }
        }
        AiCategoryPhase.Browsing -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (state.isSelecting) {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.files_selected_count, state.selected.size),
                                color = MaterialTheme.colorScheme.tertiary,
                                style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            Box(Modifier.padding(start = 8.dp).size(44.dp).clip(CircleShape).tapClickable("ai_category_clear", onClick = viewModel::clearSelection), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.tertiary)
                            }
                        },
                        actions = {
                            Box(Modifier.padding(end = 12.dp).size(44.dp).tapPlainClickable("ai_category_select_all", onClick = viewModel::toggleAll), contentAlignment = Alignment.Center) {
                                RoundCheck(state.allSelected)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                } else {
                    AppTopBar(title = { AppTopBarTitle(stringResource(kind.titleRes())) }, onBack = onBack)
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp + getBannerOverlayHeight()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.items, key = { it.path }) { entry ->
                        Cell(entry, kind, entry.path in state.selected, state.isSelecting, { viewModel.toggle(entry) })
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.isSelecting,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 14.dp + getBannerOverlayHeight())) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(27.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .tapClickable("ai_category_delete") {
                                    tapEvent("ai_cleanup_confirm")
                                    viewModel.deleteSelected()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.ai_delete_selected, state.selected.size, Formatter.formatShortFileSize(context, state.selectedBytes)),
                                color = Color.White,
                                style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Cell(entry: FileEntry, kind: AiKind, selected: Boolean, selecting: Boolean, onToggle: () -> Unit) {
    val context = LocalContext.current
    Column {
        AiEntryThumb(
            entry = entry,
            kind = kind,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = { tapEvent("ai_category_open"); if (selecting) onToggle() else openEntry(context, entry) },
                    onLongClick = onToggle
                )
        )
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    Formatter.formatShortFileSize(context, entry.sizeBytes),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 12.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
                if (!entry.isMedia) {
                    Text(
                        entry.name,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        color = MaterialTheme.colorScheme.surface,
                        style = TextStyle(fontSize = 10.sp, fontFamily = medium)
                    )
                }
            }
            Box(Modifier.size(32.dp).tapPlainClickable("ai_category_select", onClick = onToggle), contentAlignment = Alignment.Center) {
                RoundCheck(selected, size = 22.dp)
            }
        }
    }
}
