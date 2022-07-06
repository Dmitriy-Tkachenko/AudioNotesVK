package com.tkachenko.audionotesvk.views.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.utils.Utils
import com.tkachenko.audionotesvk.services.AudioNoteRecorderService

private const val TAG = "AudioNoteRecordFragment"

class AudioNoteRecordFragment: Fragment() {
    private lateinit var tvTime: TextView
    private var audioNotesService: AudioNoteRecorderService? = null
    private var serviceIntent: Intent? = null
    private var isBoundService: Boolean? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            audioNotesService = (iBinder as AudioNoteRecorderService.MyBinder).service
            isBoundService = true
            updateTime()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            isBoundService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceIntent = Intent(requireActivity(), AudioNoteRecorderService::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_audio_note_record, container, false)
        tvTime = view.findViewById(R.id.tv_time)
        return view
    }

    override fun onStart() {
        super.onStart()
        bindAudioService()
    }

    override fun onStop() {
        super.onStop()
        unbindAudioService()
    }

    private fun bindAudioService() {
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindAudioService() {
        requireActivity().unbindService(serviceConnection)
    }

    private fun updateTime() {
        audioNotesService?.timerLiveData?.observe(this) { time ->
            tvTime.text = Utils.getTimeStringFromDouble(time)
        }
    }

    companion object {
        fun newInstance() = AudioNoteRecordFragment()
    }
}