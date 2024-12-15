package edu.put.whisper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.grpc.authentication.AuthenticationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    private lateinit var rvTranscriptions: RecyclerView
    private lateinit var transcriptionAdapter: TranscriptionAdapter
    private lateinit var authClient: AuthenticationClient
    private lateinit var btnDelete: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        rvTranscriptions = findViewById(R.id.rvTranscriptions)
        rvTranscriptions.layoutManager = LinearLayoutManager(this)
        btnDelete = findViewById(R.id.btnDelete)

        authClient = AuthenticationClient(Uri.parse("http://100.80.80.156:50051/"))

        loadTranscriptions()

        btnDelete.setOnClickListener {
            val selectedItems = transcriptionAdapter.getSelectedItems()
            // TU LOGIKA USUWANIA TRANSKRYPCJI
            Toast.makeText(this, "Usunięto ${selectedItems.size} transkrypcji", Toast.LENGTH_SHORT).show()

            //transcriptionAdapter.notifyDataSetChanged() // odświeżenie adaptera
        }
    }

    private fun loadTranscriptions() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val history = authClient.getTranscriptions()
                withContext(Dispatchers.Main) {
                    transcriptionAdapter = TranscriptionAdapter(history.toMutableList()) { transcription ->
                        val intent = Intent(this@HistoryActivity, TranscriptionDetailActivity::class.java)
                        intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcription.text)
                        intent.putExtra(
                            "EXTRA_TRANSCRIPTION_DATE",
                            SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(transcription.timestamp.toEpochMilli())
                        )
                        startActivity(intent)
                    }
                    rvTranscriptions.adapter = transcriptionAdapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HistoryActivity", "Error loading transcriptions", e)
                    Toast.makeText(this@HistoryActivity, "Failed to load transcriptions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
