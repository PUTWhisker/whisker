package edu.put.whisper.utils

import android.app.VoiceInteractor
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.Settings.Global.getString
import android.util.Log
import edu.put.whisper.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress


fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}

fun isServerAlive(serverUrl: String): Boolean {
    Log.d("ServerStatus", "Checking server: $serverUrl")
    return try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$serverUrl")
            .build()

        val response = client.newCall(request).execute()
        val isSuccessful = response.isSuccessful
        Log.d("ServerStatus", "Server reachable: $isSuccessful")
        response.close()
        isSuccessful
    } catch (e: Exception) {
        Log.e("ServerStatus", "Error checking server: ${e.message}")
        false
    }
}


