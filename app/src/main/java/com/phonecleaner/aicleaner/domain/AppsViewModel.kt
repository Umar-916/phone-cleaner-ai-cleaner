package com.phonecleaner.aicleaner.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.apps.AppsRepository
import com.phonecleaner.aicleaner.domain.model.AppFilter
import com.phonecleaner.aicleaner.domain.model.AppSort
import com.phonecleaner.aicleaner.domain.model.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppsUiState(
    val isLoading: Boolean = true,
    val apps: List<InstalledApp> = emptyList(),
    val totalCount: Int = 0,
    val userCount: Int = 0,
    val systemCount: Int = 0,
    val query: String = "",
    val filter: AppFilter = AppFilter.USER,
    val sort: AppSort = AppSort.NAME
)

class AppsViewModel(private val repository: AppsRepository) : ViewModel() {

    private val allApps = MutableStateFlow<List<InstalledApp>?>(null)
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(AppFilter.USER)
    private val sort = MutableStateFlow(AppSort.NAME)

    val state: StateFlow<AppsUiState> = combine(allApps, query, filter, sort) { apps, q, f, s ->
        if (apps == null) return@combine AppsUiState(query = q, filter = f, sort = s)
        val visible = apps
            .filter { app ->
                when (f) {
                    AppFilter.ALL -> true
                    AppFilter.USER -> !app.isSystem
                    AppFilter.SYSTEM -> app.isSystem
                }
            }
            .filter { q.isBlank() || it.label.contains(q.trim(), ignoreCase = true) || it.packageName.contains(q.trim(), ignoreCase = true) }
            .let { list ->
                when (s) {
                    AppSort.NAME -> list.sortedBy { it.label.lowercase() }
                    AppSort.SIZE -> list.sortedByDescending { it.apkSizeBytes }
                    AppSort.RECENT -> list.sortedByDescending { it.installedAt }
                }
            }
        AppsUiState(
            isLoading = false,
            apps = visible,
            totalCount = apps.size,
            userCount = apps.count { !it.isSystem },
            systemCount = apps.count { it.isSystem },
            query = q,
            filter = f,
            sort = s
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppsUiState())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            allApps.value = repository.loadApps()
            _refreshing.value = false
        }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: AppFilter) {
        filter.value = value
    }

    fun setSort(value: AppSort) {
        sort.value = value
    }
}
