package com.tkachenko.audionotesvk.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tkachenko.audionotesvk.view.adapters.AudioNotesAdapter
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.entities.AudioNote

class AudioNotesFragment: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var mAdapter = AudioNotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_audio_notes, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val audioNotes: MutableList<AudioNote> = mutableListOf()

        for (i in 0 until 10) {
            audioNotes.add(AudioNote("Audio note $i"))
        }

        mAdapter.setData(audioNotes)
    }

    companion object {
        fun newInstance(): AudioNotesFragment {
            return AudioNotesFragment()
        }
    }
}