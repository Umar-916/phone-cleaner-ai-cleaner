package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.battery.BatteryInfo
import com.phonecleaner.aicleaner.data.battery.BatteryRepository
import com.phonecleaner.aicleaner.data.battery.VolumeLevel
import com.phonecleaner.aicleaner.data.battery.VolumeStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BatteryUiState(
    val battery: BatteryInfo = BatteryInfo(),
    val canWriteSettings: Boolean = false,
    val screenTimeoutMs: Int = 0,
    val brightness: Int = 0,
    val autoBrightness: Boolean = false,
    val volumes: List<VolumeLevel> = emptyList(),
    val ringerMode: Int = 0,
    val hasVibrator: Boolean = false
)

class BatteryViewModel(private val repository: BatteryRepository) : ViewModel() {

    private val _state = MutableStateFlow(BatteryUiState(hasVibrator = repository.hasVibrator()))
    val state: StateFlow<BatteryUiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    fun start() {
        refreshControls()
        if (observeJob?.isActive == true) return
        observeJob = viewModelScope.launch {
            repository.observe().collect { info -> _state.update { it.copy(battery = info) } }
        }
    }

    fun stop() {
        observeJob?.cancel()
    }

    fun refreshControls() {
        _state.update {
            it.copy(
                canWriteSettings = repository.canWriteSettings(),
                screenTimeoutMs = repository.screenTimeoutMs(),
                brightness = repository.brightness(),
                autoBrightness = repository.isAutoBrightness(),
                volumes = repository.volumes(),
                ringerMode = repository.ringerMode()
            )
        }
    }

    /** Returns false when the system settings permission is missing, so the screen can ask for it. */
    fun setScreenTimeout(ms: Int): Boolean {
        val done = repository.setScreenTimeout(ms)
        if (done) TaskTracker.mark(MainTask.BATTERY)
        refreshControls()
        return done
    }

    fun setBrightness(value: Int): Boolean {
        val done = repository.setBrightness(value)
        if (done) TaskTracker.mark(MainTask.BATTERY)
        _state.update { it.copy(brightness = repository.brightness(), autoBrightness = repository.isAutoBrightness()) }
        return done
    }

    fun setVolume(stream: VolumeStream, value: Int) {
        repository.setVolume(stream, value)
        _state.update { it.copy(volumes = repository.volumes(), ringerMode = repository.ringerMode()) }
    }

    fun markBatteryTask() = TaskTracker.mark(MainTask.BATTERY)

    fun testVibration() = repository.testVibration()

    override fun onCleared() {
        observeJob?.cancel()
    }
}
