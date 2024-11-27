package edu.put.whisper.utilities

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import edu.put.whisper.R
import io.grpc.soundtransfer.SoundTransferGrpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class Utilities(private val context: Context) {

    suspend fun uploadRecording(filePath: String, callback: (String?) -> Unit) {
        try {
            val serverUri = Uri.parse(context.resources.getString(R.string.server_url))
            val transfer = SoundTransferGrpc(serverUri)
            val output: String? = transfer.sendSoundFile(filePath)

            withContext(Dispatchers.Main) {
                callback(output)
            }
        } catch (e: Exception) {
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
}