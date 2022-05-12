package com.tkachenko.audionotesvk.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.entities.AudioNote

class AudioNotesAdapter: RecyclerView.Adapter<AudioNotesAdapter.AudioNoteHolder>() {
    private val audioNotes: MutableList<AudioNote> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(audioNotes: List<AudioNote>) {
        this.audioNotes.clear()
        this.audioNotes.addAll(audioNotes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioNoteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_note, parent,false)
        return AudioNoteHolder(view = view)
    }

    override fun onBindViewHolder(holder: AudioNoteHolder, position: Int) {
        val audioNote = audioNotes[position]
        holder.bind(audioNote)
    }

    override fun getItemCount() = audioNotes.size

    inner class AudioNoteHolder(view: View): RecyclerView.ViewHolder(view) {
        //private val textView: TextView = view.findViewById(R.id.text_view)

        fun bind(model: AudioNote) {
            //textView.text = model.text
        }
    }
}