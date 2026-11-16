package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.photo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.presentation.ui.components.AppSwitch
import com.phonecleaner.aicleaner.presentation.ui.components.AppSlider
import com.phonecleaner.aicleaner.presentation.ui.components.FeatureProgress
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.domain.CompressPhase
import com.phonecleaner.aicleaner.domain.CompressorState
import com.phonecleaner.aicleaner.domain.PhotoCompressorViewModel
import com.phonecleaner.aicleaner.domain.model.CompressedFile
import com.phonecleaner.aicleaner.domain.model.OutputFormat
import com.phonecleaner.aicleaner.domain.model.PhotoInfo
import com.phonecleaner.aicleaner.domain.model.PhotoPreview
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val Good = Color(0xFF22B573)

private val thumbCache = LruCache<String, ImageBitmap>(60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompressorScreen(
    onBack: () -> Unit,
    onTaskCompleted: (String) -> Unit = {},
    viewModel: PhotoCompressorViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        viewModel.addPhotos(uris)
    }
    val pickImages = { pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    val writePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.compressAll()
    }
    val startCompress = {
        context.postAnalytic("photo_compress_start")
        val needsWritePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (needsWritePermission) writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) else viewModel.compressAll()
    }

    val processing = state.phase is CompressPhase.Processing
    BackHandler(enabled = processing) {}

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = { AppTopBarTitle(stringResource(R.string.feature_photo_compressor)) },
                onBack = if (processing) null else onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val phase = state.phase) {
                CompressPhase.Editing -> if (state.photos.isEmpty()) {
                    EmptyView(state.isLoadingPhotos, pickImages)
                } else {
                    EditorView(state, viewModel, pickImages, startCompress)
                }

                is CompressPhase.Processing -> FeatureProgress(
                    title = stringResource(R.string.pc_processing),
                    progress = if (phase.total == 0) 0f else phase.done.toFloat() / phase.total,
                    subtitle = "${phase.done} / ${phase.total}",
                    accent = Brand.Photo
                )
                is CompressPhase.Done -> {
                    val savedBytes = (phase.files.sumOf { it.originalBytes } - phase.files.sumOf { it.newBytes }).coerceAtLeast(0L)
                    val summary = stringResource(R.string.pc_done_summary, Formatter.formatFileSize(context, savedBytes), phase.files.size)
                    DoneView(phase, onAgain = viewModel::reset, onDone = {
                        viewModel.reset()
                        onTaskCompleted(summary)
                    })
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Color(0xFF38A3FF), MaterialTheme.colorScheme.primary))
                else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun EmptyView(loading: Boolean, onPick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(110.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            else Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.pc_empty_title),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.pc_empty_body),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontFamily = medium)
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(stringResource(R.string.pc_select_photos), onPick)
    }
}

@Composable
private fun EditorView(state: CompressorState, viewModel: PhotoCompressorViewModel, onAdd: () -> Unit, onCompress: () -> Unit) {
    val settings = state.settings
    val photo = state.selected

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.photos, key = { _, p -> p.uri.toString() }) { index, item ->
                    Thumb(item, selected = index == state.selectedIndex, viewModel = viewModel,
                        onClick = { viewModel.select(index) }, onRemove = { viewModel.removePhoto(index) })
                }
                item {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable(onClick = onAdd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (photo != null) {
                ComparisonRow(photo, state.preview)
            }

            Section(stringResource(R.string.pc_format)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutputFormat.entries.forEach { format ->
                        OptionChip(settings.format == format, format.label) { viewModel.setFormat(format) }
                    }
                }
            }

            Section(stringResource(R.string.pc_quality)) {
                if (settings.usesQuality) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppSlider(
                            value = settings.quality.toFloat(),
                            onValueChange = { viewModel.setQuality(it.roundToInt()) },
                            valueRange = 10f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${settings.quality}%",
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.pc_png_lossless),
                        color = MaterialTheme.colorScheme.surface,
                        style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                    )
                }
            }

            Section(stringResource(R.string.pc_size)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionChip(!settings.custom, stringResource(R.string.pc_size_original)) { viewModel.setCustomSize(false) }
                    OptionChip(settings.custom, stringResource(R.string.pc_size_custom)) { viewModel.setCustomSize(true) }
                }
                if (settings.custom) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DimensionField(stringResource(R.string.pc_width), state.widthText, Modifier.weight(1f), viewModel::setWidthText)
                        DimensionField(stringResource(R.string.pc_height), state.heightText, Modifier.weight(1f), viewModel::setHeightText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.pc_keep_aspect),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                        )
                        AppSwitch(checked = settings.keepAspect, onCheckedChange = viewModel::setKeepAspect)
                    }
                    if (settings.keepAspect && state.photos.size > 1) {
                        Text(
                            stringResource(R.string.pc_batch_hint),
                            color = MaterialTheme.colorScheme.surface,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontFamily = medium)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp + getBannerOverlayHeight())
        ) {
            PrimaryButton(
                stringResource(R.string.pc_compress_action, state.photos.size),
                onCompress,
                enabled = !state.isLoadingPhotos
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            title,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        content()
    }
}

@Composable
private fun OptionChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
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
private fun DimensionField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        singleLine = true,
        label = { Text(label) },
        suffix = { Text("px") },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.tertiary,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.surface,
            focusedSuffixColor = MaterialTheme.colorScheme.surface,
            unfocusedSuffixColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun Thumb(photo: PhotoInfo, selected: Boolean, viewModel: PhotoCompressorViewModel, onClick: () -> Unit, onRemove: () -> Unit) {
    val key = photo.uri.toString()
    val bitmap by produceState(initialValue = thumbCache[key], key) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { viewModel.thumbnail(photo.uri)?.asImageBitmap() }?.also { thumbCache.put(key, it) }
        }
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .then(if (selected) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(14.dp)) else Modifier)
            .clickable(onClick = onClick)
    ) {
        bitmap?.let { Image(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun ComparisonRow(photo: PhotoInfo, preview: PhotoPreview) {
    val context = LocalContext.current
    val saved = if (photo.sizeBytes > 0 && preview.compressedBytes > 0) {
        ((1f - preview.compressedBytes.toFloat() / photo.sizeBytes) * 100).roundToInt()
    } else {
        null
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreviewCard(
            Modifier.weight(1f),
            stringResource(R.string.pc_original),
            preview.original,
            "${photo.width}×${photo.height}",
            Formatter.formatShortFileSize(context, photo.sizeBytes),
            loading = false
        )
        PreviewCard(
            Modifier.weight(1f),
            stringResource(R.string.pc_compressed),
            preview.compressed,
            if (preview.outWidth > 0) "${preview.outWidth}×${preview.outHeight}" else "…",
            if (preview.compressedBytes > 0) {
                "~" + Formatter.formatShortFileSize(context, preview.compressedBytes.toLong()) +
                    (saved?.let { if (it > 0) " (-$it%)" else " (+${-it}%)" }.orEmpty())
            } else {
                "…"
            },
            loading = preview.loading,
            highlight = saved != null && saved > 0
        )
    }
}

@Composable
private fun PreviewCard(
    modifier: Modifier,
    title: String,
    bitmap: android.graphics.Bitmap?,
    dimensions: String,
    size: String,
    loading: Boolean,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.secondary).padding(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { Image(it.asImageBitmap(), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()) }
            if (loading) CircularProgressIndicator(strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        Text(dimensions, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 12.sp, fontFamily = medium))
        Text(
            size,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (highlight) Good else MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun DoneView(phase: CompressPhase.Done, onAgain: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val originalTotal = phase.files.sumOf { it.originalBytes }
    val newTotal = phase.files.sumOf { it.newBytes }
    val savedBytes = (originalTotal - newTotal).coerceAtLeast(0L)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = Good, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.pc_done_title),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.pc_done_summary, Formatter.formatFileSize(context, savedBytes), phase.files.size),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 15.sp, fontFamily = medium)
        )
        Text(
            stringResource(R.string.pc_done_location),
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
        if (phase.failed > 0) {
            Text(
                stringResource(R.string.pc_done_failed, phase.failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp),
                style = TextStyle(fontSize = 13.sp, fontFamily = medium)
            )
        }
        Spacer(Modifier.height(20.dp))
        phase.files.forEach { file -> ResultRow(file) }
        Spacer(Modifier.height(20.dp))

        if (phase.files.isNotEmpty()) {
            PrimaryButton(stringResource(R.string.pc_share), onClick = { shareFiles(context, phase.files.map { it.uri }) })
            Spacer(Modifier.height(10.dp))
        }
        Text(
            stringResource(R.string.pc_compress_more),
            modifier = Modifier.clickable(onClick = onAgain).padding(12.dp),
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        PrimaryButton(stringResource(R.string.junk_done_action), onDone)
        Spacer(Modifier.height(getBannerOverlayHeight()))
    }
}

@Composable
private fun ResultRow(file: CompressedFile) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondary).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            file.name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Text(
            Formatter.formatShortFileSize(context, file.originalBytes) + " → " + Formatter.formatShortFileSize(context, file.newBytes),
            color = Good,
            style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
    }
}

private fun shareFiles(context: android.content.Context, uris: List<Uri>) {
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_STREAM, uris.first()) }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply { putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) }
    }.apply {
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    tryCatch { context.startActivity(Intent.createChooser(intent, null)) }
}
