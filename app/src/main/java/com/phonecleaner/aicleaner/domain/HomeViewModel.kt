package com.phonecleaner.aicleaner.domain

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecleaner.aicleaner.core.datastore.AppDataStore
import com.phonecleaner.aicleaner.core.utils.NetworkObserver
import com.phonecleaner.aicleaner.core.utils.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StorageInfo(
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val freeBytes: Long = 0L
) {
    val usedFraction: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
}

class HomeViewModel(
    private val appDataStore: AppDataStore,
    networkObserver: NetworkObserver
) : ViewModel() {

    private val _homeLandCount = MutableStateFlow(0)
    val homeLandCount = _homeLandCount.asStateFlow()

    private val _storage = MutableStateFlow(StorageInfo())
    val storage = _storage.asStateFlow()

    val networkStatus = networkObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.Available)

    init {
        refreshStorage()
        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.incrementCountForHome()
            appDataStore.getCountForHome().collect { count ->
                _homeLandCount.value = count
            }
        }
    }

    fun refreshStorage() {
        viewModelScope.launch {
            _storage.value = withContext(Dispatchers.IO) { readStorage() }
        }
    }

    fun onPermissionGranted() = viewModelScope.launch(Dispatchers.IO) {
        appDataStore.setNotificationPermissionRequested(true)
    }

    fun onPermissionDenied() = Unit

    private fun readStorage(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val free = stat.availableBytes
        return StorageInfo(totalBytes = total, usedBytes = total - free, freeBytes = free)
    }
}
