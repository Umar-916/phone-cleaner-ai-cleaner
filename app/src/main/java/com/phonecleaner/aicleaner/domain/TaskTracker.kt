package com.phonecleaner.aicleaner.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MainTask { JUNK, PROCESS, BATTERY }

/** Remembers which of the three main care tasks were finished during this app session. */
object TaskTracker {
    private val _done = MutableStateFlow<Set<MainTask>>(emptySet())
    val done: StateFlow<Set<MainTask>> = _done.asStateFlow()

    fun mark(task: MainTask) = _done.update { it + task }
}
