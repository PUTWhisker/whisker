package edu.put.whisper.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import edu.put.whisper.R
import io.grpc.soundtransfer.SoundTransferClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utilities(private val context: Context) {

    suspend fun uploadRecording(filePath: String, language: String, callback: (String?) -> Unit) {
        Log.d("DEBUG", "Uploading file: $filePath with language: $language")
        try {
            val serverUri = Uri.parse(context.resources.getString(R.string.server_url))
            val transfer = SoundTransferClient(serverUri)
            val output = transfer.transcribeFile(filePath, language).text
            withContext(Dispatchers.Main) {
                callback(output)
            }
        } catch (e: Exception) {
            Log.e("DEBUG", "Error uploading recording", e)
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                callback(null)
            }
        }
    }

    fun setVisibility(visibility: Int, vararg views: View) {
        for (view in views) {
            view.visibility = visibility
        }
    }

    fun goBack(activity: AppCompatActivity) {
        activity.finish()
    }

    fun generateDefaultFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "recording_${dateFormat.format(Date())}"
    }

    fun getRecordingFilePath(fileName: String): String {
        val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (!musicDirectory.exists()) {
            musicDirectory.mkdirs()
        }
        return File(musicDirectory, "$fileName.mp3").path
    }

    fun getTempRecordingFilePath(): String {
        val musicDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File(musicDirectory, "tempRecording.mp3").path
    }

    fun isMicrophonePresent(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

}