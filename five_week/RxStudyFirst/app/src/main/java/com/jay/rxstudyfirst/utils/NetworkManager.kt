package com.jay.rxstudyfirst.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

class NetworkManager(context: Context) {
    private val connectivityManager
            = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun register(request: NetworkRequest, callback: ConnectivityManager.NetworkCallback){
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun unregister(callback: ConnectivityManager.NetworkCallback){
        connectivityManager.unregisterNetworkCallback(callback)
    }

    fun networkState(): Boolean {

        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

}