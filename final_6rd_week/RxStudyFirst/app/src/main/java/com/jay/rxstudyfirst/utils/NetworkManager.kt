package com.jay.rxstudyfirst.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter

class NetworkManager(context: Context) {
    private val connectivityManager
            = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getNetworkStateChanges(request: NetworkRequest): Flowable<Boolean> {
        return Flowable.create({ emitter: FlowableEmitter<Boolean> ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    emitter.onNext(true)
                }

                override fun onLost(network: Network) {
                    emitter.onNext(false)
                }
            }

            connectivityManager.registerNetworkCallback(request, callback)
            emitter.setCancellable {
                connectivityManager.unregisterNetworkCallback(callback)
            }

        }, BackpressureStrategy.BUFFER)
    }

//    fun networkState(): Boolean {
//
//        val nw = connectivityManager.activeNetwork ?: return false
//        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
//        return when {
//            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//            else -> false
//        }
//    }

}