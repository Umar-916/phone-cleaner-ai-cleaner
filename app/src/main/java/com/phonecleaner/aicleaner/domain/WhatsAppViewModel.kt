package com.phonecleaner.aicleaner.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.whatsapp.WhatsAppScanner
import com.phonecleaner.aicleaner.domain.model.WaCategory
import com.phonecleaner.aicleaner.domain.model.WaFile
import com.phonecleaner.aicleaner.domain.model.WaFilter
import com.phonecleaner.aicleaner.domain.model.WaTab
import com.phonecleaner.aicleaner.domain.model.hasSentTab
import com.phonecleaner.aicleaner.domain.model.WaScanEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WaData(
    val files: List<WaFile>,
    val selected: Set<String> = emptySet(),
    val openCategory: WaCategory? = null,
    val filter: WaFilter = WaFilter.ALL,
    val tab: WaTab = WaTab.RECEIVED
) {
    val isSelecting: Boolean get() = selected.isNotEmpty()

    val byCategory: Map<WaCategory, List<WaFile>> by lazy {
        files.groupBy { it.category }
    }

    val visibleFiles: List<WaFile>
        get() {
            val now = System.currentTimeMillis()
            val category = openCategory
            return byCategory[category].orEmpty()
                .filter { it.matches(filter, now) }
                .filter { category?.hasSentTab != true || it.isSent == (tab == WaTab.SENT) }
                .sortedByDescending { it.sizeBytes }
        }

    val selectedFiles: List<WaFile> get() = files.filter { it.path in selected }
    val selectedBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }
    val totalBytes: Long get() = files.sumOf { it.sizeBytes }
}

sealed interface WaUiState {
    data object Idle : WaUiState
    data class Scanning(val count: Int, val bytes: Long) : WaUiState
    data object NotFound : WaUiState
    data class Result(val data: WaData) : WaUiState
    data class Cleaning(val done: Int, val total: Int) : WaUiState
    data class Done(val freedBytes: Long, val removedCount: Int) : WaUiState
}

class WhatsAppViewModel(private val scanner: WhatsAppScanner) : ViewModel() {

    /** Survives navigation: true once the scan reveal animation has played, so returning to the screen does not replay it. */
    var revealed by androidx.compose.runtime.mutableStateOf(false)

    private val _state = MutableStateFlow<WaUiState>(WaUiState.Idle)
    val state: StateFlow<WaUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun startScan() {
        if (job?.isActive == true) return
        _state.value = WaUiState.Scanning(0, 0L)
        job = viewModelScope.launch {
            scanner.scan().collect { event ->
                _state.value = when (event) {
                    is WaScanEvent.Progress -> WaUiState.Scanning(event.count, event.bytes)
                    is WaScanEvent.Finished ->
                        if (!event.foundFolders || event.files.isEmpty()) WaUiState.NotFound
                        else WaUiState.Result(WaData(event.files))
                }
            }
        }
    }

    fun openCategory(category: WaCategory?) = updateData { it.copy(openCategory = category, filter = WaFilter.ALL, tab = WaTab.RECEIVED, selected = emptySet()) }

    fun setTab(tab: WaTab) = updateData { it.copy(tab = tab, selected = emptySet()) }

    fun setFilter(filter: WaFilter) = updateData { it.copy(filter = filter) }

    fun toggle(file: WaFile) = updateData {
        it.copy(selected = if (file.path in it.selected) it.selected - file.path else it.selected + file.path)
    }

    fun toggleVisible() = updateData { data ->
        val paths = data.visibleFiles.map { it.path }
        data.copy(selected = if (data.selected.containsAll(paths)) data.selected - paths.toSet() else data.selected + paths)
    }

    fun toggleCategory(category: WaCategory) = updateData { data ->
        val paths = data.byCategory[category].orEmpty().map { it.path }
        data.copy(selected = if (data.selected.containsAll(paths)) data.selected - paths.toSet() else data.selected + paths)
    }

    fun clearSelection() = updateData { it.copy(selected = emptySet()) }

    fun thumbnail(file: WaFile, sizePx: Int) = scanner.thumbnail(file, sizePx)

    fun cleanSelected() {
        val data = (_state.value as? WaUiState.Result)?.data ?: return
        val targets = data.selectedFiles
        if (targets.isEmpty() || job?.isActive == true) return
        _state.value = WaUiState.Cleaning(0, targets.size)
        job = viewModelScope.launch {
            val outcome = scanner.delete(targets) { done -> _state.value = WaUiState.Cleaning(done, targets.size) }
            _state.value = WaUiState.Done(outcome.freedBytes, outcome.removedCount)
        }
    }

    fun reset() {
        job?.cancel()
        _state.value = WaUiState.Idle
    }

    private fun updateData(transform: (WaData) -> WaData) {
        _state.update { current ->
            if (current is WaUiState.Result) current.copy(data = transform(current.data)) else current
        }
    }

    override fun onCleared() {
        job?.cancel()
    }
}
