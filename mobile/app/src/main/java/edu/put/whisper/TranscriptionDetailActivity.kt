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
import android.view.View
import android.widget.EditText
import edu.put.whisper.utils.Utilities

class TranscriptionDetailActivity : AppCompatActivity() {
    private lateinit var utilities: Utilities

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

        utilities = Utilities(this)

        val errorMessage = intent.getStringExtra("EXTRA_ERROR_MESSAGE")
        val transcriptionText = intent.getStringExtra("EXTRA_TRANSCRIPTION_TEXT")
        val transcriptionDate = intent.getStringExtra("EXTRA_TRANSCRIPTION_DATE")
        val filePath = intent.getStringExtra("EXTRA_FILE_PATH")
        val language = intent.getStringExtra("EXTRA_LANGUAGE")

        val btnTranslate = findViewById<LinearLayout>(R.id.btnTranslate)
        val btnRoles = findViewById<LinearLayout>(R.id.btnRoles)
        val btnEdit = findViewById<LinearLayout>(R.id.btnEdit)
        val btnAcceptChanges = findViewById<LinearLayout>(R.id.btnAcceptChanges)
        val btnDiscardChanges = findViewById<LinearLayout>(R.id.btnDiscardChanges)
        val tvTranscriptionText = findViewById<EditText>(R.id.tvFullTranscription)
        val tvTranscriptionDate = findViewById<TextView>(R.id.tvTranscriptionDate)


        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            tvTranscriptionText.setText(errorMessage)
        }

        if (transcriptionText != null && transcriptionDate != null) {


            tvTranscriptionText.setText(transcriptionText)
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

            btnEdit.setOnClickListener {
                utilities.setVisibility(View.GONE, btnEdit, btnRoles, btnTranslate)
                utilities.setVisibility(View.VISIBLE, btnAcceptChanges, btnDiscardChanges)
                tvTranscriptionText.isEnabled = true
            }

            btnAcceptChanges.setOnClickListener {
                // Tutaj ten updatedText mozesz zapisac do bazy danych czy tam uzyc swojej funkcji, cokolwiek chcesz z tym zrobic :3
                val updatedText = tvTranscriptionText.text.toString()
                Toast.makeText(this, "Changes Accepted", Toast.LENGTH_SHORT).show()

                utilities.setVisibility(View.GONE, btnAcceptChanges, btnDiscardChanges)
                utilities.setVisibility(View.VISIBLE, btnEdit, btnRoles, btnTranslate)

                tvTranscriptionText.isEnabled = false
            }

            btnDiscardChanges.setOnClickListener {
                tvTranscriptionText.setText(transcriptionText)
                Toast.makeText(this, "Changes Discarded", Toast.LENGTH_SHORT).show()

                utilities.setVisibility(View.GONE, btnAcceptChanges, btnDiscardChanges)
                utilities.setVisibility(View.VISIBLE, btnEdit, btnRoles, btnTranslate)

                tvTranscriptionText.isEnabled = false
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
