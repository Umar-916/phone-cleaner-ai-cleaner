package com.phonecleaner.aicleaner.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.ai.AiCleanupScanner
import com.phonecleaner.aicleaner.data.ai.AiEvent
import com.phonecleaner.aicleaner.data.ai.AiPhase
import com.phonecleaner.aicleaner.data.ai.AiResultStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data class Analyzing(val phase: AiPhase, val done: Int, val total: Int) : AiUiState
    data object Result : AiUiState
}

class AiCleanupViewModel(private val scanner: AiCleanupScanner) : ViewModel() {

    /** Survives navigation: true once the scan reveal animation has played, so returning to the screen does not replay it. */
    var revealed by androidx.compose.runtime.mutableStateOf(false)

    private val _state = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        _state.value = AiUiState.Analyzing(AiPhase.COLLECTING, 0, 0)
        job = viewModelScope.launch {
            scanner.analyze().collect { event ->
                _state.value = when (event) {
                    is AiEvent.Progress -> AiUiState.Analyzing(event.phase, event.done, event.total)
                    is AiEvent.Finished -> {
                        AiResultStore.set(event.groups)
                        AiUiState.Result
                    }
                }
            }
        }
    }


    fun reset() {
        job?.cancel()
        _state.value = AiUiState.Idle
    }

    override fun onCleared() {
        job?.cancel()
    }
}
