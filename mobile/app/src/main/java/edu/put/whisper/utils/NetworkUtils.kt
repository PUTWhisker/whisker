package edu.put.whisper.utils

import android.app.VoiceInteractor
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.provider.Settings.Global.getString
import android.util.Log
import edu.put.whisper.R
import io.grpc.soundtransfer.SoundTransferClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit


fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}


suspend fun isServerAlive(serverUrl: String): Boolean {
        Log.d("ServerStatus", "Checking server: $serverUrl")

    val soundTransferClient = SoundTransferClient(Uri.parse(serverUrl))
    return withContext(Dispatchers.IO) {
        soundTransferClient.testConnection()
    }
}




