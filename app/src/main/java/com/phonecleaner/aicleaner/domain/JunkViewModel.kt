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
    data class Scanning(val progress: ScanProgress, val items: List<JunkItem> = emptyList()) : JunkUiState
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
    data class Cleaning(
        val done: Int,
        val total: Int,
        val doneBytes: Long = 0L,
        val totalBytes: Long = 0L,
        val groupBytes: Map<JunkGroup, Long> = emptyMap(),
        val targets: List<JunkItem> = emptyList()
    ) : JunkUiState
    data class Done(val freedBytes: Long, val removedCount: Int) : JunkUiState
}

class JunkViewModel(private val scanner: JunkScanner) : ViewModel() {

    private companion object { const val INTRO_MS = 2000L
        const val REPEAT_INTRO_MS = 600L
        const val MIN_SCAN_MS = 6500L
        const val MIN_CLEAN_MS = 6500L
        const val MIN_REVEAL_MS = 2500L
        const val REVEAL_LIMIT = 45 }

    private val _state = MutableStateFlow<JunkUiState>(JunkUiState.Idle)
    val state: StateFlow<JunkUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun startScan() {
        if (job?.isActive == true) return
        _state.value = JunkUiState.Scanning(ScanProgress("", 0L, 0))
        job = viewModelScope.launch {
            kotlinx.coroutines.delay(INTRO_MS)
            val startedAt = android.os.SystemClock.elapsedRealtime()
            scanner.scan().collect { event ->
                when (event) {
                    is ScanEvent.Progress -> {
                        val previous = (_state.value as? JunkUiState.Scanning)?.progress?.fraction ?: 0f
                        _state.value = JunkUiState.Scanning(event.progress.copy(fraction = maxOf(previous, event.progress.fraction)))
                    }
                    is ScanEvent.Finished -> reveal(event.items, startedAt)
                }
            }
        }
    }

    private suspend fun reveal(items: List<JunkItem>, startedAt: Long) {
        val totalBytes = items.sumOf { it.sizeBytes }
        val byCategory = items.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.sizeBytes } }
        val shown = items.sortedBy { item -> JunkGroup.entries.indexOfFirst { item.category in it.categories } }.take(REVEAL_LIMIT)
        val remaining = (MIN_SCAN_MS - (android.os.SystemClock.elapsedRealtime() - startedAt)).coerceAtLeast(MIN_REVEAL_MS)
        val stepDelay = if (shown.isEmpty()) remaining else (remaining / shown.size).coerceIn(95L, 170L)
        for (i in 1..shown.size) {
            val ratio = i.toFloat() / shown.size
            val progress = ScanProgress("", (totalBytes * ratio).toLong(), (items.size * ratio).toInt(), ratio * 0.95f, byCategory.mapValues { (it.value * ratio).toLong() })
            _state.value = JunkUiState.Scanning(progress, shown.take(i))
            kotlinx.coroutines.delay(stepDelay)
        }
        if (shown.isEmpty()) kotlinx.coroutines.delay(remaining)
        _state.value = JunkUiState.Result(
            items = items,
            selected = items.filter { it.category in JunkGroup.AD_JUNK.categories }.map { it.path }.toSet()
        )
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
        val totalBytes = targets.sumOf { it.sizeBytes }
        val groupBytes = JunkGroup.entries.associateWith { g -> targets.filter { it.category in g.categories }.sumOf { it.sizeBytes } }
        _state.value = JunkUiState.Cleaning(0, targets.size, 0L, totalBytes, groupBytes, targets)
        val cleanStart = android.os.SystemClock.elapsedRealtime()
        job = viewModelScope.launch {
            val outcome = scanner.delete(targets) { done ->
                _state.value = JunkUiState.Cleaning(done, targets.size, targets.take(done).sumOf { it.sizeBytes }, totalBytes, groupBytes, targets)
            }
            val rest = MIN_CLEAN_MS - (android.os.SystemClock.elapsedRealtime() - cleanStart)
            if (rest > 0) kotlinx.coroutines.delay(rest)
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

object JunkIntro {
    @Volatile var radarPlayed = false
}
