package edu.put.whisper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.grpc.authentication.TranscriptionElement
import java.text.SimpleDateFormat
import java.util.*

class TranscriptionAdapter(
    private val transcriptions: MutableList<TranscriptionElement>,
    private val onItemClick: (TranscriptionElement) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<TranscriptionAdapter.TranscriptionViewHolder>() {

    private val selectedItems = mutableSetOf<Int>() //lista do trzymania stanu zaznaczenia

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

        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(ResourcesCompat.getColor(holder.itemView.resources, R.color.primaryLight, null))
        } else {
            holder.itemView.setBackgroundColor(ResourcesCompat.getColor(holder.itemView.resources, R.color.white, null))
        }

        holder.itemView.setOnClickListener { onItemClick(transcription) }

        holder.itemView.setOnLongClickListener {
            toggleSelection(position)
            true
        }
    }

    override fun getItemCount() = transcriptions.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position) // powiadamiamy adapter ze element zmienil stan
        onSelectionChanged()
    }

    // zwraca liste zaznaczonych transkrypcji
    fun getSelectedItems(): List<TranscriptionElement> {
        return selectedItems.map { transcriptions[it] }
    }

    fun removeItems(itemsToRemove: List<TranscriptionElement>) {
        transcriptions.removeAll(itemsToRemove)
        selectedItems.clear()
        notifyDataSetChanged()
    }
}
