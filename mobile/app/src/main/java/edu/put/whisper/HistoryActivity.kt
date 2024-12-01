package edu.put.whisper

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
                val formattedHistory = history.map { "${it.text}, ${it.timestamp}" }
                withContext(Dispatchers.Main) {
                    transcriptionAdapter = TranscriptionAdapter(formattedHistory) { transcription ->
                        Toast.makeText(this@HistoryActivity, "Clicked: $transcription", Toast.LENGTH_SHORT).show()
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
