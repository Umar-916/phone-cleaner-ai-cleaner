package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.media.DuplicateScanner
import com.phonecleaner.aicleaner.domain.model.DuplicateGroup
import com.phonecleaner.aicleaner.domain.model.DuplicateProgress
import com.phonecleaner.aicleaner.domain.model.DuplicatePhase
import com.phonecleaner.aicleaner.domain.model.DuplicateScanEvent
import com.phonecleaner.aicleaner.domain.model.DuplicateScanResult
import com.phonecleaner.aicleaner.domain.model.MediaFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DuplicateTab { PHOTOS, VIDEOS }

data class DuplicatesData(
    val photoGroups: List<DuplicateGroup>,
    val videoGroups: List<DuplicateGroup>,
    val selected: Set<Long>,
    val keepers: Map<String, Long>,
    val tab: DuplicateTab = DuplicateTab.PHOTOS
) {
    val activeGroups: List<DuplicateGroup>
        get() = if (tab == DuplicateTab.PHOTOS) photoGroups else videoGroups

    private val allFiles: Map<Long, MediaFile> by lazy {
        (photoGroups + videoGroups).flatMap { it.files }.associateBy { it.id }
    }

    val selectedFiles: List<MediaFile> get() = selected.mapNotNull { allFiles[it] }
    val selectedBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }

    fun keeperOf(group: DuplicateGroup): Long = keepers[group.id] ?: group.files.first().id
}

sealed interface DuplicatesUiState {
    data object Idle : DuplicatesUiState
    data class Scanning(val progress: DuplicateProgress) : DuplicatesUiState
    data class Result(val data: DuplicatesData) : DuplicatesUiState
    data class Done(val freedBytes: Long, val removedCount: Int) : DuplicatesUiState
}

class DuplicatesViewModel(private val scanner: DuplicateScanner) : ViewModel() {

    private val _state = MutableStateFlow<DuplicatesUiState>(DuplicatesUiState.Idle)
    val state: StateFlow<DuplicatesUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun startScan() {
        if (job?.isActive == true) return
        _state.value = DuplicatesUiState.Scanning(DuplicateProgress(DuplicatePhase.READING, 0, 0))
        job = viewModelScope.launch {
            scanner.scan().collect { event ->
                when (event) {
                    is DuplicateScanEvent.Progress -> _state.value = DuplicatesUiState.Scanning(event.progress)
                    is DuplicateScanEvent.Finished -> _state.value = DuplicatesUiState.Result(initialData(event.result))
                }
            }
        }
    }

    fun setTab(tab: DuplicateTab) = updateData { it.copy(tab = tab) }

    fun toggle(file: MediaFile) = updateData { data ->
        val group = data.activeGroups.firstOrNull { g -> g.files.any { it.id == file.id } } ?: return@updateData data
        if (data.keeperOf(group) == file.id) return@updateData data
        data.copy(selected = if (file.id in data.selected) data.selected - file.id else data.selected + file.id)
    }

    fun setKeeper(group: DuplicateGroup, file: MediaFile) = updateData { data ->
        val previous = data.keeperOf(group)
        if (previous == file.id) return@updateData data
        data.copy(
            keepers = data.keepers + (group.id to file.id),
            selected = data.selected - file.id + previous
        )
    }

    fun selectAllButKeepers() = updateData { data ->
        val all = (data.photoGroups + data.videoGroups).flatMap { group ->
            val keeper = data.keeperOf(group)
            group.files.map { it.id }.filter { it != keeper }
        }
        data.copy(selected = all.toSet())
    }

    fun deselectAll() = updateData { it.copy(selected = emptySet()) }

    fun deleteLegacy() {
        val data = (_state.value as? DuplicatesUiState.Result)?.data ?: return
        val targets = data.selectedFiles
        viewModelScope.launch {
            val removed = scanner.deleteLegacy(targets)
            onDeleted(removed.toSet())
        }
    }

    /** Called after the system confirmed the deletion (or the legacy delete finished). */
    fun onDeleted(removedIds: Set<Long>? = null) {
        val data = (_state.value as? DuplicatesUiState.Result)?.data ?: return
        val removed = removedIds ?: data.selected
        val freed = data.selectedFiles.filter { it.id in removed }.sumOf { it.sizeBytes }
        _state.value = DuplicatesUiState.Done(freed, removed.size)
    }

    fun reset() {
        job?.cancel()
        _state.value = DuplicatesUiState.Idle
    }

    private fun initialData(result: DuplicateScanResult): DuplicatesData {
        val groups = result.photoGroups + result.videoGroups
        val selected = groups.flatMap { g -> g.files.drop(1).map { it.id } }.toSet()
        return DuplicatesData(result.photoGroups, result.videoGroups, selected, emptyMap())
    }

    private fun updateData(transform: (DuplicatesData) -> DuplicatesData) {
        _state.update { current ->
            if (current is DuplicatesUiState.Result) current.copy(data = transform(current.data)) else current
        }
    }

    override fun onCleared() {
        job?.cancel()
    }
}
