package edu.put.whisper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Context

class TranscriptionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transcription_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val errorMessage = intent.getStringExtra("EXTRA_ERROR_MESSAGE")
        val transcriptionText = intent.getStringExtra("EXTRA_TRANSCRIPTION_TEXT")
        val transcriptionDate = intent.getStringExtra("EXTRA_TRANSCRIPTION_DATE")
        val filePath = intent.getStringExtra("EXTRA_FILE_PATH")
        val language = intent.getStringExtra("EXTRA_LANGUAGE")

        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        if (transcriptionText != null && transcriptionDate != null) {
            val tvTranscriptionText = findViewById<TextView>(R.id.tvFullTranscription)
            val tvTranscriptionDate = findViewById<TextView>(R.id.tvTranscriptionDate)

            tvTranscriptionText.text = transcriptionText
            tvTranscriptionDate.text = transcriptionDate

            findViewById<LinearLayout>(R.id.btnTranslate).setOnClickListener {
                val intent = Intent(this, TranslateActivity::class.java)
                intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcriptionText)
                startActivity(intent)
            }

            findViewById<LinearLayout>(R.id.btnRoles).setOnClickListener {
                val intent = Intent(this, RolesActivity::class.java)
                intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcriptionText)
                intent.putExtra("EXTRA_FILE_PATH", filePath)
                intent.putExtra("EXTRA_LANGUAGE", language)
                startActivity(intent)
            }

            findViewById<ImageButton>(R.id.btnCopyTranscription).setOnClickListener {
                copyToClipboard(transcriptionText)
            }
        }
    }
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Transcription", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Transcription copied", Toast.LENGTH_SHORT).show()
    }
}
