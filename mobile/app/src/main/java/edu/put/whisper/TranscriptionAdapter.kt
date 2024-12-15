package edu.put.whisper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.grpc.authentication.TranscriptionElement
import java.text.SimpleDateFormat
import java.util.*

class TranscriptionAdapter(
    private val transcriptions: List<TranscriptionElement>,
    private val onItemClick: (TranscriptionElement) -> Unit
) : RecyclerView.Adapter<TranscriptionAdapter.TranscriptionViewHolder>() {

    class TranscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transcriptionTextView: TextView = itemView.findViewById(R.id.tvTranscriptionItem)
        val transcriptionDateView: TextView = itemView.findViewById(R.id.tvTranscriptionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranscriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transcription, parent, false)
        return TranscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranscriptionViewHolder, position: Int) {
        val transcription = transcriptions[position]
        val previewText = transcription.text.split(" ").take(5).joinToString(" ")
        holder.transcriptionTextView.text = previewText

        val formattedDate = formatDate(transcription.timestamp.toEpochMilli())
        holder.transcriptionDateView.text = formattedDate

        holder.itemView.setOnClickListener { onItemClick(transcription) }
    }

    override fun getItemCount() = transcriptions.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}
