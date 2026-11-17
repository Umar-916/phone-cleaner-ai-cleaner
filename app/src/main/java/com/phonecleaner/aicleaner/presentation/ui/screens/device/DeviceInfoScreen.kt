package com.phonecleaner.aicleaner.presentation.ui.screens.device

import com.phonecleaner.aicleaner.presentation.ui.theme.AppFonts
import com.phonecleaner.aicleaner.core.common.tapped
import com.phonecleaner.aicleaner.core.common.tapClickable
import android.text.format.Formatter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.graphicsLayer
import com.phonecleaner.aicleaner.presentation.ui.components.featheredEdges
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.phonecleaner.aicleaner.data.battery.BatteryInfo
import com.phonecleaner.aicleaner.data.battery.BatteryRepository
import com.phonecleaner.aicleaner.data.device.DeviceInfoRepository
import com.phonecleaner.aicleaner.data.device.InfoSection
import com.phonecleaner.aicleaner.data.device.UsageSummary
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery.healthLabel
import com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery.sourceLabel
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.koinInject

private val semiBold = AppFonts.SemiBold
private val medium = AppFonts.Medium
private val bold = AppFonts.Bold

private fun sectionStyle(title: Int): Pair<ImageVector, Color> = when (title) {
    R.string.device_section_device -> Icons.Rounded.Smartphone to Brand.Blue
    R.string.device_section_system -> Icons.Rounded.Settings to Brand.Apps
    R.string.device_section_hardware -> Icons.Rounded.Memory to Brand.Photo
    R.string.device_section_storage -> Icons.Rounded.Storage to Brand.Junk
    else -> Icons.Rounded.Tv to Brand.Process
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    onBack: () -> Unit,
    onClean: () -> Unit = {},
    onOptimize: () -> Unit = {},
    repository: DeviceInfoRepository = koinInject(),
    batteryRepository: BatteryRepository = koinInject()
) {
    val sections = remember { repository.read() }
    val usage = remember { repository.usage() }
    val battery by remember { batteryRepository.observe() }.collectAsState(initial = BatteryInfo())
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = { AppTopBarTitle(stringResource(R.string.info_center_title)) }, onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DeviceHeader(usage)
            Text(stringResource(R.string.info_device_info), modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            UsageCard(
                title = stringResource(R.string.info_storage_used),
                fraction = usage.storageFraction,
                detail = Formatter.formatFileSize(LocalContext.current, usage.storageUsed) + " / " +
                    Formatter.formatFileSize(LocalContext.current, usage.storageTotal),
                action = stringResource(R.string.info_clean),
                accent = Brand.Blue,
                ringEnd = Brand.Cyan,
                ringFirst = true,
                onAction = onClean
            )
            UsageCard(
                title = stringResource(R.string.info_ram_used),
                fraction = usage.ramFraction,
                detail = Formatter.formatFileSize(LocalContext.current, usage.ramUsed) + " / " +
                    Formatter.formatFileSize(LocalContext.current, usage.ramTotal),
                action = stringResource(R.string.info_optimize),
                accent = Brand.Photo,
                ringEnd = Brand.BlueLight,
                ringFirst = false,
                onAction = onOptimize
            )
            CpuCard(usage)
            BatteryCard(battery)

            Spacer(Modifier.height(16.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun DeviceHeader(usage: UsageSummary) {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "infoArt")
    val bob by t.animateFloat(-1f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(2600, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "bob")
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(R.drawable.infocenter_art),
        contentDescription = usage.deviceName,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .graphicsLayer { translationY = bob * 5.dp.toPx() }
            .featheredEdges(0.12f)
    )
}

@Composable
private fun UsageCard(
    title: String,
    fraction: Float,
    detail: String,
    action: String,
    accent: Color,
    ringEnd: Color,
    ringFirst: Boolean,
    onAction: () -> Unit
) {
    val animated by animateFloatAsState(fraction, tween(900, easing = FastOutSlowInEasing), label = "usageRing")
    val warn = fraction >= 0.9f
    val start = if (warn) Brand.Danger else accent
    val end = if (warn) Brand.GoldDeep else ringEnd

    val ring: @Composable () -> Unit = {
        Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 11.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(accent.copy(alpha = 0.14f), 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                rotate(-90f) {
                    drawArc(
                        Brush.sweepGradient(0f to start, 1f to end, center = center),
                        0f, 360f * animated, false, topLeft, arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(fraction * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 22.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
                )
                Text(stringResource(R.string.info_used), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
            }
        }
    }
    val info: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, horizontalAlignment = if (ringFirst) Alignment.Start else Alignment.Start) {
            Text(title, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 13.sp, fontFamily = medium))
            Spacer(Modifier.height(2.dp))
            Text(
                detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Brush.horizontalGradient(listOf(start, end)))
                    .tapClickable("usage_card_click", onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                Text(action, color = Color.White, style = TextStyle(fontSize = 15.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).tintedCard(accent).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ringFirst) {
            ring()
            Spacer(Modifier.size(16.dp))
            info(Modifier.weight(1f))
        } else {
            info(Modifier.weight(1f))
            Spacer(Modifier.size(16.dp))
            ring()
        }
    }
}

@Composable
private fun CpuCard(usage: UsageSummary) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).tintedCard(Brand.Photo).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.info_cpu_detail), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                Text(
                    stringResource(R.string.info_high_performance),
                    modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFCDF3DC)).padding(horizontal = 6.dp, vertical = 2.dp),
                    color = Color(0xFF1B8A4B),
                    style = TextStyle(fontSize = 9.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text("•  " + stringResource(R.string.info_cores, usage.cores), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 13.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            Text("•  " + usage.abi + " " + stringResource(R.string.info_architecture), maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 13.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.size(12.dp))
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(Color(0xFF8D3EDD), Color(0xFF6A1FC0)))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Memory, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
    }
}

@Composable
private fun BatteryCard(battery: BatteryInfo) {
    val mah = battery.estimatedCapacityMah
    @Composable
    fun Row2(label: String, value: String, icon: ImageVector?, tint: Color, extra: (@Composable () -> Unit)? = null) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).tintedCard(tint).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
                Text(value, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 15.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            }
            extra?.invoke()
            if (icon != null) Icon(icon, null, tint = tint, modifier = Modifier.size(30.dp))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.info_battery_info), modifier = Modifier.padding(start = 4.dp), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        Row2(stringResource(R.string.info_current_capacity), "${battery.level}%", null, Brand.Photo) {
            Column(horizontalAlignment = Alignment.End) {
                com.phonecleaner.aicleaner.presentation.ui.components.AppProgressBar(battery.level / 100f, Modifier.width(80.dp), height = 8.dp)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.info_approx, mah?.toString() ?: "—"), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 10.sp, fontFamily = medium))
            }
        }
        Row2(stringResource(R.string.info_total_capacity), (mah?.let { "$it mAh" } ?: "—"), Icons.Rounded.BatteryFull, Color(0xFF2FC76B))
        Row2(stringResource(R.string.battery_health), healthLabel(battery.health), Icons.Rounded.Favorite, Color(0xFFF2605F))
        Row2(stringResource(R.string.battery_voltage), "%.1f V".format(battery.voltageMv / 1000f), Icons.Rounded.Bolt, Color(0xFFF5A524))
    }
}

@Composable
private fun Section(section: InfoSection) {
    val (icon, tint) = sectionStyle(section.title)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.secondary).padding(vertical = 8.dp)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.size(10.dp))
            Text(
                stringResource(section.title),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
        }
        section.rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(row.label),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 14.sp, fontFamily = medium)
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    row.value,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun Modifier.tintedCard(tint: Color): Modifier = this
    .background(MaterialTheme.colorScheme.secondary)
    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.08f), Color.Transparent)))
    .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
