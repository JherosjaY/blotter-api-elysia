package com.example.blottermanagementsystem.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * NetworkMonitor - Detects internet connectivity changes
 * Used for offline-first architecture to trigger auto-sync
 */
class NetworkMonitor(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Check if device is currently online
     */
    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Observe network connectivity changes
     * Emits true when online, false when offline
     */
    fun observeNetworkConnectivity(): Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NetworkMonitor", "🌐 Network available - ONLINE")
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                Log.d("NetworkMonitor", "📵 Network lost - OFFLINE")
                trySend(false)
            }
            
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val isOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                              capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Log.d("NetworkMonitor", "🔄 Network capabilities changed - ${if (isOnline) "ONLINE" else "OFFLINE"}")
                trySend(isOnline)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, networkCallback)
        
        // Send initial state
        trySend(isOnline())
        
        awaitClose {
            Log.d("NetworkMonitor", "⏹️ Unregistering network callback")
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
