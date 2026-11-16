package com.phonecleaner.aicleaner.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkObserverImpl(context: Context) : NetworkObserver {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @SuppressLint("MissingPermission")
    override fun observe(): Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Initial state check
        val currentStatus = if (connectivityManager.activeNetwork != null) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
        trySend(currentStatus)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}

interface NetworkObserver {
    fun observe(): Flow<NetworkStatus>
}

