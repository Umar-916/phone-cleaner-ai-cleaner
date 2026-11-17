package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.data.files.FilesRepository
import com.phonecleaner.aicleaner.domain.model.BrowserItem
import com.phonecleaner.aicleaner.domain.model.CategorySummary
import com.phonecleaner.aicleaner.domain.model.DocumentType
import com.phonecleaner.aicleaner.domain.model.FileCategory
import com.phonecleaner.aicleaner.domain.model.FileEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class FilesOverviewState(
    val storage: StorageInfo = StorageInfo(),
    val summaries: Map<FileCategory, CategorySummary> = emptyMap(),
    val isLoading: Boolean = false
)

class FilesViewModel(
    private val repository: FilesRepository,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(FilesOverviewState(storage = repository.storage()))
    val state: StateFlow<FilesOverviewState> = _state.asStateFlow()

    fun refresh(withCategories: Boolean) {
        _state.update { it.copy(storage = repository.storage(), isLoading = withCategories) }
        if (!withCategories) return
        viewModelScope.launch {
            val summaries = repository.summaries(dataStore.showHiddenFilesFlow.first())
            _state.update { it.copy(summaries = summaries, isLoading = false) }
        }
    }
}

enum class FileViewMode { ALL, ALBUM }

data class FileCategoryState(
    val category: FileCategory = FileCategory.IMAGES,
    val isLoading: Boolean = true,
    val entries: List<FileEntry> = emptyList(),
    val query: String = "",
    val searching: Boolean = false,
    val viewMode: FileViewMode = FileViewMode.ALL,
    val documentType: DocumentType = DocumentType.ALL,
    val openAlbum: String? = null,
    val selected: Set<String> = emptySet(),
    val global: Boolean = false
) {
    val visible: List<FileEntry>
        get() = entries
            .filter { openAlbum == null || it.folderName == openAlbum }
            .filter { documentType == DocumentType.ALL || it.extension in documentType.extensions }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }

    val albums: List<Pair<String, List<FileEntry>>>
        get() = entries.groupBy { it.folderName }.toList().sortedByDescending { it.second.size }

    val selectedEntries: List<FileEntry> get() = entries.filter { it.path in selected }
    val isSelecting: Boolean get() = selected.isNotEmpty()
}

class FileCategoryViewModel(
    private val repository: FilesRepository,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(FileCategoryState())
    val state: StateFlow<FileCategoryState> = _state.asStateFlow()

    fun load(category: FileCategory) {
        _state.value = FileCategoryState(category = category, isLoading = true)
        viewModelScope.launch {
            val entries = repository.list(category, dataStore.showHiddenFilesFlow.first())
            _state.update { it.copy(entries = entries, isLoading = false) }
        }
    }

    fun loadAll() {
        _state.value = FileCategoryState(global = true, searching = true, isLoading = true)
        viewModelScope.launch {
            val hidden = dataStore.showHiddenFilesFlow.first()
            val entries = FileCategory.entries.flatMap { repository.list(it, hidden) }.distinctBy { it.path }.sortedByDescending { it.modifiedMs }
            _state.update { it.copy(entries = entries, isLoading = false) }
        }
    }

    fun setViewMode(mode: FileViewMode) = _state.update { it.copy(viewMode = mode, openAlbum = null) }
    fun setDocumentType(type: DocumentType) = _state.update { it.copy(documentType = type) }
    fun openAlbum(name: String?) = _state.update { it.copy(openAlbum = name, viewMode = if (name == null) FileViewMode.ALBUM else FileViewMode.ALL) }
    fun setSearching(enabled: Boolean) = _state.update { it.copy(searching = enabled, query = if (enabled) it.query else "") }
    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    fun toggleSelect(entry: FileEntry) = _state.update {
        it.copy(selected = if (entry.path in it.selected) it.selected - entry.path else it.selected + entry.path)
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }
    fun selectAllVisible() = _state.update { it.copy(selected = it.visible.map { entry -> entry.path }.toSet()) }

    fun delete(targets: List<FileEntry>, onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val removed = repository.delete(targets).map { it.path }.toSet()
            _state.update { it.copy(entries = it.entries.filter { entry -> entry.path !in removed }, selected = it.selected - removed) }
            onDone(removed.size)
        }
    }
}

enum class BrowserSort { NAME, SIZE, DATE }

data class BrowserState(
    val directory: String = "",
    val items: List<BrowserItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRoot: Boolean = true,
    val selected: Set<String> = emptySet(),
    val sort: BrowserSort = BrowserSort.NAME,
    val showHidden: Boolean = false,
    val nameClash: Boolean = false
) {
    val isSelecting: Boolean get() = selected.isNotEmpty()
}

class StorageBrowserViewModel(
    private val repository: FilesRepository,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    private val root = repository.rootDirectory()

    fun open(path: String? = null) {
        val directory = path?.let { File(it) } ?: root
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val showHidden = dataStore.showHiddenFilesFlow.first()
            val items = repository.browse(directory, showHidden)
            _state.update {
                it.copy(
                    directory = directory.absolutePath,
                    items = sorted(items, it.sort),
                    isLoading = false,
                    isRoot = directory == root,
                    selected = emptySet(),
                    showHidden = showHidden,
                    nameClash = false
                )
            }
        }
    }

    fun up(): Boolean {
        val current = _state.value
        if (current.isRoot || current.directory.isEmpty()) return false
        open(File(current.directory).parent)
        return true
    }

    fun toggleSelect(item: BrowserItem) = _state.update {
        it.copy(selected = if (item.path in it.selected) it.selected - item.path else it.selected + item.path)
    }

    fun selectAll() = _state.update { it.copy(selected = it.items.map { item -> item.path }.toSet()) }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun setSort(sort: BrowserSort) = _state.update { it.copy(sort = sort, items = sorted(it.items, sort)) }

    fun toggleHidden() {
        viewModelScope.launch {
            dataStore.setShowHiddenFiles(!_state.value.showHidden)
            open(_state.value.directory)
        }
    }

    fun createFolder(name: String, onResult: (Boolean) -> Unit) {
        val clean = name.trim().trim('/')
        if (clean.isEmpty()) return onResult(false)
        viewModelScope.launch {
            val created = repository.createFolder(File(_state.value.directory), clean)
            if (created) open(_state.value.directory)
            onResult(created)
        }
    }

    fun delete(item: BrowserItem) = deletePaths(listOf(item.path))

    fun deleteSelected() = deletePaths(_state.value.selected.toList())

    private fun deletePaths(paths: List<String>) {
        viewModelScope.launch {
            repository.deletePaths(paths)
            open(_state.value.directory)
        }
    }

    private fun sorted(items: List<BrowserItem>, sort: BrowserSort): List<BrowserItem> {
        val folderFirst = compareByDescending<BrowserItem> { it.isDirectory }
        return when (sort) {
            BrowserSort.NAME -> items.sortedWith(folderFirst.thenBy { it.name.lowercase() })
            BrowserSort.SIZE -> items.sortedWith(folderFirst.thenByDescending { it.sizeBytes }.thenBy { it.name.lowercase() })
            BrowserSort.DATE -> items.sortedWith(folderFirst.thenByDescending { it.modifiedMs })
        }
    }
}
