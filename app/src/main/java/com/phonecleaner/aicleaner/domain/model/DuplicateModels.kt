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
    val durationMs: Long,
    /** True for non-media files (documents, audio, APKs, archives...) which have no thumbnail. */
    val isFile: Boolean = false,
    val mime: String = ""
)

enum class DuplicateKind { EXACT, SIMILAR }

data class DuplicateGroup(
    val id: String,
    val kind: DuplicateKind,
    val isVideo: Boolean,
    val files: List<MediaFile>,
    val isFile: Boolean = false
)

data class DuplicateScanResult(
    val photoGroups: List<DuplicateGroup>,
    val videoGroups: List<DuplicateGroup>,
    val fileGroups: List<DuplicateGroup> = emptyList()
)

enum class DuplicatePhase { READING, COMPARING, ANALYZING }

data class DuplicateProgress(val phase: DuplicatePhase, val done: Int, val total: Int)

sealed interface DuplicateScanEvent {
    data class Progress(val progress: DuplicateProgress) : DuplicateScanEvent
    data class Finished(val result: DuplicateScanResult) : DuplicateScanEvent
}
