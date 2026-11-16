package com.phonecleaner.aicleaner.presentation.ui.screens.files

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.AppProgressBar
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import com.phonecleaner.aicleaner.core.common.MediaAccess
import com.phonecleaner.aicleaner.core.common.StoragePermission
import com.phonecleaner.aicleaner.presentation.ui.components.MediaPermissionPrompt
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.FilesViewModel
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.navigation.getTotalBottomHeight
import com.phonecleaner.aicleaner.presentation.ui.components.StoragePermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

fun FileCategory.icon(): ImageVector = when (this) {
    FileCategory.IMAGES -> Icons.Rounded.Image
    FileCategory.VIDEO -> Icons.Rounded.Videocam
    FileCategory.AUDIO -> Icons.Rounded.MusicNote
    FileCategory.DOCUMENTS -> Icons.Rounded.Description
    FileCategory.APK -> Icons.Rounded.Android
    FileCategory.DOWNLOAD -> Icons.Rounded.Download
}

fun FileCategory.tint(): Color = when (this) {
    FileCategory.IMAGES -> Color(0xFFD946EF)
    FileCategory.VIDEO -> Brand.Blue
    FileCategory.AUDIO -> Brand.Danger
    FileCategory.DOCUMENTS -> Color(0xFF2563EB)
    FileCategory.APK -> Brand.Good
    FileCategory.DOWNLOAD -> Brand.Photo
}

/** The Files tab: storage overview, search entry and one card per file category. */
@Composable
fun FilesScreen(
    onOpenCategory: (FileCategory) -> Unit,
    onSearch: () -> Unit,
    onBrowse: () -> Unit,
    viewModel: FilesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(StoragePermission.isGranted(context)) }
    var accessTick by remember { mutableStateOf(0) }
    var pendingCategory by remember { mutableStateOf<FileCategory?>(null) }
    var promptVisible by rememberSaveable { mutableStateOf(false) }
    var mediaPromptFor by remember { mutableStateOf<FileCategory?>(null) }
    var mediaDeniedOnce by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = StoragePermission.isGranted(context)
                accessTick++
            }
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
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        accessTick++
        val target = mediaPromptFor
        if (result.values.all { it } && target != null) {
            mediaPromptFor = null
            onOpenCategory(target)
        } else {
            mediaDeniedOnce = true
        }
    }
    val requestAccess = {
        if (StoragePermission.usesSettingsScreen) settingsLauncher.launch(StoragePermission.settingsIntent(context))
        else legacyLauncher.launch(StoragePermission.LEGACY_PERMISSION)
    }

    LaunchedEffect(granted, accessTick) {
        viewModel.refresh(withCategories = true)
        if (granted) promptVisible = false
    }
    LaunchedEffect(Unit) { context.postAnalytic("files_tab_display") }

    val guarded: (() -> Unit) -> () -> Unit = { action ->
        { if (granted) action() else promptVisible = true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            stringResource(R.string.nav_files),
            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 28.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .clickable(onClick = guarded(onSearch))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.surface)
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.files_search_hint), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 15.sp, fontFamily = medium))
        }

        Spacer(Modifier.height(16.dp))
        StorageCard(state.storage.usedFraction, state.storage.usedBytes, state.storage.totalBytes, guarded(onBrowse))
        Spacer(Modifier.height(16.dp))

        FileCategory.entries.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    val summary = state.summaries[category]
                    val allowed = MediaAccess.isGranted(context, category)
                    CategoryCard(
                        category = category,
                        subtitle = if (allowed && summary != null) Formatter.formatShortFileSize(context, summary.bytes) else "—",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when {
                                allowed -> onOpenCategory(category)
                                MediaAccess.permissionsFor(category) != null -> mediaPromptFor = category
                                else -> promptVisible = true
                            }
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(getTotalBottomHeight() + getBannerOverlayHeight() + 8.dp))
    }

    if (promptVisible && !granted) {
        StoragePermissionPrompt(
            onDismiss = {
                promptVisible = false
            },
            accent = Brand.Blue,
            onAllow = requestAccess
        )
    }

    mediaPromptFor?.let { target ->
        MediaPermissionPrompt(
            showSettings = mediaDeniedOnce,
            onDismiss = { mediaPromptFor = null },
            onAllow = {
                if (mediaDeniedOnce) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                    runCatching { context.startActivity(intent) }
                    mediaPromptFor = null
                } else {
                    mediaLauncher.launch(MediaAccess.permissionsFor(target) ?: emptyArray())
                }
            }
        )
    }
}

@Composable
private fun StorageCard(fraction: Float, used: Long, total: Long, onClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brand.Hero)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Text(
            stringResource(R.string.files_internal_storage),
            color = Color.White.copy(alpha = 0.9f),
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                Formatter.formatFileSize(context, used),
                color = Color.White,
                style = TextStyle(fontSize = 30.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Text(
                "  |  " + Formatter.formatFileSize(context, total),
                modifier = Modifier.padding(bottom = 4.dp),
                color = Color.White.copy(alpha = 0.8f),
                style = TextStyle(fontSize = 14.sp, fontFamily = medium)
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        AppProgressBar(
            fraction = fraction,
            height = 8.dp,
            trackColor = Color.White.copy(alpha = 0.25f),
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.White, Color(0xFFBFE3FF)))
        )
    }
}

@Composable
private fun CategoryCard(category: FileCategory, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val tint = category.tint()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon(), null, tint = tint, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(category.label),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        Text(subtitle, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, fontFamily = medium))
    }
}
