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
    private lateinit var btnBack: ImageButton
    private lateinit var utilities: Utilities

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_transcription)

        tvTranscriptionsLive = findViewById(R.id.tvTranscriptionsLive)
        scrollView = findViewById(R.id.scrollView)
        btnBack = findViewById(R.id.btnBack)
        utilities = Utilities(this)
        val serverUrl = getString(R.string.server_url)
        val serverUri = Uri.parse(serverUrl)
        soundTransferClient = SoundTransferClient(serverUri)

        btnBack.setOnClickListener {
            utilities.goBack(this)
        }
        requestAudioPermission()
    }

    private fun startTranscription() {
        soundTransferClient.transcribeLive { transcribedText ->
            runOnUiThread {
                tvTranscriptionsLive.append("\n$transcribedText")
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
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
        super.onDestroy()
        soundTransferClient.stopStream()
        soundTransferClient.close()
    }
}
