package edu.put.whisper

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import io.grpc.soundtransfer.SoundTransferClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TranslateActivity : AppCompatActivity() {

    private lateinit var client: SoundTransferClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val serverUrl = getString(R.string.server_url)
        val serverUri = Uri.parse(serverUrl)
        client = SoundTransferClient(serverUri)
        val transcriptionText = intent.getStringExtra("EXTRA_TRANSCRIPTION_TEXT")

        val spinnerLanguage = findViewById<Spinner>(R.id.spinner_language)
        val spinnerTargetLanguage = findViewById<Spinner>(R.id.spinner_target_language)
        val editTextOriginal = findViewById<EditText>(R.id.edit_text_original)
        val editTextTranslated = findViewById<EditText>(R.id.edit_text_translated)
        val buttonTranslate = findViewById<Button>(R.id.button_translate)

        val languages = resources.getStringArray(R.array.languages)
        val languagesFull = resources.getStringArray(R.array.languages_full)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languagesFull)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerLanguage.adapter = adapter
        spinnerTargetLanguage.adapter = adapter

        //domyslne wartosci wybranych jezykow
        spinnerLanguage.setSelection(languages.indexOf("pl"))
        spinnerTargetLanguage.setSelection(languages.indexOf("en"))

        if (!transcriptionText.isNullOrEmpty()) {
            editTextOriginal.setText(transcriptionText)
        }


        buttonTranslate.setOnClickListener {
            val originalText = editTextOriginal.text.toString()
            val sourceLanguage = languages[spinnerLanguage.selectedItemPosition]
            val targetLanguage = languages[spinnerTargetLanguage.selectedItemPosition]

            if (originalText.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val tempFile = createTempFile(originalText)
                        val flow = client.translate(tempFile.absolutePath, sourceLanguage, targetLanguage)

                        var translatedText = ""
                        flow.collect { response ->
                            translatedText += response.text
                        }


                        editTextTranslated.setText(translatedText)

                        tempFile.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        editTextTranslated.setText("Wystąpił błąd podczas tłumaczenia")
                    }
                }
            } else {
                editTextTranslated.setText("Wprowadź tekst do przetłumaczenia")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }

    private fun createTempFile(text: String): File {
        val tempFile = File.createTempFile("soundtransfer", ".txt", cacheDir)
        tempFile.writeText(text)
        return tempFile
    }

}
