package com.ogogo_labs.pref_listener.debug.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.ogogo_labs.pref_listener.debug.utils.Logger.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitoringUtil(context: Context) : NetworkCallback() {
    private val mNetworkRequest: NetworkRequest =
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
    private val mConnectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val networkState: StateFlow<Boolean> = _networkState.asStateFlow()

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        logD("NetworkMonitoringUtil onAvailable() called: Connected to network")
        _networkState.value = true
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        logD("NetworkMonitoringUtil onLost() called: Lost network connection")
        _networkState.value = false
    }

    @SuppressLint("MissingPermission")
    fun registerNetworkCallbackEvents() {
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this)
        logD("NetworkMonitoringUtil registerNetworkCallbackEvents() called")
    }

    @SuppressLint("MissingPermission")
    fun checkNetworkState(): Boolean {
        try {
            val networkInfo = mConnectivityManager.activeNetworkInfo
            _networkState.value = networkInfo != null
                    && networkInfo.isConnected

        } catch (exception: Exception) {
            exception.printStackTrace()
            _networkState.value = false
        } finally {
            return networkState.value
        }
    }
}
