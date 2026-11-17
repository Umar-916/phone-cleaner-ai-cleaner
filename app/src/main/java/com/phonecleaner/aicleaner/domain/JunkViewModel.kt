package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.scanner.JunkScanner
import com.phonecleaner.aicleaner.domain.model.JunkCategory
import com.phonecleaner.aicleaner.domain.model.JunkItem
import com.phonecleaner.aicleaner.domain.model.ScanEvent
import com.phonecleaner.aicleaner.domain.model.ScanProgress
import com.phonecleaner.aicleaner.domain.model.JunkGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface JunkUiState {
    data object Idle : JunkUiState
    data class Scanning(val progress: ScanProgress) : JunkUiState
    data class Result(val items: List<JunkItem>, val selected: Set<String>) : JunkUiState {
        val grouped: Map<JunkCategory, List<JunkItem>> by lazy {
            items.groupBy { it.category }.toSortedMap(compareBy { it.ordinal })
        }
        val groups: Map<JunkGroup, List<JunkItem>> by lazy {
            JunkGroup.entries.associateWith { group -> items.filter { it.category in group.categories } }
        }
        val selectedItems: List<JunkItem> get() = items.filter { it.path in selected }
        val selectedBytes: Long get() = selectedItems.sumOf { it.sizeBytes }
        val totalBytes: Long get() = items.sumOf { it.sizeBytes }
    }
    data class Cleaning(val done: Int, val total: Int) : JunkUiState
    data class Done(val freedBytes: Long, val removedCount: Int) : JunkUiState
}

class JunkViewModel(private val scanner: JunkScanner) : ViewModel() {

    private val _state = MutableStateFlow<JunkUiState>(JunkUiState.Idle)
    val state: StateFlow<JunkUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun startScan() {
        if (job?.isActive == true) return
        _state.value = JunkUiState.Scanning(ScanProgress("", 0L, 0))
        job = viewModelScope.launch {
            scanner.scan().collect { event ->
                when (event) {
                    is ScanEvent.Progress -> {
                        val previous = (_state.value as? JunkUiState.Scanning)?.progress?.fraction ?: 0f
                        _state.value = JunkUiState.Scanning(event.progress.copy(fraction = maxOf(previous, event.progress.fraction)))
                    }
                    is ScanEvent.Finished -> _state.value = JunkUiState.Result(
                        items = event.items,
                        selected = event.items.filter { it.category.selectedByDefault }.map { it.path }.toSet()
                    )
                }
            }
        }
    }

    fun toggleItem(item: JunkItem) = updateSelection { _, selected ->
        if (item.path in selected) selected - item.path else selected + item.path
    }

    fun toggleCategory(category: JunkCategory) = updateSelection { result, selected ->
        val paths = result.items.filter { it.category == category }.map { it.path }
        if (selected.containsAll(paths)) selected - paths.toSet() else selected + paths
    }

    fun toggleGroup(group: JunkGroup) = updateSelection { result, selected ->
        val paths = result.items.filter { it.category in group.categories }.map { it.path }
        if (paths.isNotEmpty() && selected.containsAll(paths)) selected - paths.toSet() else selected + paths
    }

    fun setAllSelected(selectAll: Boolean) = updateSelection { result, _ ->
        if (selectAll) result.items.map { it.path }.toSet() else emptySet()
    }

    fun cleanSelected() {
        val result = _state.value as? JunkUiState.Result ?: return
        val targets = result.selectedItems
        if (targets.isEmpty() || job?.isActive == true) return
        _state.value = JunkUiState.Cleaning(0, targets.size)
        job = viewModelScope.launch {
            val outcome = scanner.delete(targets) { done ->
                _state.value = JunkUiState.Cleaning(done, targets.size)
            }
            _state.value = JunkUiState.Done(outcome.freedBytes, outcome.removedCount)
            TaskTracker.mark(MainTask.JUNK)
        }
    }

    fun reset() {
        job?.cancel()
        _state.value = JunkUiState.Idle
    }

    private fun updateSelection(transform: (JunkUiState.Result, Set<String>) -> Set<String>) {
        _state.update { current ->
            if (current is JunkUiState.Result) current.copy(selected = transform(current, current.selected)) else current
        }
    }

    override fun onCleared() {
        job?.cancel()
    }
}
