package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.junk

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.presentation.ui.theme.HeatPeach
import com.phonecleaner.aicleaner.presentation.ui.theme.HeatRed
import com.phonecleaner.aicleaner.presentation.ui.theme.heatColor
import com.phonecleaner.aicleaner.core.common.tapPlainClickable
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Check
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
import com.phonecleaner.aicleaner.core.utils.AppBackButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.rotate as rotateDraw
import androidx.compose.animation.core.animateFloat
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

private val bold = AppFonts.Bold
private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium

private val Ink = Color(0xFF250B3B)
private val PeachTop = Color(0xFFFFE0D2)
private val PinkTop = Color(0xFFF9D4EA)
private val PurpleTop = Color(0xFFB985F0)
private const val MAX_LISTED = 1000

private fun groupIcon(group: JunkGroup): Pair<ImageVector, Color> = when (group) {
    JunkGroup.RESIDUALS -> Icons.Rounded.Description to Color(0xFFB5179E)
    JunkGroup.OBSOLETE -> Icons.Rounded.FolderDelete to Color(0xFF6F3FD1)
    JunkGroup.AD_JUNK -> Icons.Rounded.CleaningServices to Color(0xFF3D7BFF)
    JunkGroup.TRASH -> Icons.Rounded.Delete to Color(0xFFC62828)
}

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

@Composable
fun JunkScanView(
    state: JunkUiState,
    viewModel: JunkViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(state is JunkUiState.Done) {
        if (state is JunkUiState.Done) {
            kotlinx.coroutines.delay(1700)
            onDone()
        }
    }
    val cleaning = state as? JunkUiState.Cleaning
    val scanning = state is JunkUiState.Scanning || cleaning != null
    val result = state as? JunkUiState.Result
    val progress = (state as? JunkUiState.Scanning)?.progress
    val revealed = (state as? JunkUiState.Scanning)?.items.orEmpty()
    val cleanTimed = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(cleaning != null) {
        if (cleaning != null) cleanTimed.animateTo(1f, tween(6200, easing = androidx.compose.animation.core.LinearEasing))
    }
    val cleanFraction = cleanTimed.value

    val timed = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(scanning, cleaning == null) {
        if (scanning && cleaning == null) timed.animateTo(0.99f, tween(6200, easing = androidx.compose.animation.core.LinearEasing))
    }
    val scanFraction = if (revealed.isNotEmpty() && progress != null) {
        0.35f + 0.6f * (progress.fraction / 0.95f).coerceIn(0f, 1f)
    } else {
        minOf(timed.value, 0.35f)
    }
    val fraction by animateFloatAsState(
        if (cleaning != null) 1f - cleanFraction else if (scanning) scanFraction else 1f,
        tween(300), label = "junkFraction"
    )
    val isDone = state is JunkUiState.Done
    val totalBytes = when {
        isDone -> 0L
        cleaning != null -> (cleaning.totalBytes * (1f - cleanFraction)).toLong()
        scanning -> progress?.foundBytes ?: 0L
        else -> result?.totalBytes ?: 0L
    }
    val heat = when {
        isDone -> 0f
        cleaning != null -> 1f - cleanFraction
        scanning -> fraction.coerceIn(0f, 1f)
        else -> 1f
    }
    val shade = if (cleaning != null || isDone) heat else Math.pow(heat.toDouble(), 2.6).toFloat()
    val headerColor by animateColorAsState(if (cleaning != null || isDone) cleanColor(shade) else heatColor(shade), tween(500, easing = androidx.compose.animation.core.LinearEasing), label = "junkHeader")
    val onHeader = lerp(Ink, Color.White, ((shade - 0.7f) / 0.3f).coerceIn(0f, 1f))
    val checkColor by animateColorAsState(lerp(OnboardingPalette.purple, Color(0xFFE23B3B), ((shade - 0.7f) / 0.3f).coerceIn(0f, 1f)), tween(500), label = "checkColor")
    val (number, unit) = splitSize(totalBytes)

    var confirm by rememberSaveable { mutableStateOf(false) }
    var expanded by remember(result?.items?.size) {
        mutableStateOf(result?.groups?.filter { it.value.isNotEmpty() }?.keys ?: emptySet<JunkGroup>())
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalCheckColor provides checkColor) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(Brush.verticalGradient(listOf(headerColor, headerColor.copy(alpha = 0.82f))))
        ) {
            if (scanning && cleaning == null) RadarSweep()
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                run {
                    if (!scanning) AppBackButton(onClick = onBack) else Spacer(Modifier.size(44.dp))
                }
                Text(
                    stringResource(R.string.junk_header_title),
                    modifier = Modifier.padding(start = 8.3.dp),
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
                    color = onHeader,
                    style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(10.dp))
                if (scanning) {
                    val stageRes = listOf(R.string.junk_stage_1, R.string.junk_stage_2, R.string.junk_stage_3, R.string.junk_stage_4, R.string.junk_stage_5)
                    androidx.compose.animation.Crossfade(
                        targetState = if (cleaning != null) -1 else (fraction * stageRes.size).toInt().coerceIn(0, stageRes.lastIndex),
                        label = "junkStage"
                    ) { stage ->
                        Text(
                            if (stage < 0) stringResource(R.string.junk_cleaning_label) else stringResource(stageRes[stage]) + "…",
                            modifier = Modifier.padding(horizontal = 32.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = onHeader.copy(alpha = 0.8f),
                            style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                        )
                    }
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
                    if (cleaning != null) {
                        val ordered = JunkGroup.entries.reversed().flatMap { g -> cleaning.targets.filter { it.category in g.categories }.reversed() }
                        val indexOf = ordered.withIndex().associate { it.value.path to it.index }
                        val position = ordered.size * cleanFraction
                        JunkGroup.entries.forEach { group ->
                            val rem = cleaning.targets.filter { it.category in group.categories && (indexOf[it.path] ?: 0) + 1 > position }
                            if (rem.isNotEmpty()) {
                                item(key = group.name) {
                                    Box(Modifier.animateItem()) {
                                        GroupRow(group, rem.sumOf { it.sizeBytes }, false, false, false, true, true, {}, {})
                                    }
                                }
                                val shownRem = rem.take(200)
                                items(shownRem.size, key = { "c_" + shownRem[it].path }) { idx ->
                                    val p = (position - (indexOf[shownRem[idx].path] ?: 0)).coerceIn(0f, 1f)
                                    Box(
                                        Modifier.animateItem().graphicsLayer {
                                            translationX = -p * size.width * 0.45f
                                            alpha = 1f - p
                                        }
                                    ) { ItemRow(shownRem[idx], true) {} }
                                }
                            }
                        }
                    } else JunkGroup.entries.forEachIndexed { groupIndex, group ->
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
                            Box(Modifier.animateItem()) { GroupRow(
                                group = group,
                                bytes = groupBytes,
                                scanning = scanning,
                                stepDone = scanning && fraction >= (groupIndex + 1) * 0.23f,
                                expandable = !scanning && items.isNotEmpty(),
                                open = isOpen,
                                checked = !scanning && (items.isEmpty() || items.all { it.path in result!!.selected }),
                                onToggleOpen = { expanded = if (isOpen) expanded - group else expanded + group },
                                onToggleChecked = { viewModel.toggleGroup(group) }
                            ) }
                        }
                        if (scanning) {
                            val shownNow = revealed.filter { it.category in group.categories }
                            items(shownNow.size, key = { "r_${group.name}_${shownNow[it].path}" }) { idx ->
                                Box(Modifier.animateItem()) { RevealRow(shownNow[idx]) }
                            }
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

                if (isDone) {
                    Box(Modifier.fillMaxWidth().padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 22.dp + getBannerOverlayHeight())) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple)))
                                .tapClickable("junk_scan_view_click_2") { onDone() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.junk_done_action), color = Color.White, style = TextStyle(fontSize = 17.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                        }
                    }
                } else if (!scanning && result != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 22.dp + getBannerOverlayHeight())
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
                                .tapClickable("junk_scan_view_click_3") {
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
}

@Composable
private fun GroupRow(
    group: JunkGroup,
    bytes: Long,
    scanning: Boolean,
    stepDone: Boolean,
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
            .tapClickable("group_row_click", enabled = expandable, onClick = onToggleOpen)
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
            if (scanning && stepDone) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF55C97E)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else if (scanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                )
            } else {
                RoundCheck(checked, onToggleChecked)
            }
        }
    }
}

@Composable
private fun RevealRow(item: JunkItem) {
    val enter = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) { enter.animateTo(1f, tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
    Box(
        Modifier.graphicsLayer {
            alpha = enter.value
            translationX = (1f - enter.value) * size.width * 0.3f
        }
    ) { ItemRow(item, true) {} }
}

@Composable
private fun ItemRow(item: JunkItem, checked: Boolean, onToggle: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().tapClickable("item_row_click", onClick = onToggle).padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
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
        }
        Text(
            if (item.isDirectory) stringResource(R.string.junk_empty_folder_label)
            else Formatter.formatShortFileSize(context, item.sizeBytes),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 12.sp, fontFamily = medium)
        )
        RoundCheck(checked, onToggle)
    }
}

private val JunkStart = Color(0xFFB28BE0)
private val JunkGreen = Color(0xFF86C98B)
private val JunkPurple = Color(0xFFB892DC)
private val JunkRed = Color(0xFFE59A9A)

private fun junkColor(bytes: Long): Color {
    val mb = bytes / 1048576f
    return if (mb <= 60f) lerp(JunkGreen, JunkPurple, (mb / 60f).coerceIn(0f, 1f))
    else lerp(JunkPurple, JunkRed, ((mb - 60f) / 140f).coerceIn(0f, 1f))
}

@Composable
private fun RadarSweep() {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "radar")
    val angle by t.animateFloat(
        0f, 360f,
        androidx.compose.animation.core.infiniteRepeatable(tween(3200, easing = androidx.compose.animation.core.LinearEasing)),
        label = "radarAngle"
    )
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height)
        listOf(0.30f, 0.52f, 0.76f).forEach {
            drawCircle(Color(0xFF8D3EDD).copy(alpha = 0.22f), radius = size.width * it, center = c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx()))
        }
        rotateDraw(degrees = angle, pivot = c) {
            drawArc(
                brush = Brush.sweepGradient(0f to Color.Transparent, 0.12f to Color(0xFF8D3EDD).copy(alpha = 0.32f), 0.121f to Color.Transparent, center = c),
                startAngle = 0f, sweepAngle = 360f, useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - size.width * 0.76f, c.y - size.width * 0.76f),
                size = androidx.compose.ui.geometry.Size(size.width * 1.52f, size.width * 1.52f)
            )
        }
    }
}

private val LocalCheckColor = androidx.compose.runtime.compositionLocalOf { Color(0xFF7A2ACB) }

@Composable
private fun RoundCheck(checked: Boolean, onToggle: () -> Unit) {
    Box(Modifier.size(40.dp).tapPlainClickable("junk_scan_view_tap_1", onClick = onToggle), contentAlignment = Alignment.Center) {
        com.phonecleaner.aicleaner.presentation.ui.components.RoundCheck(checked, color = LocalCheckColor.current)
    }
}


private val CleanGreen = Color(0xFF7ED8A0)

private fun cleanColor(t: Float): Color =
    if (t > 0.35f) heatColor(t) else lerp(CleanGreen, HeatPeach, (t / 0.35f).coerceIn(0f, 1f))
