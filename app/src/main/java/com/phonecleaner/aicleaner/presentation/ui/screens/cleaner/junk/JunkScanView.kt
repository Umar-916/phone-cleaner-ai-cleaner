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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.rotate as rotateDraw
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.launch
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
private const val RADAR_SCALE = 0.8f // 1f = old size
private val ENTER_EASING = androidx.compose.animation.core.CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private const val ROW_STAGGER_MS = 110L

private fun groupIcon(group: JunkGroup): Pair<ImageVector, Color> = when (group) {
    JunkGroup.RESIDUALS -> Icons.Rounded.Description to Color(0xFFB5179E)
    JunkGroup.OBSOLETE -> Icons.Rounded.FolderDelete to Color(0xFF6F3FD1)
    JunkGroup.AD_JUNK -> Icons.Rounded.CleaningServices to Color(0xFF3D7BFF)
    JunkGroup.TRASH -> Icons.Rounded.Delete to Color(0xFFC62828)
}

// Same formatter as the group rows and the Clean button, so every number on screen matches.
private fun splitSize(context: android.content.Context, bytes: Long): Pair<String, String> {
    val text = Formatter.formatFileSize(context, bytes)
    val cut = text.lastIndexOfAny(charArrayOf(' ', ' ', ' '))
    return if (cut < 0) text to "" else text.substring(0, cut) to text.substring(cut + 1)
}

/** "Scanning" + 1..3 animated dots; unseen dots are transparent so the text never shifts. */
@Composable
private fun animatedDots(base: String): androidx.compose.ui.text.AnnotatedString {
    val v by androidx.compose.animation.core.rememberInfiniteTransition(label = "dots").animateFloat(
        0f, 3f,
        androidx.compose.animation.core.infiniteRepeatable(tween(1500, easing = androidx.compose.animation.core.LinearEasing)),
        label = "dotsValue"
    )
    val n = v.toInt().coerceIn(0, 2) + 1
    val clean = base.trimEnd('.', '…', ' ')
    return androidx.compose.ui.text.buildAnnotatedString {
        append(clean)
        append(".".repeat(n))
        withStyle(androidx.compose.ui.text.SpanStyle(color = Color.Transparent)) { append(".".repeat(3 - n)) }
    }
}

@Composable
fun JunkScanView(
    state: JunkUiState,
    viewModel: JunkViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit = {},
    onJunkFree: () -> Unit = {}
) {
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(state is JunkUiState.Done) {
        if (state is JunkUiState.Done) {
            kotlinx.coroutines.delay(1700)
            onDone()
        }
    }
    var startPhase by remember { mutableStateOf(state is JunkUiState.Scanning) }
    val firstRun = remember { state is JunkUiState.Scanning }
    val radarAlpha = remember { androidx.compose.animation.core.Animatable(if (firstRun) 1f else 0f) }
    val sweep = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (firstRun) {
            sweep.animateTo(360f, tween(4000, easing = androidx.compose.animation.core.LinearEasing))
        } else {
            kotlinx.coroutines.delay(600)
        }
        startPhase = false
        radarAlpha.animateTo(0f, tween(450))
    }
    val cleaning = state as? JunkUiState.Cleaning
    val scanning = state is JunkUiState.Scanning || cleaning != null
    val start = startPhase && state is JunkUiState.Scanning
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
    // Follows the real scan; the timer only creeps a little until the first progress arrives.
    val scanFraction = if (progress != null && progress.fraction > 0f) {
        (progress.fraction / 0.95f).coerceIn(0f, 1f) * 0.99f
    } else {
        minOf(timed.value, 0.04f)
    }
    val fraction by animateFloatAsState(
        if (cleaning != null) 1f - cleanFraction else if (scanning) scanFraction else 1f,
        tween(300), label = "junkFraction"
    )
    val isDone = state is JunkUiState.Done
    val totalBytes = when {
        isDone -> 0L
        cleaning != null -> (cleaning.totalBytes * (1f - cleanFraction)).toLong()
        // Header = sum of the group rows, so the top number always matches what is listed below.
        scanning -> progress?.categoryBytes?.values?.sum() ?: 0L
        else -> result?.totalBytes ?: 0L
    }
    // Nothing found (already cleaned): the result is "junk free", shown in the calm green header.
    val junkFree = result != null && result.items.isEmpty()
    val heat = when {
        isDone || junkFree -> 0f
        cleaning != null -> 1f - cleanFraction
        // No junk found so far: keep the header calm instead of heating up to red.
        scanning -> if (totalBytes == 0L) minOf(fraction, 0.3f) else fraction.coerceIn(0f, 1f)
        else -> 1f
    }
    val shade = if (cleaning != null || isDone || junkFree) heat else Math.pow(heat.toDouble(), 2.6).toFloat()
    val headerColor by animateColorAsState(if (cleaning != null || isDone || junkFree) cleanColor(shade) else lerp(Color(0xFFE2CDF8), heatColor(shade), if (start) 0f else (shade * 6f).coerceIn(0f, 1f)), tween(500, easing = androidx.compose.animation.core.LinearEasing), label = "junkHeader")
    // Pick text colour from the header's actual luminance: a hard switch avoids the muddy
    // in-between colours a blend produced on pink/red backgrounds.
    val onHeader = if (headerColor.luminance() < 0.2f) Color.White else Ink
    val checkColor by animateColorAsState(lerp(OnboardingPalette.purple, Color(0xFFE23B3B), ((shade - 0.7f) / 0.3f).coerceIn(0f, 1f)), tween(500), label = "checkColor")
    val shownBytes by animateFloatAsState(totalBytes.toFloat(), tween(450, easing = androidx.compose.animation.core.LinearEasing), label = "junkBytes")
    val (number, unit) = if (junkFree) "0" to "MB" else splitSize(context, shownBytes.toLong())

    var confirm by rememberSaveable { mutableStateOf(false) }
    var expanded by remember(result?.items?.size) {
        mutableStateOf(emptySet<JunkGroup>())
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalCheckColor provides checkColor) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(Brush.verticalGradient(listOf(headerColor, headerColor.copy(alpha = 0.82f))))
        ) {
            if (radarAlpha.value > 0f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = radarAlpha.value }) { RadarSweep(sweep.value) }
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppBackButton(onClick = onBack)
                Text(
                    stringResource(R.string.junk_header_title),
                    modifier = Modifier.padding(start = 11.3.dp),
                    color = onHeader,
                    style = TextStyle(fontSize = 20.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.animation.Crossfade(targetState = start, modifier = Modifier.fillMaxWidth().height(100.dp), animationSpec = tween(450), label = "junkStart") { isStart ->
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (isStart) {
                    Text("${(fraction * 100).toInt().coerceAtLeast(1)}%", color = onHeader, style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        animatedDots(stringResource(R.string.junk_start_title)),
                        color = onHeader,
                        style = TextStyle(fontSize = 32.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        progress?.currentPath?.substringAfterLast('/').orEmpty(),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = onHeader.copy(alpha = 0.8f),
                        style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                    )
                } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        number,
                        color = onHeader,
                        style = TextStyle(fontSize = 64.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        unit,
                        modifier = Modifier.padding(start = 3.dp, bottom = 10.dp),
                        color = onHeader,
                        style = TextStyle(fontSize = 11.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    "${(fraction * 100).toInt()}%",
                    color = onHeader,
                    style = TextStyle(fontSize = 18.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                }
                }
                }
                Spacer(Modifier.height(10.dp))
                // Fixed-height slot so the sheet below never jumps between intro / scanning / result.
                Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.TopCenter) {
                androidx.compose.animation.Crossfade(
                    targetState = if (start) 0 else if (scanning) 1 else 2,
                    animationSpec = tween(450),
                    label = "junkSubHeader"
                ) { mode ->
                if (mode == 0) Unit else if (mode == 1) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val stageRes = listOf(R.string.junk_stage_1, R.string.junk_stage_2, R.string.junk_stage_3, R.string.junk_stage_4, R.string.junk_stage_5)
                    androidx.compose.animation.Crossfade(
                        targetState = if (cleaning != null) -1 else (fraction * stageRes.size).toInt().coerceIn(0, stageRes.lastIndex),
                        label = "junkStage"
                    ) { stage ->
                        Text(
                            animatedDots(if (stage < 0) stringResource(R.string.junk_cleaning_label) else stringResource(stageRes[stage])),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = onHeader.copy(alpha = 0.8f),
                            style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        progress?.currentPath?.substringAfterLast('/').orEmpty(),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        color = onHeader.copy(alpha = 0.65f),
                        style = TextStyle(fontSize = 11.sp, fontFamily = medium)
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
                        if (junkFree) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                stringResource(R.string.junk_free_banner),
                                modifier = Modifier.padding(start = 10.dp),
                                color = Color.White,
                                style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                            )
                        } else {
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
                if (junkFree) JunkFreeBody(Modifier.weight(1f)) else JunkList(
                    modifier = Modifier.weight(1f),
                    cleaning = cleaning,
                    cleanFraction = if (cleaning != null) cleanFraction else 0f,
                    result = result,
                    scanning = scanning,
                    progress = progress,
                    revealed = revealed,
                    stepsDone = (fraction / 0.23f).toInt(),
                    start = start,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    viewModel = viewModel
                )

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
                } else if (junkFree) {
                    Box(Modifier.fillMaxWidth().padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 22.dp + getBannerOverlayHeight())) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8D3EDD), OnboardingPalette.purple)))
                                .tapClickable("junk_scan_view_junk_free") { onJunkFree() },
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

/** Shown when nothing is left to clean: a clear "0 MB, all cleared" message instead of empty rows. */
@Composable
private fun JunkFreeBody(modifier: Modifier) {
    val pop = remember { androidx.compose.animation.core.Animatable(0.4f) }
    val fade = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            launch { pop.animateTo(1f, androidx.compose.animation.core.spring(androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, androidx.compose.animation.core.Spring.StiffnessLow)) }
            launch { fade.animateTo(1f, tween(500)) }
        }
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp).graphicsLayer { alpha = fade.value },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(104.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                .clip(CircleShape).background(Color(0xFF55C97E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.junk_free_title),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.junk_free_caption),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.surface,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
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
    onToggleChecked: () -> Unit,
    hideSize: Boolean = false
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
        if (expandable || (scanning && group != JunkGroup.RESIDUALS)) {
            Icon(
                if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 4.dp).size(22.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        if (!hideSize) Text(
            Formatter.formatFileSize(context, bytes),
            color = MaterialTheme.colorScheme.tertiary,
            style = TextStyle(fontSize = 14.sp, fontFamily = medium)
        )
        Box(Modifier.padding(start = 6.dp).size(40.dp), contentAlignment = Alignment.Center) {
            androidx.compose.animation.Crossfade(
                targetState = if (scanning && stepDone) 1 else if (scanning) 0 else 2,
                animationSpec = tween(350),
                label = "groupTrailing"
            ) { mode ->
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    when (mode) {
                        1 -> Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF55C97E)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        0 -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        )
                        else -> RoundCheck(checked, onToggleChecked)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevealRow(item: JunkItem) {
    // Same entrance as the battery list, but quicker and sliding in from the left.
    val enter = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) { enter.animateTo(1f, tween(300, easing = ENTER_EASING)) }
    Box(
        Modifier.graphicsLayer {
            alpha = enter.value
            translationX = -(1f - enter.value) * 90.dp.toPx()
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
            else Formatter.formatFileSize(context, item.sizeBytes),
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
private fun RadarSweep(angle: Float) {
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height)
        // Radar is drawn smaller than before (RADAR_SCALE); the header background is unchanged.
        val r = size.width * RADAR_SCALE
        drawCircle(Color.White.copy(alpha = 0.3f), radius = r * 0.76f, center = c)
        listOf(0.30f, 0.52f, 0.76f).forEach {
            drawCircle(Color.White.copy(alpha = 0.45f), radius = r * it, center = c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx()))
        }
        rotateDraw(degrees = angle, pivot = c) {
            drawArc(
                brush = Brush.sweepGradient(0f to Color.Transparent, 0.12f to Color(0xFF8D3EDD).copy(alpha = 0.85f), 0.121f to Color.Transparent, center = c),
                startAngle = 0f, sweepAngle = 360f, useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - r * 0.76f, c.y - r * 0.76f),
                size = androidx.compose.ui.geometry.Size(r * 1.52f, r * 1.52f)
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

@Composable
private fun JunkList(
    modifier: Modifier,
    cleaning: JunkUiState.Cleaning?,
    cleanFraction: Float,
    result: JunkUiState.Result?,
    scanning: Boolean,
    progress: com.phonecleaner.aicleaner.domain.model.ScanProgress?,
    revealed: List<JunkItem>,
    stepsDone: Int,
    start: Boolean,
    expanded: Set<JunkGroup>,
    onExpandedChange: (Set<JunkGroup>) -> Unit,
    viewModel: JunkViewModel
) {
        LazyColumn(
    modifier = modifier,
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
                        // When values start (intro over) the group rows slide in one by one, left to right.
                        val enter = remember { androidx.compose.animation.core.Animatable(if (scanning && start) 0f else 1f) }
                        androidx.compose.runtime.LaunchedEffect(start) {
                            if (!start && enter.value < 1f) {
                                kotlinx.coroutines.delay(groupIndex * ROW_STAGGER_MS)
                                enter.animateTo(1f, tween(300, easing = ENTER_EASING))
                            }
                        }
                        Box(Modifier.animateItem().graphicsLayer {
                            alpha = enter.value
                            translationX = -(1f - enter.value) * 90.dp.toPx()
                        }) { GroupRow(
                            group = group,
                            bytes = groupBytes,
                            scanning = scanning,
                            stepDone = scanning && !start && stepsDone >= groupIndex + 1,
                            hideSize = start,
                            expandable = !scanning && items.isNotEmpty(),
                            open = isOpen,
                            checked = !scanning && items.isNotEmpty() && items.all { it.path in result!!.selected },
                            onToggleOpen = { onExpandedChange(if (isOpen) expanded - group else expanded + group) },
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
}
