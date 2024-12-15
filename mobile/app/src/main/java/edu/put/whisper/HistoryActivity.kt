package edu.put.whisper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var deletell: LinearLayout

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
        deletell = findViewById(R.id.deletell)

        authClient = AuthenticationClient(Uri.parse("http://100.80.80.156:50051/"))

        loadTranscriptions()

        btnDelete.setOnClickListener {
            val selectedItems = transcriptionAdapter.getSelectedItems()

            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No transcriptions selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show confirmation dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Delete transcription?")
                .setMessage("Do you want to delete ${selectedItems.size} transcriptions?")
                .setPositiveButton("Yes") { _, _ ->
                    // User confirmed deletion
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            for (item in selectedItems) {
                                authClient.deleteTranscription(item.id) // Usuwanie transkrypcji po ID
                            }
                            withContext(Dispatchers.Main) {
                                transcriptionAdapter.removeItems(selectedItems)
                                updateDeleteLayoutVisibility()
                                Toast.makeText(this@HistoryActivity, "Deleted ${selectedItems.size} transcriptions", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@HistoryActivity, "Error while deleting transcriptions", Toast.LENGTH_SHORT).show()
                                Log.e("HistoryActivity", "Error deleting transcriptions", e)
                            }
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            dialog.show()
        }

    }

    private fun loadTranscriptions() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val history = authClient.getTranscriptions() // Pobranie historii transkrypcji
                withContext(Dispatchers.Main) {
                    transcriptionAdapter = TranscriptionAdapter(
                        transcriptions = history.toMutableList(),
                        onItemClick = { transcription -> // Lambda dla kliknięcia na transkrypcję
                            val intent = Intent(this@HistoryActivity, TranscriptionDetailActivity::class.java)
                            intent.putExtra("EXTRA_TRANSCRIPTION_TEXT", transcription.text)
                            intent.putExtra(
                                "EXTRA_TRANSCRIPTION_DATE",
                                SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(transcription.timestamp.toEpochMilli())
                            )
                            startActivity(intent)
                        },
                        onSelectionChanged = { // Lambda dla zmiany zaznaczenia
                            updateDeleteLayoutVisibility()
                        }
                    )
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


    private fun updateDeleteLayoutVisibility() {
        if (transcriptionAdapter.getSelectedItems().isNotEmpty()) {
            deletell.visibility = View.VISIBLE
        } else {
            deletell.visibility = View.GONE
        }
    }
}
