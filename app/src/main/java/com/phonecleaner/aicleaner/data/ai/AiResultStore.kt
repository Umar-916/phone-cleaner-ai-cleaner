package com.phonecleaner.aicleaner.data.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AiResultStore {
    private val _groups = MutableStateFlow<List<AiGroup>>(emptyList())
    val groups: StateFlow<List<AiGroup>> = _groups.asStateFlow()

    fun set(groups: List<AiGroup>) {
        _groups.value = groups
    }

    fun remove(paths: Set<String>) {
        _groups.update { current ->
            current.map { it.copy(files = it.files.filter { file -> file.path !in paths }) }.filter { it.files.isNotEmpty() }
        }
    }

    fun group(kind: AiKind): AiGroup? = _groups.value.firstOrNull { it.kind == kind }
}
