package edu.put.whisper

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.grpc.soundtransfer.SoundTransferClient
import android.Manifest
import android.view.View
import android.widget.ScrollView
import edu.put.whisper.utils.Utilities

class LiveTranscriptionActivity : AppCompatActivity() {

    private lateinit var soundTransferClient: SoundTransferClient
    private lateinit var tvTranscriptionsLive: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var utilities: Utilities

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_transcription)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        tvTranscriptionsLive = findViewById(R.id.tvTranscriptionsLive)
        scrollView = findViewById(R.id.scrollView)
        utilities = Utilities(this)
        val serverUrl = getString(R.string.server_url)
        val serverUri = Uri.parse(serverUrl)
        soundTransferClient = SoundTransferClient(serverUri)

        requestAudioPermission()
    }

    private fun startTranscription() {
        val selectedLanguage = intent.getStringExtra("LANGUAGE") ?: "eng"

        soundTransferClient.transcribeLive(selectedLanguage) { updatedText ->
            runOnUiThread {
                tvTranscriptionsLive.text = updatedText
            }
        }
    }


    private fun stopTranscription() {
        try {
            soundTransferClient.stopStream()
            Log.d("LiveTranscription", "Transcription stopped.")
        } catch (e: Exception) {
            Log.e("LiveTranscription", "Error stopping transcription: ${e.message}")
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            startTranscription()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LiveTranscription", "RECORD_AUDIO permission granted")
                startTranscription()
            } else {
                Log.e("LiveTranscription", "RECORD_AUDIO permission denied")
                finish()
            }
        }
    }

    override fun onDestroy() {
        stopTranscription()
        try {
            soundTransferClient.stopStream()
        } catch (e: Exception) {
            Log.e("LiveTranscription", "Error stopping stream: ${e.message}")
        } finally {
            soundTransferClient.close()
        }
        super.onDestroy()
    }

    @Synchronized
    private fun safeCloseClient() {
        try {
            soundTransferClient.stopStream()
            soundTransferClient.close()
        } catch (e: Exception) {
            Log.e("LiveTranscription", "Error closing client: ${e.message}")
        }
    }
}
// jezeli newchunk jest na false to trzeba nadpisac wiadomosc
