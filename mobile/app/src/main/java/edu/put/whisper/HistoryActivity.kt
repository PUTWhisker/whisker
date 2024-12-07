package edu.put.whisper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvTranscriptions = findViewById(R.id.rvTranscriptions)
        rvTranscriptions.layoutManager = LinearLayoutManager(this)

        authClient = AuthenticationClient(Uri.parse("http://100.80.80.156:50051/"))

        loadTranscriptions()
    }

    private fun loadTranscriptions() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val history = authClient.GetTranslations()
                withContext(Dispatchers.Main) {
                    transcriptionAdapter = TranscriptionAdapter(history) { transcription ->
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
                    Toast.makeText(this@HistoryActivity, "Failed to load transcriptions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
