package edu.put.whisper

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TranscriptionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transcription_detail)

        val errorMessage = intent.getStringExtra("EXTRA_ERROR_MESSAGE")
        val transcriptionText = intent.getStringExtra("EXTRA_TRANSCRIPTION_TEXT")
        val transcriptionDate = intent.getStringExtra("EXTRA_TRANSCRIPTION_DATE")

        if (errorMessage != null) {
            // Display the error message as a Toast
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        if (transcriptionText != null && transcriptionDate != null) {
            // Proceed with displaying transcription details
            val tvTranscriptionText = findViewById<TextView>(R.id.tvFullTranscription)
            val tvTranscriptionDate = findViewById<TextView>(R.id.tvTranscriptionDate)

            tvTranscriptionText.text = transcriptionText
            tvTranscriptionDate.text = transcriptionDate

            findViewById<LinearLayout>(R.id.btnTranslate).setOnClickListener {
                val intent = Intent(this, TranslateActivity::class.java)
                intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcriptionText)
                startActivity(intent)
            }
        }
    }
}
