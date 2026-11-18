package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.process.ProcessRepository
import com.phonecleaner.aicleaner.domain.model.BoostResult
import com.phonecleaner.aicleaner.domain.model.MemoryStats
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
    data class Running(val percent: Int = 0, val label: String = "") : BoostState
    data class Done(val result: BoostResult) : BoostState
}

data class ProcessUiState(
    val memory: MemoryStats = MemoryStats(),
    val boost: BoostState = BoostState.Idle
)

class ProcessViewModel(private val repository: ProcessRepository) : ViewModel() {

    private val _state = MutableStateFlow(ProcessUiState(memory = repository.memory()))
    val state: StateFlow<ProcessUiState> = _state.asStateFlow()

    private var pollJob: Job? = null
    private var boostJob: Job? = null

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

    fun boost() {
        if (boostJob?.isActive == true || _state.value.boost is BoostState.Done) return
        _state.update { it.copy(boost = BoostState.Running()) }
        boostJob = viewModelScope.launch {
            val result = repository.boost { percent, label ->
                _state.update { current ->
                    if (current.boost is BoostState.Running) current.copy(boost = BoostState.Running(percent, label)) else current
                }
            }
            _state.update { it.copy(boost = BoostState.Done(result), memory = repository.memory()) }
            TaskTracker.mark(MainTask.PROCESS)
        }
    }

    fun resetBoost() {
        boostJob?.cancel()
        boostJob = null
        _state.update { it.copy(boost = BoostState.Idle) }
    }

    override fun onCleared() {
        pollJob?.cancel()
        boostJob?.cancel()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2000L
    }
}
