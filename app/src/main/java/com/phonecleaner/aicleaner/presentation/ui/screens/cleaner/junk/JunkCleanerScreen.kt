package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.junk

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.presentation.ui.components.MediaPermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.StoragePermission
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.JunkUiState
import com.phonecleaner.aicleaner.domain.JunkViewModel
import com.phonecleaner.aicleaner.domain.model.JunkCategory
import com.phonecleaner.aicleaner.domain.model.JunkItem
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun JunkCleanerScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: JunkViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var permissionGranted by remember { mutableStateOf(StoragePermission.isGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionGranted = StoragePermission.isGranted(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val legacyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = StoragePermission.isGranted(context)
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        permissionGranted = StoragePermission.isGranted(context)
    }

    LaunchedEffect(permissionGranted, state is JunkUiState.Idle) {
        if (permissionGranted && state is JunkUiState.Idle) {
            context.postAnalytic("junk_scan_start")
            viewModel.startScan()
        }
    }

    val busy = state is JunkUiState.Scanning || state is JunkUiState.Cleaning
    BackHandler(enabled = busy) {}

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val showsScan = state is JunkUiState.Scanning || state is JunkUiState.Cleaning || state is JunkUiState.Done || (state is JunkUiState.Result && (state as JunkUiState.Result).items.isNotEmpty())
            if (!showsScan) {
                AppTopBar(
                    title = { AppTopBarTitle(stringResource(R.string.feature_junk_cleaner)) },
                    onBack = if (busy) null else onBack
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                JunkUiState.Idle -> if (!permissionGranted) {
                    StoragePermissionPrompt(onDismiss = onBack, accent = Brand.Junk) {
                        if (StoragePermission.usesSettingsScreen) {
                            settingsLauncher.launch(StoragePermission.settingsIntent(context))
                        } else {
                            legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
                        }
                    }
                }

                is JunkUiState.Scanning -> JunkScanView(current, viewModel, onBack)
                is JunkUiState.Result -> if (current.items.isEmpty()) {
                    EmptyView(onRescan = viewModel::reset)
                } else {
                    JunkScanView(current, viewModel, onBack)
                }

                is JunkUiState.Cleaning -> JunkScanView(current, viewModel, onBack)
                is JunkUiState.Done -> {
                    val summary = stringResource(R.string.junk_done_body, Formatter.formatFileSize(context, current.freedBytes), current.removedCount)
                    DoneView {
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
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun ResultView(state: JunkUiState.Result, viewModel: JunkViewModel) {
    val context = LocalContext.current
    var confirm by rememberSaveable { mutableStateOf(false) }
    val allSelected = state.selected.size == state.items.size
    val expanded = remember { mutableStateOf(setOf<JunkCategory>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.junk_total_found, Formatter.formatFileSize(context, state.totalBytes)),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = { viewModel.setAllSelected(!allSelected) }) {
                Text(
                    stringResource(if (allSelected) R.string.junk_deselect_all else R.string.junk_select_all),
                    color = MaterialTheme.colorScheme.primary,
                    style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.grouped.forEach { (category, items) ->
                val isOpen = category in expanded.value
                item(key = category.name) {
                    CategoryCard(
                        category = category,
                        items = items,
                        selected = state.selected,
                        expanded = isOpen,
                        onToggleExpand = {
                            expanded.value = if (isOpen) expanded.value - category else expanded.value + category
                        },
                        onToggleCategory = { viewModel.toggleCategory(category) },
                        onToggleItem = viewModel::toggleItem
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())) {
            PrimaryButton(
                text = stringResource(R.string.junk_clean_selected, Formatter.formatFileSize(context, state.selectedBytes)),
                onClick = { if (state.selected.isNotEmpty()) confirm = true }
            )
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.junk_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.junk_confirm_body,
                        state.selected.size,
                        Formatter.formatFileSize(context, state.selectedBytes)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    context.postAnalytic("junk_clean_confirm")
                    viewModel.cleanSelected()
                }) { Text(stringResource(R.string.junk_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CategoryCard(
    category: JunkCategory,
    items: List<JunkItem>,
    selected: Set<String>,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleCategory: () -> Unit,
    onToggleItem: (JunkItem) -> Unit
) {
    val context = LocalContext.current
    val selectedCount = items.count { it.path in selected }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckbox(checked = selectedCount == items.size, onCheckedChange = { onToggleCategory() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(category.label),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "${items.size} · " + Formatter.formatFileSize(context, items.sumOf { it.sizeBytes }),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                )
            }
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.surface
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                items.take(MAX_LISTED).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onToggleItem(item) }.padding(start = 22.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCheckbox(checked = item.path in selected, onCheckedChange = { onToggleItem(item) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                            )
                            Text(
                                item.path.substringBeforeLast('/'),
                                maxLines = 1,
                                overflow = TextOverflow.MiddleEllipsis,
                                color = MaterialTheme.colorScheme.surface,
                                style = TextStyle(fontSize = 11.sp, fontFamily = medium)
                            )
                        }
                        Text(
                            if (item.isDirectory) stringResource(R.string.junk_empty_folder_label)
                            else Formatter.formatFileSize(context, item.sizeBytes),
                            color = MaterialTheme.colorScheme.surface,
                            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                        )
                    }
                }
                if (items.size > MAX_LISTED) {
                    Text(
                        "+${items.size - MAX_LISTED}",
                        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun DoneView(onDone: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        onDone()
    }
    var shown by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { shown = true }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (shown) 1f else 0.3f,
        androidx.compose.animation.core.spring(androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, androidx.compose.animation.core.Spring.StiffnessLow),
        label = "doneScale"
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(150.dp).scale(scale).clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xFF8BE0A6), Color(0xFF55C97E)))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(84.dp)) }
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.task_completed),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun EmptyView(onRescan: () -> Unit) {
    CenteredColumn {
        Icon(Icons.Rounded.FolderOff, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.junk_empty_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.junk_empty_body),
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(stringResource(R.string.junk_rescan), onRescan)
    }
}

private const val MAX_LISTED = 50
