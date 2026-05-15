package com.example.camerasamsungapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object IpAddressHelper {
    private const val TAG = "IpAddressHelper"

    fun getLocalIpAddress(context: Context): String? {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager == null) {
                Log.w(TAG, "ConnectivityManager not available")
                return null
            }
            
            @Suppress("DEPRECATION")
            val networks = connectivityManager.allNetworks
            if (networks.isEmpty()) {
                Log.w(TAG, "No networks available")
                return null
            }
            
            for (network in networks) {
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue
                
                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    continue
                }
                
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    linkProperties?.linkAddresses?.forEach { addr ->
                        val ip = addr.address.hostAddress ?: return@forEach
                        if (ip.contains(":").not() && ip.isNotBlank()) {
                            Log.d(TAG, "Found Wi-Fi IP: $ip")
                            return ip
                        }
                    }
                }
            }
            
            Log.w(TAG, "No Wi-Fi IP found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
            null
        }
    }
}
