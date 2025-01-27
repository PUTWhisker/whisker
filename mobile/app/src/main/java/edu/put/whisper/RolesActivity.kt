package edu.put.whisper

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.grpc.soundtransfer.SoundTransferClient
import io.grpc.soundtransfer.SpeakerAndLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RolesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SpeakerAdapter
    private lateinit var client: SoundTransferClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roles)

        recyclerView = findViewById(R.id.rvSpeakers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val serverUrl = getString(R.string.server_url)
        val serverUri = Uri.parse(serverUrl)
        client = SoundTransferClient(serverUri)

        CoroutineScope(Dispatchers.Main).launch {
            val speakerLines: List<SpeakerAndLine> = client.diarizateSpeakers("path_to_audio_file", "pl")
            adapter = SpeakerAdapter(speakerLines)
            recyclerView.adapter = adapter
        }
    }
}
