package com.phonecleaner.aicleaner.domain.model

import android.net.Uri

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val addedAtMs: Long,
    val isVideo: Boolean,
    val width: Int,
    val height: Int,
    val durationMs: Long
)

enum class DuplicateKind { EXACT, SIMILAR }

data class DuplicateGroup(
    val id: String,
    val kind: DuplicateKind,
    val isVideo: Boolean,
    val files: List<MediaFile>
)

data class DuplicateScanResult(
    val photoGroups: List<DuplicateGroup>,
    val videoGroups: List<DuplicateGroup>
)

enum class DuplicatePhase { READING, COMPARING, ANALYZING }

data class DuplicateProgress(val phase: DuplicatePhase, val done: Int, val total: Int)

sealed interface DuplicateScanEvent {
    data class Progress(val progress: DuplicateProgress) : DuplicateScanEvent
    data class Finished(val result: DuplicateScanResult) : DuplicateScanEvent
}
