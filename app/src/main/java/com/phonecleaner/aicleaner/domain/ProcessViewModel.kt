package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.process.ProcessRepository
import com.phonecleaner.aicleaner.domain.model.BoostResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BoostState {
    data object Idle : BoostState
    data class Running(val percent: Int = 0, val label: String = "") : BoostState
    data class Done(val result: BoostResult) : BoostState
}

class ProcessViewModel(private val repository: ProcessRepository) : ViewModel() {

    private val _state = MutableStateFlow<BoostState>(BoostState.Idle)
    val state: StateFlow<BoostState> = _state.asStateFlow()

    private var job: Job? = null

    fun boost() {
        if (job?.isActive == true || _state.value is BoostState.Done) return
        _state.value = BoostState.Running()
        job = viewModelScope.launch {
            val result = repository.boost { percent, label ->
                _state.update { current -> if (current is BoostState.Running) BoostState.Running(percent, label) else current }
            }
            _state.value = BoostState.Done(result)
            TaskTracker.mark(MainTask.PROCESS)
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        _state.value = BoostState.Idle
    }

    override fun onCleared() {
        job?.cancel()
    }
}
