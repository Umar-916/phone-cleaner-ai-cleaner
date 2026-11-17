package com.phonecleaner.aicleaner.presentation.ui.screens.cleaner.battery

import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.phonecleaner.aicleaner.R

@Composable
internal fun healthLabel(health: Int): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> stringResource(R.string.battery_health_good)
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> stringResource(R.string.battery_health_overheat)
    BatteryManager.BATTERY_HEALTH_COLD -> stringResource(R.string.battery_health_cold)
    BatteryManager.BATTERY_HEALTH_DEAD -> stringResource(R.string.battery_health_dead)
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> stringResource(R.string.battery_health_voltage)
    else -> stringResource(R.string.battery_unknown)
}

@Composable
internal fun sourceLabel(plugged: Int): String = when (plugged) {
    BatteryManager.BATTERY_PLUGGED_AC -> stringResource(R.string.battery_source_ac)
    BatteryManager.BATTERY_PLUGGED_USB -> stringResource(R.string.battery_source_usb)
    BatteryManager.BATTERY_PLUGGED_WIRELESS -> stringResource(R.string.battery_source_wireless)
    else -> stringResource(R.string.battery_source_battery)
}
