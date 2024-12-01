package edu.put.whisper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TranscriptionAdapter(
    private val transcriptions: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<TranscriptionAdapter.TranscriptionViewHolder>() {

    class TranscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transcriptionTextView: TextView = itemView.findViewById(R.id.tvTranscriptionItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranscriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transcription, parent, false)
        return TranscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranscriptionViewHolder, position: Int) {
        val transcription = transcriptions[position]
        holder.transcriptionTextView.text = transcription
        holder.itemView.setOnClickListener { onItemClick(transcription) }
    }

    override fun getItemCount() = transcriptions.size
}
