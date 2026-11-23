package com.phonecleaner.aicleaner.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import com.phonecleaner.aicleaner.data.process.ProcessRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class BatteryInfo(
    val level: Int = 0,
    val status: Int = BatteryManager.BATTERY_STATUS_UNKNOWN,
    val plugged: Int = 0,
    val health: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val temperatureC: Float = 0f,
    val voltageMv: Int = 0,
    val technology: String = "",
    val estimatedCapacityMah: Int? = null,
    val chargeTimeRemainingMs: Long? = null
) {
    val isCharging: Boolean get() = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
}

enum class VolumeStream(val stream: Int) {
    MEDIA(AudioManager.STREAM_MUSIC),
    ALARM(AudioManager.STREAM_ALARM),
    RING(AudioManager.STREAM_RING),
    NOTIFICATION(AudioManager.STREAM_NOTIFICATION)
}

data class VolumeLevel(val stream: VolumeStream, val current: Int, val max: Int)

class BatteryRepository(private val context: Context, private val processes: ProcessRepository) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val resolver = context.contentResolver

    fun observe(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { trySend(read(it)) }
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        sticky?.let { trySend(read(it)) }
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }

    fun canWriteSettings(): Boolean = Settings.System.canWrite(context)

    fun screenTimeoutMs(): Int = runCatching { Settings.System.getInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT) }.getOrDefault(0)

    fun setScreenTimeout(ms: Int): Boolean = canWriteSettings() &&
        runCatching { Settings.System.putInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, ms) }.getOrDefault(false)

    fun brightness(): Int = runCatching { Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS) }.getOrDefault(0)

    fun isAutoBrightness(): Boolean = runCatching {
        Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }.getOrDefault(false)

    fun setBrightness(value: Int): Boolean {
        if (!canWriteSettings()) return false
        val level = value.coerceIn(0, 255)
        return runCatching {
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            // Android 10+ devices read the float key; write it too so the change actually applies.
            runCatching { Settings.System.putFloat(resolver, "screen_brightness_float", level / 255f) }
            val ok = Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, level)
            // Nudge observers so the system re-applies the new brightness immediately.
            runCatching { resolver.notifyChange(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), null) }
            ok
        }.getOrDefault(false)
    }

    fun volumes(): List<VolumeLevel> = VolumeStream.entries.map {
        VolumeLevel(it, audioManager.getStreamVolume(it.stream), audioManager.getStreamMaxVolume(it.stream))
    }

    fun setVolume(stream: VolumeStream, value: Int): Boolean =
        runCatching { audioManager.setStreamVolume(stream.stream, value, 0) }.isSuccess

    fun ringerMode(): Int = audioManager.ringerMode

    @Suppress("DEPRECATION")
    fun hasVibrator(): Boolean = (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.hasVibrator() == true

    @Suppress("DEPRECATION")
    fun testVibration() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    enum class SaverStep { TIMEOUT, SOUND, BRIGHTNESS, VIBRATION, BACKGROUND }


    private fun setAutoRotate(enabled: Boolean): Boolean = canWriteSettings() &&
        runCatching { Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, if (enabled) 1 else 0) }.getOrDefault(false)

    fun applySaver(step: SaverStep): Boolean = runCatching {
        when (step) {
            SaverStep.SOUND -> {
                // Media volume is left alone: muting the user's music/video volume unasked is surprising.
                listOf(AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_SYSTEM)
                    .forEach { stream -> runCatching { audioManager.setStreamVolume(stream, 0, 0) } }
                if (canWriteSettings()) {
                    listOf(Settings.System.SOUND_EFFECTS_ENABLED, "lockscreen_sounds_enabled", Settings.System.DTMF_TONE_WHEN_DIALING)
                        .forEach { key -> runCatching { Settings.System.putInt(resolver, key, 0) } }
                }
                true
            }
            SaverStep.BRIGHTNESS -> setBrightness(10).also { setAutoRotate(false) }
            SaverStep.TIMEOUT -> setScreenTimeout(5_000)
            SaverStep.VIBRATION -> canWriteSettings() && Settings.System.putInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) && Settings.System.putInt(resolver, "vibrate_when_ringing", 0)
            SaverStep.BACKGROUND -> processes.killBackground() >= 0
        }
    }.getOrDefault(false)

    private fun read(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val percent = level * 100 / scale
        val chargeCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = if (chargeCounter > 0 && percent > 0) (chargeCounter / 1000f / (percent / 100f)).toInt() else null
        val chargeTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager.computeChargeTimeRemaining().takeIf { it > 0 }
        } else {
            null
        }
        return BatteryInfo(
            level = percent,
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN),
            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0),
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN),
            temperatureC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f,
            voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0),
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).orEmpty(),
            estimatedCapacityMah = capacity,
            chargeTimeRemainingMs = chargeTime
        )
    }
}
