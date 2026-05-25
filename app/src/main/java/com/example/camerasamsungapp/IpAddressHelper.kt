package com.example.camerasamsungapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface

object IpAddressHelper {

    private const val TAG = "IpAddressHelper"

    fun getLocalIpAddress(context: Context): String? {
        return wifiIpFromConnectivityManager(context) ?: fallbackLocalIpv4()
    }

    private fun wifiIpFromConnectivityManager(context: Context): String? {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return null

            for (network in connectivityManager.allNetworks) {
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) continue

                val linkProperties = connectivityManager.getLinkProperties(network) ?: continue
                for (address in linkProperties.linkAddresses) {
                    val ip = address.address
                    if (ip is Inet4Address && !ip.isLoopbackAddress) {
                        return ip.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Could not read Wi-Fi IP from ConnectivityManager", e)
            null
        }
    }

    private fun fallbackLocalIpv4(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (networkInterface in interfaces) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                for (address in networkInterface.inetAddresses.toList()) {
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        return address.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Could not determine local IPv4 address", e)
            null
        }
    }
}
