package com.tkachenko.audionotesvk.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tkachenko.audionotesvk.R

class AudioNoteRecordFragment: Fragment() {

    private lateinit var tvTime: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("TAG2", "onAttach")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_audio_note_record, container, false)

        Log.i("TAG2", "onCreateView")
        tvTime = view.findViewById(R.id.tv_time)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG2", "onDestroy")
    }

    fun updateUI(time: String) {
        tvTime.text = time
    }
}