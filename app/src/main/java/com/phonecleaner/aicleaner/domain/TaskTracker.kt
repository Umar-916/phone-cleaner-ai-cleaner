package com.phonecleaner.aicleaner.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MainTask { JUNK, PROCESS, BATTERY }

enum class ExtraTask { AI, WHATSAPP, DUPLICATES }

object TaskTracker {
    private val _done = MutableStateFlow<Set<MainTask>>(emptySet())
    val done: StateFlow<Set<MainTask>> = _done.asStateFlow()

    private val _extras = MutableStateFlow<Set<ExtraTask>>(emptySet())
    val extras: StateFlow<Set<ExtraTask>> = _extras.asStateFlow()

    fun mark(task: MainTask) = _done.update { it + task }

    fun markExtra(task: ExtraTask) = _extras.update { it + task }

    fun completesAll(main: MainTask? = null, extra: ExtraTask? = null): Boolean {
        val m = _done.value + listOfNotNull(main)
        val e = _extras.value + listOfNotNull(extra)
        return MainTask.PROCESS in m && MainTask.BATTERY in m && e.containsAll(ExtraTask.entries)
    }
}
