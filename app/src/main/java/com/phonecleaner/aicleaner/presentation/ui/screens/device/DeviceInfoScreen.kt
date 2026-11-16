package com.phonecleaner.aicleaner.presentation.ui.screens.device

import android.text.format.Formatter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

private fun sectionStyle(title: Int): Pair<ImageVector, Color> = when (title) {
    R.string.device_section_device -> Icons.Rounded.Smartphone to Brand.Blue
    R.string.device_section_system -> Icons.Rounded.Settings to Brand.Apps
    R.string.device_section_hardware -> Icons.Rounded.Memory to Brand.Photo
    R.string.device_section_storage -> Icons.Rounded.Storage to Brand.Junk
    else -> Icons.Rounded.Tv to Brand.Process
}

/**
 * Info Center: a header with the device name, live storage and RAM rings that lead to the cleaning
 * screens, CPU and battery details, and the full read-only list of device facts underneath.
 */
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DeviceHeader(usage)
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

            Text(
                stringResource(R.string.info_more_details),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
            )
            sections.forEach { Section(it) }
            Spacer(Modifier.height(16.dp + getBannerOverlayHeight()))
        }
    }
}

@Composable
private fun DeviceHeader(usage: UsageSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brand.Hero)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(76.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SmartToy, null, tint = Brand.Blue, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                usage.deviceName,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                style = TextStyle(fontSize = 19.sp, lineHeight = 23.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                usage.androidVersion + "  •  " + usage.abi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.85f),
                style = TextStyle(fontSize = 13.sp, fontFamily = medium)
            )
        }
    }
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
        Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
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
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Brush.horizontalGradient(listOf(start, end)))
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                Text(action, color = Color.White, style = TextStyle(fontSize = 15.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).tintedCard(accent).padding(16.dp),
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).tintedCard(Brand.Photo).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.info_cpu_detail), color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 13.sp, fontFamily = medium))
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.info_cores, usage.cores),
                color = MaterialTheme.colorScheme.tertiary,
                style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold)
            )
            Text(
                usage.processor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.surface,
                style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontFamily = medium)
            )
        }
        Spacer(Modifier.size(12.dp))
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Brand.Photo.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Memory, null, tint = Brand.Photo, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BatteryCard(battery: BatteryInfo) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).tintedCard(Brand.Blue).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.info_battery_info), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        val tiles = listOf(
            Triple(Icons.Rounded.BatteryFull, stringResource(R.string.info_level) to "${battery.level}%", Brand.Blue),
            Triple(Icons.Rounded.Favorite, stringResource(R.string.battery_health) to healthLabel(battery.health), Brand.Good),
            Triple(Icons.Rounded.Bolt, stringResource(R.string.battery_voltage) to "%.2f V".format(battery.voltageMv / 1000f), Brand.Gold),
            Triple(Icons.Rounded.Thermostat, stringResource(R.string.battery_temperature) to "%.1f °C".format(battery.temperatureC), Brand.Junk),
            Triple(Icons.Rounded.Power, stringResource(R.string.battery_source) to sourceLabel(battery.plugged), Brand.Apps),
            Triple(Icons.Rounded.Memory, stringResource(R.string.battery_capacity) to (battery.estimatedCapacityMah?.let { "$it mAh" } ?: "—"), Brand.Photo)
        )
        tiles.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (icon, pair, tint) ->
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(tint.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(tint.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pair.first, maxLines = 1, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
                            Text(pair.second, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
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

/** Card surface with a soft tint fading from the top, so the cards have a little depth without shouting. */
@Composable
private fun Modifier.tintedCard(tint: Color): Modifier = this
    .background(MaterialTheme.colorScheme.secondary)
    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.16f), Color.Transparent)))
