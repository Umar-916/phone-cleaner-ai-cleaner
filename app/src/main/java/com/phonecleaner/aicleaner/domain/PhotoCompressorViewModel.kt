package com.phonecleaner.aicleaner.domain

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.data.image.PhotoCompressor
import com.phonecleaner.aicleaner.domain.model.CompressSettings
import com.phonecleaner.aicleaner.domain.model.CompressedFile
import com.phonecleaner.aicleaner.domain.model.OutputFormat
import com.phonecleaner.aicleaner.domain.model.PhotoInfo
import com.phonecleaner.aicleaner.domain.model.PhotoPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed interface CompressPhase {
    data object Editing : CompressPhase
    data class Processing(val done: Int, val total: Int) : CompressPhase
    data class Done(val files: List<CompressedFile>, val failed: Int) : CompressPhase
}

data class CompressorState(
    val photos: List<PhotoInfo> = emptyList(),
    val selectedIndex: Int = 0,
    val settings: CompressSettings = CompressSettings(),
    val widthText: String = "",
    val heightText: String = "",
    val preview: PhotoPreview = PhotoPreview(),
    val isLoadingPhotos: Boolean = false,
    val phase: CompressPhase = CompressPhase.Editing
) {
    val selected: PhotoInfo? get() = photos.getOrNull(selectedIndex)
}

class PhotoCompressorViewModel(private val compressor: PhotoCompressor) : ViewModel() {

    private val _state = MutableStateFlow(CompressorState())
    val state: StateFlow<CompressorState> = _state.asStateFlow()

    private var previewJob: Job? = null
    private var processJob: Job? = null
    private val originalCache = HashMap<Uri, Bitmap>()

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _state.update { it.copy(isLoadingPhotos = true) }
        viewModelScope.launch {
            val known = _state.value.photos.map { it.uri }.toSet()
            val added = uris.filter { it !in known }.mapNotNull { compressor.readInfo(it) }
            _state.update { current ->
                val photos = current.photos + added
                val first = photos.firstOrNull()
                current.copy(
                    photos = photos,
                    isLoadingPhotos = false,
                    selectedIndex = if (current.photos.isEmpty()) 0 else current.selectedIndex,
                    widthText = current.widthText.ifEmpty { first?.width?.toString().orEmpty() },
                    heightText = current.heightText.ifEmpty { first?.height?.toString().orEmpty() },
                    settings = if (current.settings.width == 0 && first != null) {
                        current.settings.copy(width = first.width, height = first.height)
                    } else {
                        current.settings
                    }
                )
            }
            schedulePreview()
        }
    }

    fun removePhoto(index: Int) {
        _state.update { current ->
            val photos = current.photos.filterIndexed { i, _ -> i != index }
            current.copy(photos = photos, selectedIndex = current.selectedIndex.coerceAtMost((photos.size - 1).coerceAtLeast(0)))
        }
        schedulePreview()
    }

    fun select(index: Int) {
        _state.update { it.copy(selectedIndex = index) }
        syncDimensionTexts()
        schedulePreview()
    }

    fun setFormat(format: OutputFormat) = changeSettings { it.copy(format = format) }

    fun setQuality(quality: Int) = changeSettings { it.copy(quality = quality) }

    fun setCustomSize(custom: Boolean) {
        changeSettings { it.copy(custom = custom) }
        if (custom) syncDimensionTexts()
    }

    fun setKeepAspect(keep: Boolean) {
        changeSettings { it.copy(keepAspect = keep) }
        if (keep) setWidthText(_state.value.widthText)
    }

    fun setWidthText(text: String) {
        val width = text.filter { it.isDigit() }.take(MAX_DIGITS)
        val photo = _state.value.selected
        _state.update { current ->
            val w = width.toIntOrNull() ?: 0
            val height = if (current.settings.keepAspect && photo != null && w > 0) {
                (w.toFloat() * photo.height / photo.width).roundToInt()
            } else {
                current.settings.height
            }
            current.copy(
                widthText = width,
                heightText = if (current.settings.keepAspect && w > 0) height.toString() else current.heightText,
                settings = current.settings.copy(width = w, height = height)
            )
        }
        schedulePreview()
    }

    fun setHeightText(text: String) {
        val height = text.filter { it.isDigit() }.take(MAX_DIGITS)
        val photo = _state.value.selected
        _state.update { current ->
            val h = height.toIntOrNull() ?: 0
            val width = if (current.settings.keepAspect && photo != null && h > 0) {
                (h.toFloat() * photo.width / photo.height).roundToInt()
            } else {
                current.settings.width
            }
            current.copy(
                heightText = height,
                widthText = if (current.settings.keepAspect && h > 0) width.toString() else current.widthText,
                settings = current.settings.copy(width = width, height = h)
            )
        }
        schedulePreview()
    }

    fun compressAll() {
        val current = _state.value
        if (current.photos.isEmpty() || processJob?.isActive == true) return
        previewJob?.cancel()
        _state.update { it.copy(phase = CompressPhase.Processing(0, current.photos.size)) }
        processJob = viewModelScope.launch {
            val results = ArrayList<CompressedFile>()
            var failed = 0
            current.photos.forEachIndexed { index, photo ->
                val saved = runCatching {
                    val encoded = compressor.compress(photo, current.settings)
                    compressor.save(encoded.bytes, photo.name, current.settings.format)?.let { uri ->
                        CompressedFile(photo.name, photo.sizeBytes, encoded.bytes.size.toLong(), uri)
                    }
                }.getOrNull()
                if (saved != null) results += saved else failed++
                _state.update { it.copy(phase = CompressPhase.Processing(index + 1, current.photos.size)) }
            }
            _state.update { it.copy(phase = CompressPhase.Done(results, failed)) }
        }
    }

    suspend fun thumbnail(uri: Uri): Bitmap? = compressor.decodePreview(uri, 240)

    fun backToEditing() {
        _state.update { it.copy(phase = CompressPhase.Editing) }
        schedulePreview()
    }

    fun reset() {
        previewJob?.cancel()
        processJob?.cancel()
        originalCache.clear()
        _state.value = CompressorState()
    }

    private fun changeSettings(transform: (CompressSettings) -> CompressSettings) {
        _state.update { it.copy(settings = transform(it.settings)) }
        schedulePreview()
    }

    private fun syncDimensionTexts() {
        _state.update { current ->
            val photo = current.selected ?: return@update current
            if (current.settings.keepAspect && current.settings.width > 0) {
                val h = (current.settings.width.toFloat() * photo.height / photo.width).roundToInt()
                current.copy(heightText = h.toString(), settings = current.settings.copy(height = h))
            } else {
                current
            }
        }
    }

    private fun schedulePreview() {
        previewJob?.cancel()
        val photo = _state.value.selected
        if (photo == null) {
            _state.update { it.copy(preview = PhotoPreview(loading = false)) }
            return
        }
        previewJob = viewModelScope.launch {
            _state.update { it.copy(preview = it.preview.copy(loading = true, original = originalCache[photo.uri])) }
            delay(PREVIEW_DEBOUNCE_MS)
            val original = originalCache[photo.uri] ?: compressor.decodePreview(photo.uri, PREVIEW_SIDE)?.also { originalCache[photo.uri] = it }
            val settings = _state.value.settings
            val encoded = runCatching { compressor.compress(photo, settings) }.getOrNull()
            val compressedBitmap = encoded?.let { compressor.decodeBytes(it.bytes, PREVIEW_SIDE) }
            _state.update {
                it.copy(
                    preview = PhotoPreview(
                        original = original,
                        compressed = compressedBitmap,
                        compressedBytes = encoded?.bytes?.size ?: 0,
                        outWidth = encoded?.width ?: 0,
                        outHeight = encoded?.height ?: 0,
                        loading = false
                    )
                )
            }
        }
    }

    override fun onCleared() {
        previewJob?.cancel()
        processJob?.cancel()
    }

    private companion object {
        const val PREVIEW_DEBOUNCE_MS = 350L
        const val PREVIEW_SIDE = 800
        const val MAX_DIGITS = 4
    }
}
