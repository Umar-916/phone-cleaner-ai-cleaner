package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery

import android.content.Intent
import android.os.BatteryManager
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phonecleaner.aicleaner.R
import com.phonecleaner.aicleaner.core.common.postAnalytic
import com.phonecleaner.aicleaner.core.common.tryCatch
import com.phonecleaner.aicleaner.data.battery.BatteryInfo
import com.phonecleaner.aicleaner.data.battery.VolumeLevel
import com.phonecleaner.aicleaner.data.battery.VolumeStream
import com.phonecleaner.aicleaner.domain.BatteryViewModel
import com.phonecleaner.aicleaner.presentation.navigation.getBannerOverlayHeight
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBar
import com.phonecleaner.aicleaner.presentation.ui.components.AppTopBarTitle
import com.phonecleaner.aicleaner.presentation.ui.components.PermissionPrompt
import com.phonecleaner.aicleaner.presentation.ui.theme.Brand
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val semiBold = FontFamily(Font(R.font.google_sans_flex_semi))
private val medium = FontFamily(Font(R.font.google_sans_flex_medium))
private val bold = FontFamily(Font(R.font.google_sans_flex_bold))

private val timeoutOptions = listOf(15_000, 30_000, 60_000, 120_000, 300_000, 600_000)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BatteryManagerScreen(
    onBack: () -> Unit,
    viewModel: BatteryViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var needsPermission by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.start()
                Lifecycle.Event.ON_PAUSE -> viewModel.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stop()
        }
    }

    fun openSettings(action: String) {
        tryCatch { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = { AppTopBarTitle(stringResource(R.string.feature_battery)) }, onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BatteryHero(state.battery)

            ControlCard(Icons.Rounded.Timer, stringResource(R.string.battery_timeout), Brand.Blue) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    timeoutOptions.forEach { option ->
                        BatteryChip(state.screenTimeoutMs == option, timeoutLabel(option)) {
                            if (!viewModel.setScreenTimeout(option)) needsPermission = true
                        }
                    }
                }
            }

            ControlCard(Icons.Rounded.BrightnessMedium, stringResource(R.string.battery_brightness), Brand.Gold) {
                Slider(
                    value = state.brightness.toFloat(),
                    onValueChange = { if (!viewModel.setBrightness(it.roundToInt())) needsPermission = true },
                    valueRange = 1f..255f
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (state.autoBrightness) R.string.battery_auto_on else R.string.battery_auto_off),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface,
                        style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                    )
                    LinkText(stringResource(R.string.battery_auto_open)) { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                }
            }

            ControlCard(Icons.Rounded.VolumeUp, stringResource(R.string.battery_sound), Brand.Good) {
                state.volumes.forEach { level -> VolumeRow(level) { viewModel.setVolume(level.stream, it) } }
            }

            ControlCard(Icons.Rounded.Vibration, stringResource(R.string.battery_vibration), Brand.Photo) {
                Text(
                    stringResource(if (state.hasVibrator) R.string.battery_vibration_available else R.string.battery_vibration_missing),
                    color = MaterialTheme.colorScheme.surface,
                    style = TextStyle(fontSize = 13.sp, fontFamily = medium)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 6.dp)) {
                    if (state.hasVibrator) LinkText(stringResource(R.string.battery_vibration_test)) { viewModel.testVibration() }
                    LinkText(stringResource(R.string.battery_vibration_settings)) { openSettings(Settings.ACTION_SOUND_SETTINGS) }
                }
            }

            ControlCard(Icons.Rounded.PowerSettingsNew, stringResource(R.string.battery_optimization), Brand.Battery) {
                LinkText(stringResource(R.string.battery_saver)) { context.postAnalytic("battery_saver_open"); viewModel.markBatteryTask(); openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                Spacer(Modifier.height(6.dp))
                LinkText(stringResource(R.string.battery_optimization_list)) { openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) }
                Spacer(Modifier.height(6.dp))
                LinkText(stringResource(R.string.battery_usage)) { openSettings("android.intent.action.POWER_USAGE_SUMMARY") }
            }

            ControlCard(Icons.Rounded.Lightbulb, stringResource(R.string.battery_tips_title), Brand.Gold) {
                listOf(R.string.battery_tip_1, R.string.battery_tip_2, R.string.battery_tip_3, R.string.battery_tip_4).forEach { tip ->
                    Text(
                        "• " + stringResource(tip),
                        modifier = Modifier.padding(bottom = 6.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontFamily = medium)
                    )
                }
            }
            Spacer(Modifier.height(16.dp + getBannerOverlayHeight()))
        }
    }

    if (needsPermission) {
        PermissionPrompt(
            icon = Icons.Rounded.Settings,
            title = stringResource(R.string.battery_perm_title),
            body = stringResource(R.string.battery_perm_body),
            bullets = listOf(stringResource(R.string.battery_perm_b1), stringResource(R.string.battery_perm_b2)),
            allowLabel = stringResource(R.string.perm_allow),
            notNowLabel = stringResource(R.string.perm_not_now),
            accent = Brand.Battery,
            onAllow = {
                needsPermission = false
                tryCatch {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:${context.packageName}".toUri()))
                }
            },
            onDismiss = { needsPermission = false }
        )
    }
}

@Composable
private fun timeoutLabel(ms: Int): String = when {
    ms < 60_000 -> stringResource(R.string.battery_seconds, ms / 1000)
    else -> stringResource(R.string.battery_minutes, ms / 60_000)
}

@Composable
private fun BatteryHero(info: BatteryInfo) {
    val animated by animateFloatAsState(info.level / 100f, tween(900, easing = FastOutSlowInEasing), label = "batteryLevel")
    val accent = when {
        info.isCharging -> Brand.Good
        info.level <= 20 -> Brand.Danger
        else -> Brand.Blue
    }
    val track = accent.copy(alpha = 0.16f)
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.secondary).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 16.dp.toPx()
                val arc = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(track, 135f, 270f, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(accent, 135f, 270f * animated, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (info.isCharging) Icon(Icons.Rounded.BatteryChargingFull, null, tint = accent, modifier = Modifier.size(24.dp))
                Text("${info.level}%", color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 40.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
                Text(statusLabel(info), color = accent, style = TextStyle(fontSize = 13.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            InfoStat(stringResource(R.string.battery_health), healthLabel(info.health))
            InfoStat(stringResource(R.string.battery_temperature), "%.1f °C".format(info.temperatureC))
            InfoStat(stringResource(R.string.battery_voltage), "%.2f V".format(info.voltageMv / 1000f))
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            InfoStat(stringResource(R.string.battery_technology), info.technology.ifBlank { "—" })
            InfoStat(stringResource(R.string.battery_capacity), info.estimatedCapacityMah?.let { "~$it mAh" } ?: "—")
            InfoStat(stringResource(R.string.battery_source), sourceLabel(info.plugged))
        }
    }
}

@Composable
private fun statusLabel(info: BatteryInfo): String = when (info.status) {
    BatteryManager.BATTERY_STATUS_CHARGING -> stringResource(R.string.battery_charging)
    BatteryManager.BATTERY_STATUS_FULL -> stringResource(R.string.battery_full)
    BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> stringResource(R.string.battery_discharging)
    else -> stringResource(R.string.battery_unknown)
}

@Composable
private fun healthLabel(health: Int): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> stringResource(R.string.battery_health_good)
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> stringResource(R.string.battery_health_overheat)
    BatteryManager.BATTERY_HEALTH_COLD -> stringResource(R.string.battery_health_cold)
    BatteryManager.BATTERY_HEALTH_DEAD -> stringResource(R.string.battery_health_dead)
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> stringResource(R.string.battery_health_voltage)
    else -> stringResource(R.string.battery_unknown)
}

@Composable
private fun sourceLabel(plugged: Int): String = when (plugged) {
    BatteryManager.BATTERY_PLUGGED_AC -> stringResource(R.string.battery_source_ac)
    BatteryManager.BATTERY_PLUGGED_USB -> stringResource(R.string.battery_source_usb)
    BatteryManager.BATTERY_PLUGGED_WIRELESS -> stringResource(R.string.battery_source_wireless)
    else -> stringResource(R.string.battery_source_battery)
}

@Composable
private fun InfoStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 15.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold))
        Text(label, color = MaterialTheme.colorScheme.surface, style = TextStyle(fontSize = 11.sp, fontFamily = medium))
    }
}

@Composable
private fun ControlCard(icon: ImageVector, title: String, tint: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.secondary).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(10.dp))
            Text(title, color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 16.sp, fontFamily = bold, fontWeight = FontWeight.Bold))
        }
        content()
    }
}

@Composable
private fun BatteryChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.background,
            labelColor = MaterialTheme.colorScheme.tertiary,
            selectedContainerColor = primary.copy(alpha = 0.15f),
            selectedLabelColor = primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), selectedBorderColor = primary
        )
    )
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp, horizontal = 2.dp),
        color = MaterialTheme.colorScheme.primary,
        style = TextStyle(fontSize = 14.sp, fontFamily = semiBold, fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun VolumeRow(level: VolumeLevel, onChange: (Int) -> Unit) {
    val label = when (level.stream) {
        VolumeStream.MEDIA -> R.string.battery_vol_media
        VolumeStream.ALARM -> R.string.battery_vol_alarm
        VolumeStream.RING -> R.string.battery_vol_ring
        VolumeStream.NOTIFICATION -> R.string.battery_vol_notification
    }
    Column {
        Text(stringResource(label), color = MaterialTheme.colorScheme.tertiary, style = TextStyle(fontSize = 13.sp, fontFamily = medium))
        Slider(
            value = level.current.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..level.max.toFloat().coerceAtLeast(1f)
        )
    }
}
