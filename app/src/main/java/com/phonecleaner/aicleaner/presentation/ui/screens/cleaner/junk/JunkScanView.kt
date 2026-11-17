package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.junk

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.domain.JunkUiState
import com.phonecleaner.aicleaner.domain.JunkViewModel
import com.phonecleaner.aicleaner.domain.model.JunkGroup
import com.phonecleaner.aicleaner.domain.model.JunkItem
import com.phonecleaner.aicleaner.domain.model.ScanProgress
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppCheckbox
import com.phonecleaner.aicleaner.presentation.ui.theme.OnboardingPalette

private val bold = FontFamily(Font(R.font.google_sans_flex_bold))
private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))

private val Ink = Color(0xFF250B3B)
private val PeachTop = Color(0xFFFFE0D2)
private val PinkTop = Color(0xFFF9D4EA)
private val PurpleTop = Color(0xFFB985F0)
private const val MAX_LISTED = 50

private fun groupIcon(group: JunkGroup): Pair<ImageVector, Color> = when (group) {
    JunkGroup.RESIDUALS -> Icons.Rounded.Description to Color(0xFFB5179E)
    JunkGroup.OBSOLETE -> Icons.Rounded.FolderDelete to Color(0xFF6F3FD1)
    JunkGroup.AD_JUNK -> Icons.Rounded.CleaningServices to Color(0xFF3D7BFF)
    JunkGroup.TRASH -> Icons.Rounded.Delete to Color(0xFFC62828)
}

/** "154.5" + "MB": one decimal, unit shown smaller next to the number. */
private fun splitSize(bytes: Long): Pair<String, String> {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1000 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return (if (unit == 0) "%.0f".format(value) else "%.1f".format(value)) to units[unit]
}

/**
 * The scan screen and its result list in one layout: a tinted header whose colour follows the scan
 * (peach, then pink, then purple when finished) with the running total and percentage, and a rounded
 * sheet listing the four junk groups.
 */
@Composable
fun JunkScanView(
    state: JunkUiState,
    viewModel: JunkViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cleaning = state as? JunkUiState.Cleaning
    val scanning = state is JunkUiState.Scanning || cleaning != null
    val result = state as? JunkUiState.Result
    val progress = (state as? JunkUiState.Scanning)?.progress
    val cleanTimed = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(cleaning != null) {
        if (cleaning != null) cleanTimed.animateTo(1f, tween(2700, easing = androidx.compose.animation.core.LinearEasing))
    }
    val cleanFraction = cleanTimed.value

    val timed = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(scanning, cleaning == null) {
        if (scanning && cleaning == null) timed.animateTo(0.99f, tween(2800, easing = androidx.compose.animation.core.LinearEasing))
    }
    val fraction by animateFloatAsState(
        if (cleaning != null) cleanFraction else if (scanning) timed.value else 1f,
        tween(300), label = "junkFraction"
    )
    val headerColor by animateColorAsState(
        when {
            !scanning -> PurpleTop
            fraction < 0.45f -> PeachTop
            fraction < 0.98f -> PinkTop
            else -> PurpleTop
        },
        tween(600), label = "junkHeader"
    )
    val onHeader = if (scanning) Ink else Color.White
    val totalBytes = when {
        cleaning != null -> (cleaning.totalBytes * (1f - cleanFraction)).toLong()
        scanning -> progress?.foundBytes ?: 0L
        else -> result?.totalBytes ?: 0L
    }
    val (number, unit) = splitSize(totalBytes)

    var confirm by rememberSaveable { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(setOf<JunkGroup>()) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(Brush.verticalGradient(listOf(headerColor, headerColor.copy(alpha = 0.78f))))
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                run {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).clickable(enabled = !scanning, onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!scanning) Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = onHeader, modifier = Modifier.size(24.dp))
                    }
                }
                Text(
                    stringResource(R.string.junk_header_title),
                    color = onHeader,
                    style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        number,
                        color = onHeader,
                        style = TextStyle(fontSize = 64.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        unit,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
                        color = onHeader,
                        style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                    )
                }
                Text(
                    "${(fraction * 100).toInt()}%",
                    color = if (scanning) OnboardingPalette.purple else Color.White,
                    style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(10.dp))
                if (scanning) {
                    Text(
                        if (cleaning != null) stringResource(R.string.junk_cleaning_label) else stringResource(R.string.junk_scanning_file, progress?.currentPath?.substringAfterLast('/').orEmpty()),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Ink.copy(alpha = 0.65f),
                        style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33250B3B))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            stringResource(R.string.junk_no_personal_data),
                            modifier = Modifier.padding(start = 10.dp),
                            color = Color.White,
                            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    JunkGroup.entries.forEach { group ->
                        val items = result?.groups?.get(group).orEmpty()
                        val groupBytes = if (cleaning != null) {
                            ((cleaning.groupBytes[group] ?: 0L) * (1f - cleanFraction)).toLong()
                        } else if (scanning) {
                            group.categories.sumOf { progress?.categoryBytes?.get(it) ?: 0L }
                        } else {
                            items.sumOf { it.sizeBytes }
                        }
                        val isOpen = group in expanded
                        item(key = group.name) {
                            GroupRow(
                                group = group,
                                bytes = groupBytes,
                                scanning = scanning,
                                expandable = !scanning && items.isNotEmpty(),
                                open = isOpen,
                                checked = !scanning && items.isNotEmpty() && items.all { it.path in result!!.selected },
                                onToggleOpen = { expanded = if (isOpen) expanded - group else expanded + group },
                                onToggleChecked = { viewModel.toggleGroup(group) }
                            )
                        }
                        if (!scanning && isOpen) {
                            val shown = items.take(MAX_LISTED)
                            items(shown.size, key = { "${group.name}_${shown[it].path}" }) { index ->
                                val item = shown[index]
                                ItemRow(item, item.path in result!!.selected) { viewModel.toggleItem(item) }
                            }
                            if (items.size > MAX_LISTED) {
                                item(key = "${group.name}_more") {
                                    Text(
                                        "+${items.size - MAX_LISTED}",
                                        modifier = Modifier.padding(start = 36.dp, bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        style = TextStyle(fontSize = 12.sp, fontFamily = medium)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!scanning && result != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 16.dp + getBannerOverlayHeight())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    if (result.selected.isEmpty()) Brush.horizontalGradient(listOf(OnboardingPalette.dot, OnboardingPalette.dot))
                                    else Brush.horizontalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple))
                                )
                                .clickable {
                                    if (result.selected.isNotEmpty()) {
                                        context.postAnalytic("junk_clean_confirm")
                                        viewModel.cleanSelected()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.junk_clean_button, Formatter.formatFileSize(context, result.selectedBytes)),
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

@Composable
private fun GroupRow(
    group: JunkGroup,
    bytes: Long,
    scanning: Boolean,
    expandable: Boolean,
    open: Boolean,
    checked: Boolean,
    onToggleOpen: () -> Unit,
    onToggleChecked: () -> Unit
) {
    val context = LocalContext.current
    val (icon, tint) = groupIcon(group)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = expandable, onClick = onToggleOpen)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
        Text(
            stringResource(group.label),
            modifier = Modifier.padding(start = 14.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
        )
        if (expandable) {
            Icon(
                if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 4.dp).size(22.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            Formatter.formatShortFileSize(context, bytes),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Box(Modifier.padding(start = 6.dp).size(40.dp), contentAlignment = Alignment.Center) {
            if (scanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                )
            } else {
                AppCheckbox(checked = checked, onCheckedChange = { onToggleChecked() })
            }
        }
    }
}

@Composable
private fun ItemRow(item: JunkItem, checked: Boolean, onToggle: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 13.sp, fontFamily = medium)
            )
            Text(
                item.path.substringBeforeLast('/'),
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 10.sp, fontFamily = medium)
            )
        }
        Text(
            if (item.isDirectory) stringResource(R.string.junk_empty_folder_label)
            else Formatter.formatShortFileSize(context, item.sizeBytes),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
        AppCheckbox(checked = checked, onCheckedChange = { onToggle() })
    }
}
