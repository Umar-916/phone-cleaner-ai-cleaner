package com.phonecleaner.aicleaner.presentation.ui.screens.files

import com.phonecleaner.aicleaner.presentation.ui.components.OnResumeEffect
import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapEvent
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.phonecleaner.aicleaner.presentation.ui.theme.softCardColor
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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ArrowForward
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
import com.phonecleaner.aicleaner.data.storage.FolderAccess
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.FilesViewModel
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.navigation.getTotalBottomHeight
import com.phonecleaner.aicleaner.presentation.ui.components.FolderAccessPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

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

@Composable
fun FilesScreen(
    onOpenCategory: (FileCategory) -> Unit,
    onSearch: () -> Unit,
    onBrowse: () -> Unit,
    onSettings: () -> Unit = {},
    viewModel: FilesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var granted by remember { mutableStateOf(FolderAccess.mediaRoots(context).isNotEmpty()) }
    OnResumeEffect { granted = FolderAccess.mediaRoots(context).isNotEmpty() }
    var accessTick by remember { mutableStateOf(0) }
    var promptVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(granted, accessTick) {
        viewModel.refresh(withCategories = true)
        if (granted) promptVisible = false
    }
    LaunchedEffect(Unit) { context.postAnalytic("files_tab_display") }

    val guarded: (() -> Unit) -> () -> Unit = { action ->
        { if (granted) action() else promptVisible = true }
    }

    val darkPage = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    0f to (if (darkPage) Color(0xFF2A1647) else Color(0xFFEFDEFF)),
                    0.45f to Color.Transparent
                )
            )
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.nav_files),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Box(Modifier.size(40.dp).clip(CircleShape).tapClickable("files_screen_click", onClick = onSettings), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.78f))
                .tapClickable("files_screen_click_2", onClick = guarded(onSearch))
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
                    CategoryCard(
                        category = category,
                        subtitle = if (granted && summary != null) Formatter.formatShortFileSize(context, summary.bytes) else "—",
                        modifier = Modifier.weight(1f),
                        onClick = { tapEvent("files_screen_tap_2"); if (granted) onOpenCategory(category) else promptVisible = true }
                    )
                }
            }
        }
        Spacer(Modifier.height(getTotalBottomHeight() + getBannerOverlayHeight() + 8.dp))
    }

    if (promptVisible && !granted) {
        FolderAccessPrompt(
            onDismiss = { promptVisible = false },
            onGranted = { granted = true; promptVisible = false; accessTick++ },
            accent = Brand.Blue
        )
    }
}

@Composable
private fun StorageCard(fraction: Float, used: Long, total: Long, onClick: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brand.Hero)
            .tapClickable("storage_card_click", onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.files_internal_storage),
                    color = Color.White.copy(alpha = 0.9f),
                    style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        Formatter.formatFileSize(context, used),
                        color = Color.White,
                        style = TextStyle(fontSize = 26.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "  |  " + Formatter.formatFileSize(context, total),
                        modifier = Modifier.padding(bottom = 3.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                    )
                }
                Spacer(Modifier.height(14.dp))
                AppProgressBar(
                    fraction = fraction,
                    height = 7.dp,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.White, Color(0xFFD9C2FF)))
                )
            }
            Spacer(Modifier.size(8.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(R.drawable.files_mascot),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(width = 104.dp, height = 94.dp)
                )
                Box(
                    modifier = Modifier.size(26.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: FileCategory, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val tint = category.tint()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(softCardColor())
            .border(0.6.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .tapClickable("category_card_click", onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(tint.copy(alpha = 0.75f), tint))),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon(), null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(category.label),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        Text(subtitle, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
    }
}
