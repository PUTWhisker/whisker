package edu.put.whisper

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TranscriptionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transcription_detail)

        val transcriptionTextView: TextView = findViewById(R.id.tvFullTranscription)
        val transcriptionDateView: TextView = findViewById(R.id.tvTranscriptionDate)

        // Pobierz dane z Intent
        val transcriptionText = intent.getStringExtra("EXTRA_TRANSCRIPTION_TEXT")
        val transcriptionDate = intent.getStringExtra("EXTRA_TRANSCRIPTION_DATE")

        // Ustaw dane na widokach
        transcriptionTextView.text = transcriptionText
        transcriptionDateView.text = transcriptionDate
    }
}
