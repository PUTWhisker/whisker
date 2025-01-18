package edu.put.whisper

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.grpc.soundtransfer.SoundTransferClient
import io.grpc.soundtransfer.SpeakerAndLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RolesActivity : AppCompatActivity() {

    //private lateinit var tvDiarizedTranscription: TextView
    private lateinit var soundTransferClient: SoundTransferClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RolesActivity", "onCreate called")
        setContentView(R.layout.activity_roles)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // PRZETESTUJ Z CZYSTYM PLIKIEM

      //  tvDiarizedTranscription = findViewById(R.id.tvDiarizedTranscription)

        val serverUrl = getString(R.string.server_url)
        val serverUri = Uri.parse(serverUrl)
        Log.d("RolesActivity", "Server URI: $serverUri")
        soundTransferClient = SoundTransferClient(serverUri)

        val filePath = intent.getStringExtra("EXTRA_FILE_PATH")
        val language = intent.getStringExtra("EXTRA_LANGUAGE") ?: "en"
        Log.d("RolesActivity", "FilePath: $filePath, Language: $language")

        lifecycleScope.launch {
            try {
                if (filePath == null) {
                    Log.e("RolesActivity", "File path is null")
                    return@launch
                }

                val file = File(filePath)
                if (!file.exists()) {
                    Log.e("RolesActivity", "File does not exist: $filePath")
                    return@launch
                }

                val fileSize = file.length()
                if (fileSize == 0L) {
                    Log.e("RolesActivity", "File is empty: $filePath")
                    return@launch
                }

                Log.d("RolesActivity", "File exists and is non-empty. Size: $fileSize bytes")

                val speakerLines = soundTransferClient.diarizateSpeakers(filePath, language)
                if (speakerLines != null) {
                    Log.d("RolesActivity", "Server returned speaker lines: $speakerLines")
                    displaySpeakerLines(speakerLines)
                } else {
                    Log.e("RolesActivity", "Server returned null for speaker lines")
                }
            } catch (e: Exception) {
                Log.e("RolesActivity", "Error occurred during diarization", e)
            }
        }
    }

    private fun displaySpeakerLines(speakerLines: List<SpeakerAndLine>) {
        val textView = findViewById<TextView>(R.id.tvDiarizedTranscription)
        val displayText = speakerLines.joinToString(separator = "\n") { speakerAndLine ->
            "${speakerAndLine.speaker}, ${speakerAndLine.line}"
        }
        textView.text = displayText
    }

    override fun onDestroy() {
        super.onDestroy()
        soundTransferClient.close()
    }
}