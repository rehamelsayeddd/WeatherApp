package com.example.weatherapp.Network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class NetworkUtil(private val onNetworkStateChanged: (Boolean) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo?.isConnectedOrConnecting == true
        onNetworkStateChanged.invoke(isConnected)
    }
}