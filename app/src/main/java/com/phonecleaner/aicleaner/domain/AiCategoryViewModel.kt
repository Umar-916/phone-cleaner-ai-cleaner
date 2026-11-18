package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.ai.AiKind
import com.phonecleaner.aicleaner.data.ai.AiResultStore
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiCategoryPhase {
    data object Browsing : AiCategoryPhase
    data object Cleaning : AiCategoryPhase
    data class Done(val freedBytes: Long, val removed: Int) : AiCategoryPhase
}

data class AiCategoryState(
    val kind: AiKind? = null,
    val items: List<FileEntry> = emptyList(),
    val selected: Set<String> = emptySet(),
    val phase: AiCategoryPhase = AiCategoryPhase.Browsing
) {
    val isSelecting: Boolean get() = selected.isNotEmpty()
    val selectedBytes: Long get() = items.filter { it.path in selected }.sumOf { it.sizeBytes }
    val allSelected: Boolean get() = items.isNotEmpty() && selected.size == items.size
}

class AiCategoryViewModel(private val files: FilesRepository) : ViewModel() {

    private val _state = MutableStateFlow(AiCategoryState())
    val state: StateFlow<AiCategoryState> = _state.asStateFlow()

    fun load(kind: AiKind) {
        if (_state.value.kind == kind) return
        _state.value = AiCategoryState(kind = kind, items = AiResultStore.group(kind)?.files.orEmpty())
    }

    fun toggle(entry: FileEntry) = _state.update {
        it.copy(selected = if (entry.path in it.selected) it.selected - entry.path else it.selected + entry.path)
    }

    fun toggleAll() = _state.update {
        it.copy(selected = if (it.allSelected) emptySet() else it.items.map { entry -> entry.path }.toSet())
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun deleteSelected() {
        val current = _state.value
        val targets = current.items.filter { it.path in current.selected }
        if (targets.isEmpty() || current.phase != AiCategoryPhase.Browsing) return
        _state.update { it.copy(phase = AiCategoryPhase.Cleaning) }
        viewModelScope.launch {
            val removed = files.delete(targets)
            val paths = removed.map { it.path }.toSet()
            AiResultStore.remove(paths)
            _state.update {
                it.copy(
                    items = it.items.filter { entry -> entry.path !in paths },
                    selected = emptySet(),
                    phase = AiCategoryPhase.Done(removed.sumOf { entry -> entry.sizeBytes }, removed.size)
                )
            }
        }
    }
}
