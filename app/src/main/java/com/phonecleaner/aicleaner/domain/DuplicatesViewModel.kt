package com.phonecleaner.aicleaner.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

enum class DuplicateTab { PHOTOS, VIDEOS, FILES }

data class DuplicatesData(
    val photoGroups: List<DuplicateGroup>,
    val videoGroups: List<DuplicateGroup>,
    val fileGroups: List<DuplicateGroup>,
    val selected: Set<Long>,
    val keepers: Map<String, Long>,
    val tab: DuplicateTab = DuplicateTab.PHOTOS
) {
    val activeGroups: List<DuplicateGroup>
        get() = when (tab) {
            DuplicateTab.PHOTOS -> photoGroups
            DuplicateTab.VIDEOS -> videoGroups
            DuplicateTab.FILES -> fileGroups
        }

    val isEmpty: Boolean get() = photoGroups.isEmpty() && videoGroups.isEmpty() && fileGroups.isEmpty()

    private val allFiles: Map<Long, MediaFile> by lazy {
        (photoGroups + videoGroups + fileGroups).flatMap { it.files }.associateBy { it.id }
    }

    val selectedFiles: List<MediaFile> get() = selected.mapNotNull { allFiles[it] }

    private val activeIds: Set<Long> get() = activeGroups.flatMap { g -> g.files.map { it.id } }.toSet()

    /** Selection restricted to the tab on screen: each tab is cleaned on its own. */
    val tabSelected: Set<Long> get() = selected.intersect(activeIds)
    val tabSelectedFiles: List<MediaFile> get() = tabSelected.mapNotNull { allFiles[it] }
    val tabSelectedBytes: Long get() = tabSelectedFiles.sumOf { it.sizeBytes }

    fun keeperOf(group: DuplicateGroup): Long = keepers[group.id] ?: group.files.first().id
}

sealed interface DuplicatesUiState {
    data object Idle : DuplicatesUiState
    data class Scanning(val progress: DuplicateProgress) : DuplicatesUiState
    data class Result(val data: DuplicatesData) : DuplicatesUiState
    /** [remaining] is what is left in the other tabs, or null when nothing is left to clean. */
    data class Done(val freedBytes: Long, val removedCount: Int, val remaining: DuplicatesData?) : DuplicatesUiState
}

class DuplicatesViewModel(private val scanner: DuplicateScanner) : ViewModel() {

    /** Survives navigation: true once the scan reveal animation has played, so returning to the screen does not replay it. */
    var revealed by androidx.compose.runtime.mutableStateOf(false)

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
        val tabIds = data.activeGroups.flatMap { group ->
            val keeper = data.keeperOf(group)
            group.files.map { it.id }.filter { it != keeper }
        }
        data.copy(selected = data.selected + tabIds)
    }

    fun deselectAll() = updateData { data ->
        val ids = data.activeGroups.flatMap { g -> g.files.map { it.id } }.toSet()
        data.copy(selected = data.selected - ids)
    }

    private var totalFreed = 0L
    private var totalRemoved = 0

    /** Running totals across every tab cleaned in this session, used for the final summary. */
    val sessionFreedBytes: Long get() = totalFreed
    val sessionRemovedCount: Int get() = totalRemoved

    /** Deletes only what is selected in the tab on screen; the other tabs are cleaned separately. */
    fun deleteSelected() {
        val data = (_state.value as? DuplicatesUiState.Result)?.data ?: return
        val targets = data.tabSelectedFiles
        viewModelScope.launch {
            val removed = scanner.delete(targets).toSet()
            onDeleted(data, targets, removed)
        }
    }

    private fun onDeleted(data: DuplicatesData, targets: List<MediaFile>, removed: Set<Long>) {
        val freed = targets.filter { it.id in removed }.sumOf { it.sizeBytes }
        totalFreed += freed
        totalRemoved += removed.size

        fun List<DuplicateGroup>.without(): List<DuplicateGroup> =
            mapNotNull { g ->
                val left = g.files.filter { it.id !in removed }
                if (left.size > 1) g.copy(files = left) else null
            }

        val photos = data.photoGroups.without()
        val videos = data.videoGroups.without()
        val files = data.fileGroups.without()
        val nextTab = when {
            (if (data.tab == DuplicateTab.PHOTOS) photos else if (data.tab == DuplicateTab.VIDEOS) videos else files).isNotEmpty() -> data.tab
            photos.isNotEmpty() -> DuplicateTab.PHOTOS
            videos.isNotEmpty() -> DuplicateTab.VIDEOS
            files.isNotEmpty() -> DuplicateTab.FILES
            else -> data.tab
        }
        val remaining = if (photos.isEmpty() && videos.isEmpty() && files.isEmpty()) null
        else data.copy(photoGroups = photos, videoGroups = videos, fileGroups = files, selected = data.selected - removed, tab = nextTab)
        _state.value = DuplicatesUiState.Done(freed, removed.size, remaining)
    }

    /** After the Done check: go back to the list (next tab that still has duplicates). */
    fun continueWith(data: DuplicatesData) {
        _state.value = DuplicatesUiState.Result(data)
    }

    fun reset() {
        job?.cancel()
        totalFreed = 0L
        totalRemoved = 0
        _state.value = DuplicatesUiState.Idle
    }

    private fun initialData(result: DuplicateScanResult): DuplicatesData {
        val groups = result.photoGroups + result.videoGroups + result.fileGroups
        val selected = groups.flatMap { g -> g.files.drop(1).map { it.id } }.toSet()
        val firstTab = when {
            result.photoGroups.isNotEmpty() -> DuplicateTab.PHOTOS
            result.videoGroups.isNotEmpty() -> DuplicateTab.VIDEOS
            else -> DuplicateTab.FILES
        }
        return DuplicatesData(result.photoGroups, result.videoGroups, result.fileGroups, selected, emptyMap(), firstTab)
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
