package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.process.ProcessRepository
import com.phonecleaner.aicleaner.domain.model.BoostResult
import com.phonecleaner.aicleaner.domain.model.MemoryStats
import com.phonecleaner.aicleaner.domain.model.RecentApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface BoostState {
    data object Idle : BoostState
    data object Running : BoostState
    data class Done(val result: BoostResult) : BoostState
}

data class ProcessUiState(
    val memory: MemoryStats = MemoryStats(),
    val boost: BoostState = BoostState.Idle,
    val hasUsageAccess: Boolean = false,
    val recentApps: List<RecentApp> = emptyList(),
    val recentLoaded: Boolean = false
)

class ProcessViewModel(private val repository: ProcessRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProcessUiState(memory = repository.memory()))
    val state: StateFlow<ProcessUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(memory = repository.memory()) }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
    }

    fun refreshUsage() {
        viewModelScope.launch {
            val access = repository.hasUsageAccess()
            val apps = if (access) repository.recentApps() else emptyList()
            _state.update { it.copy(hasUsageAccess = access, recentApps = apps, recentLoaded = true) }
        }
    }

    fun boost() {
        if (_state.value.boost == BoostState.Running) return
        _state.update { it.copy(boost = BoostState.Running) }
        viewModelScope.launch {
            val result = repository.boost()
            _state.update { it.copy(boost = BoostState.Done(result), memory = repository.memory()) }
            TaskTracker.mark(MainTask.PROCESS)
        }
    }

    fun resetBoost() {
        _state.update { it.copy(boost = BoostState.Idle) }
    }

    override fun onCleared() {
        pollJob?.cancel()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2000L
    }
}
