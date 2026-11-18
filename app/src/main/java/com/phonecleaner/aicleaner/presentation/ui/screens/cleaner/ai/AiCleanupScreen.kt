package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.ai

import android.os.Environment
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tapClickable
import com.phonecleaner.aicleaner.data.ai.AiGroup
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.data.ai.AiPhase
import com.phonecleaner.aicleaner.data.ai.AiResultStore
import com.phonecleaner.aicleaner.domain.AiCleanupViewModel
import com.phonecleaner.aicleaner.domain.AiUiState
import com.phonecleaner.aicleaner.domain.model.FileEntry
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.GradientButton
import com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import com.phonecleaner.aicleaner.presentation.ui.components.ScanLoadingView
import com.phonecleaner.aicleaner.presentation.ui.components.StopProcessSheet
import com.phonecleaner.aicleaner.presentation.ui.components.FolderAccessPrompt
import com.phonecleaner.aicleaner.presentation.ui.screens.files.openEntry
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCleanupScreen(
    onBack: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onOpenCategory: (AiKind) -> Unit,
    onFinished: (String) -> Unit = {},
    viewModel: AiCleanupViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val groups by AiResultStore.groups.collectAsState()
    var granted by remember { mutableStateOf(FolderAccess.mediaRoots(context).isNotEmpty()) }
    var revealed by remember { mutableStateOf(false) }
    var stopSheet by remember { mutableStateOf(false) }
    val scanAnim = remember { Animatable(0f) }


    LaunchedEffect(granted, state is AiUiState.Idle) {
        if (granted && state is AiUiState.Idle) {
            context.postAnalytic("ai_cleanup_start")
            viewModel.start()
        }
    }
    LaunchedEffect(state) {
        if (state is AiUiState.Idle) {
            revealed = false
            scanAnim.snapTo(0f)
        }
    }

    val analyzing = state is AiUiState.Analyzing
    BackHandler(enabled = analyzing) { stopSheet = true }
    LaunchedEffect(analyzing) { if (!analyzing) stopSheet = false }
    if (stopSheet) {
        StopProcessSheet(
            onCancel = { stopSheet = false; viewModel.reset(); onBack() },
            onDismiss = { stopSheet = false }
        )
    }

    val loading = analyzing || (state is AiUiState.Result && (!revealed || groups.isEmpty()))
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { if (!loading) AppTopBar(title = { AppTopBarTitle(stringResource(R.string.feature_ai_cleanup)) }, onBack = onBack) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(if (loading) PaddingValues(0.dp) else padding)) {
            when (val current = state) {
                AiUiState.Idle -> if (!granted) {
                    FolderAccessPrompt(onDismiss = onBack, onGranted = { granted = true }, accent = Brand.Ai)
                }

                is AiUiState.Analyzing -> ScanLoadingView(
                    progress = if (current.total > 0) current.done.toFloat() / current.total else null,
                    finished = false,
                    label = stringResource(if (current.phase == AiPhase.COLLECTING) R.string.ai_collecting else R.string.ai_analyzing),
                    p = scanAnim
                )

                AiUiState.Result -> when {
                    !revealed -> ScanLoadingView(null, true, stringResource(R.string.ai_analyzing), scanAnim) { revealed = true }
                    groups.isEmpty() -> com.phonecleaner.aicleaner.presentation.ui.components.TaskDoneCheck(stringResource(R.string.congrats_text)) { viewModel.reset(); onFinished("") }
                    else -> ResultView(groups, onOpenCategory, onOpenDuplicates)
                }

            }
        }
    }
}

@Composable
private fun ResultView(groups: List<AiGroup>, onOpenCategory: (AiKind) -> Unit, onOpenDuplicates: () -> Unit) {
    val context = LocalContext.current
    val totalStorage = remember { android.os.StatFs(Environment.getDataDirectory().path).totalBytes }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp + getBannerOverlayHeight()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Header(Formatter.formatFileSize(context, groups.sumOf { it.bytes }), Formatter.formatFileSize(context, totalStorage))
        }
        items(groups, key = { it.kind.name }) { group ->
            KindSection(group, onViewAll = { onOpenCategory(group.kind) })
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .tapClickable("ai_open_duplicates", onClick = onOpenDuplicates)
                    .padding(16.dp),
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
}

@Composable
private fun Header(freeable: String, total: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.files_mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 150.dp, height = 136.dp)
        )
        Spacer(Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Stat(stringResource(R.string.ai_stat_freeable), freeable, Brand.Gold)
            Stat(stringResource(R.string.ai_stat_total), total, MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
private fun Stat(label: String, value: String, dot: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.size(8.dp))
            Text(label, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 14.sp, fontFamily = medium))
        }
        Text(
            value,
            modifier = Modifier.padding(start = 16.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun KindSection(group: AiGroup, onViewAll: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(group.kind.icon(), null, tint = group.kind.tint(), modifier = Modifier.size(26.dp))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(group.kind.titleRes()), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                Text(Formatter.formatShortFileSize(context, group.bytes), color = Brand.Danger, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .tapClickable("ai_view_all_${group.kind.name.lowercase()}", onClick = onViewAll)
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.ai_view_all), color = Color.White, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(group.files.take(PREVIEW_COUNT), key = { it.path }) { entry -> PreviewTile(entry, group.kind) }
        }
    }
}

@Composable
private fun PreviewTile(entry: FileEntry, kind: AiKind) {
    val context = LocalContext.current
    AiEntryThumb(
        entry = entry,
        kind = kind,
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .tapClickable("ai_open_preview") { openEntry(context, entry) }
    )
}


private const val PREVIEW_COUNT = 8
