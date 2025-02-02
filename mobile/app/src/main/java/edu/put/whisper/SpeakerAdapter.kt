package edu.put.whisper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.grpc.soundtransfer.SpeakerAndLine

class SpeakerAdapter(private val data: List<SpeakerAndLine>) :
    RecyclerView.Adapter<SpeakerAdapter.SpeakerViewHolder>() {

    inner class SpeakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSpeaker: TextView = itemView.findViewById(R.id.tvSpeaker)
        val tvText: TextView = itemView.findViewById(R.id.tvLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_speaker_and_line, parent, false)
        return SpeakerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
        val item = data[position]
        holder.tvSpeaker.text = "${item.speaker}:"
        holder.tvText.text = item.line
    }

    override fun getItemCount(): Int = data.size
}
