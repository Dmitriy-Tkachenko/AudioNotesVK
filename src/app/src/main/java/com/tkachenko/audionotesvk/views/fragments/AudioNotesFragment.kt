package com.tkachenko.audionotesvk.views.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.services.AudioNotePlayerService
import com.tkachenko.audionotesvk.services.AudioNoteUploaderService
import com.tkachenko.audionotesvk.views.adapters.AudioNotesAdapter
import com.tkachenko.audionotesvk.views.viewmodels.AudioNotesViewModel

private const val TAG = "AudioNotesFragment"

class AudioNotesFragment: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var textWelcome: TextView
    private lateinit var mAdapter: AudioNotesAdapter
    private var serviceIntentPlayer: Intent? = null
    private var serviceIntentUploader: Intent? = null
    private var audioNotesService: AudioNotePlayerService? = null
    private var isBound: Boolean? = null
    private var holder: AudioNotesAdapter.AudioNoteHolder? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            audioNotesService = (iBinder as AudioNotePlayerService.MyBinder).service
            isBound = true
            updateProgressIndicator()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            isBound = false
        }
    }

    private val audioNotesViewModel: AudioNotesViewModel by lazy { ViewModelProvider(this)[AudioNotesViewModel::class.java] }

    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceIntentPlayer = Intent(requireActivity(), AudioNotePlayerService::class.java)
        serviceIntentUploader = Intent(requireActivity(), AudioNoteUploaderService::class.java)
        mAdapter = AudioNotesAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_audio_notes, container, false)
        textWelcome = view.findViewById(R.id.text_welcome)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        recyclerView.itemAnimator = null
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioNotesViewModel.fetchAudioNotes().observe(viewLifecycleOwner) { audioNotes ->
            if (audioNotes.isNotEmpty()) {
                mAdapter.setData(audioNotes)
                textWelcome.visibility = View.GONE
            }
        }

        mAdapter.attachCallback(object: AudioNotesAdapter.Callback {
            @SuppressLint("FragmentLiveDataObserve")
            override fun onClickBtnPlay(title: String, position: Int) {
                if (isPlaying) stopAudioService()
                isPlaying = true
                holder = recyclerView.findViewHolderForAdapterPosition(position) as AudioNotesAdapter.AudioNoteHolder
                startAudioService(title)
                mAdapter.startPlay(position)
                audioNotesService?.attachCallback(object : AudioNotePlayerService.Callback {
                    override fun onCompletion() {
                        isPlaying = false
                        stopAudioService()
                        mAdapter.stopPlay(position)
                    }
                })
            }

            override fun onClickBtnStop(title: String, position: Int) {
                isPlaying = false
                stopAudioService()
                mAdapter.stopPlay(position)
            }

            override fun onClickBtnUpload(title: String, position: Int) {
                if (isPlaying) {
                    isPlaying = false
                    stopAudioService()
                    mAdapter.stopPlay(position)
                }
                startUploadService(title)
            }
        })

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val title = mAdapter.removeData(position)
                audioNotesService?.deleteFilee(title)
                Toast.makeText(requireActivity(), "Аудиозапись \"$title\" удалена", Toast.LENGTH_LONG).show()
                if (mAdapter.itemCount == 0) {
                    textWelcome.visibility = View.VISIBLE
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateProgressIndicator() {
        audioNotesService?.maxProgressLiveData?.observe(this@AudioNotesFragment) {
            holder?.setProgressIndicatorMax(it)
        }
        audioNotesService?.progressLiveData?.observe(this@AudioNotesFragment) {
            holder?.updateProgressIndicator(it)
            Log.d(TAG, it.toString())
        }
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
        requireActivity().bindService(serviceIntentPlayer, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindAudioService() {
        requireActivity().unbindService(serviceConnection)
    }

    private fun startAudioService(title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceIntentPlayer?.action = AudioNotePlayerService.START_PLAY
            serviceIntentPlayer?.putExtra(AudioNotePlayerService.TITLE, title)
            requireActivity().startForegroundService(serviceIntentPlayer)
        } else {
            serviceIntentPlayer?.action = AudioNotePlayerService.START_PLAY
            serviceIntentPlayer?.putExtra(AudioNotePlayerService.TITLE, title)
            requireActivity().startService(serviceIntentPlayer)
        }
    }

    private fun stopAudioService() {
        serviceIntentPlayer?.action = AudioNotePlayerService.STOP_PLAY
        requireActivity().startService(serviceIntentPlayer)
    }

    private fun startUploadService(title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceIntentUploader?.action = AudioNoteUploaderService.START_UPLOAD
            serviceIntentUploader?.putExtra(AudioNoteUploaderService.TITLE, title)
            requireActivity().startForegroundService(serviceIntentUploader)
        } else {
            serviceIntentUploader?.action = AudioNoteUploaderService.START_UPLOAD
            serviceIntentUploader?.putExtra(AudioNoteUploaderService.TITLE, title)
            requireActivity().startService(serviceIntentUploader)
        }
    }

    private fun stopUploadService() {
        serviceIntentUploader?.action = AudioNoteUploaderService.STOP_UPLOAD
        requireActivity().startService(serviceIntentUploader)
    }

    companion object {
        fun newInstance() = AudioNotesFragment()
    }
}