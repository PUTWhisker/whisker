package edu.put.whisper

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LiveTranscriptionActivity : AppCompatActivity() {

    private lateinit var tvTranscriptionsLive: TextView
    private lateinit var btnBack: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_transcription)

        tvTranscriptionsLive = findViewById(R.id.tvTranscriptionsLive)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

    }
}