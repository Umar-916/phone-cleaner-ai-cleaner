package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.ai.AiCleanupScanner
import com.phonecleaner.aicleaner.data.ai.AiEvent
import com.phonecleaner.aicleaner.data.ai.AiGroup
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.data.ai.AiPhase
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiResult(val groups: List<AiGroup>, val selected: Set<String>) {
    val selectedEntries: List<FileEntry> get() = groups.flatMap { it.files }.filter { it.path in selected }
    val selectedBytes: Long get() = selectedEntries.sumOf { it.sizeBytes }
    val totalBytes: Long get() = groups.sumOf { it.bytes }
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data class Analyzing(val phase: AiPhase, val done: Int, val total: Int) : AiUiState
    data class Result(val data: AiResult) : AiUiState
    data class Cleaning(val done: Int, val total: Int) : AiUiState
    data class Done(val freedBytes: Long, val removed: Int) : AiUiState
}

class AiCleanupViewModel(
    private val scanner: AiCleanupScanner,
    private val files: FilesRepository
) : ViewModel() {

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
                    is AiEvent.Finished -> AiUiState.Result(
                        AiResult(
                            groups = event.groups,
                            selected = event.groups
                                .filter { it.kind == AiKind.SCREENSHOTS || it.kind == AiKind.OLD_DOWNLOADS }
                                .flatMap { g -> g.files.map { it.path } }.toSet()
                        )
                    )
                }
            }
        }
    }

    fun toggle(file: FileEntry) = update { data ->
        data.copy(selected = if (file.path in data.selected) data.selected - file.path else data.selected + file.path)
    }

    fun toggleGroup(group: AiGroup) = update { data ->
        val paths = group.files.map { it.path }
        data.copy(selected = if (data.selected.containsAll(paths)) data.selected - paths.toSet() else data.selected + paths)
    }

    fun cleanSelected() {
        val data = (_state.value as? AiUiState.Result)?.data ?: return
        val targets = data.selectedEntries
        if (targets.isEmpty() || job?.isActive == true) return
        _state.value = AiUiState.Cleaning(0, targets.size)
        job = viewModelScope.launch {
            val removed = files.delete(targets)
            _state.value = AiUiState.Done(removed.sumOf { it.sizeBytes }, removed.size)
        }
    }

    fun reset() {
        job?.cancel()
        _state.value = AiUiState.Idle
    }

    private fun update(transform: (AiResult) -> AiResult) {
        _state.update { current -> if (current is AiUiState.Result) current.copy(data = transform(current.data)) else current }
    }

    override fun onCleared() {
        job?.cancel()
    }
}
