package com.tkachenko.audionotesvk.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.services.AudioNoteRecorderService
import com.tkachenko.audionotesvk.views.fragments.AudioNoteRecordFragment
import com.tkachenko.audionotesvk.views.fragments.AudioNoteSaveDialogFragment
import com.tkachenko.audionotesvk.views.fragments.AudioNotesFragment

private const val TAG = "AudioNotesActivity"
private const val AUDIO_NOTES_FRAGMENT = "AudioNotesFragment"
private const val AUDIO_NOTE_RECORD_FRAGMENT = "AudioNoteRecordFragment"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val BOOL = "BOOL"

class AudioNotesActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private lateinit var btnRecord: ImageView
    private lateinit var btnStop: ImageView

    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var isRecording = false
    private var serviceIntent: Intent? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_notes)

        serviceIntent = Intent(this, AudioNoteRecorderService::class.java)

        btnRecord = findViewById(R.id.btn_record)
        btnStop = findViewById(R.id.btn_stop)

        stateRestoration(savedInstanceState)

        btnRecord.setOnClickListener(this)
        btnRecord.setOnTouchListener(this)
        btnStop.setOnClickListener(this)
        btnStop.setOnTouchListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BOOL, isRecording)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isRecording = savedInstanceState.getBoolean(BOOL)
    }

    private fun stateRestoration(savedInstanceState: Bundle?) {
        val isFragmentContainerEmpty = savedInstanceState == null

        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, AudioNotesFragment(), AUDIO_NOTES_FRAGMENT)
                .commit()
        } else if (savedInstanceState?.containsKey(BOOL) == true) {
            if (savedInstanceState.getBoolean(BOOL)) {
                btnRecord.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
            }
        }
    }

    private fun onClickBtnRecord() {
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            supportFragmentManager.findFragmentByTag(AUDIO_NOTES_FRAGMENT)?. let { audioNotesFragment ->
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_1, R.anim.exit_1)
                    .remove(audioNotesFragment)
                    .add(R.id.fragment_container, AudioNoteRecordFragment.newInstance(), AUDIO_NOTE_RECORD_FRAGMENT)
                    .commit()

                btnRecord.visibility = View.GONE
                btnStop.visibility = View.VISIBLE

                startAudioService()
                isRecording = true
            }
        }
    }

    private fun onClickBtnStop() {
        stopAudioService()
        isRecording = false

        val audioNoteSaveDialogFragment = AudioNoteSaveDialogFragment.newInstance()
        audioNoteSaveDialogFragment.show(supportFragmentManager, TAG)

        audioNoteSaveDialogFragment.attachCallback(object : AudioNoteSaveDialogFragment.Callback {
            override fun onClickBtnSave() {
                supportFragmentManager.findFragmentByTag(AUDIO_NOTE_RECORD_FRAGMENT)?.let { audioNoteRecordFragment ->
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_2, R.anim.exit_2)
                        .remove(audioNoteRecordFragment)
                        .add(R.id.fragment_container, AudioNotesFragment.newInstance(), AUDIO_NOTES_FRAGMENT)
                        .commit()

                    btnStop.visibility = View.GONE
                    btnRecord.visibility = View.VISIBLE
                }
            }

            override fun onClickBtnCancelSave() {
                supportFragmentManager.findFragmentByTag(AUDIO_NOTE_RECORD_FRAGMENT)?.let { audioNoteRecordFragment ->
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_2, R.anim.exit_2)
                        .remove(audioNoteRecordFragment)
                        .add(R.id.fragment_container, AudioNotesFragment.newInstance(), AUDIO_NOTES_FRAGMENT)
                        .commit()

                    btnStop.visibility = View.GONE
                    btnRecord.visibility = View.VISIBLE
                }
            }

        })
    }

    private fun startAudioService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceIntent?.action = AudioNoteRecorderService.START_SERVICE
            startForegroundService(serviceIntent)
        } else {
            serviceIntent?.action = AudioNoteRecorderService.STOP_SERVICE
            startService(serviceIntent)
        }
    }

    private fun stopAudioService() {
        serviceIntent?.action = AudioNoteRecorderService.STOP_SERVICE
        startService(serviceIntent)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_record -> {
                onClickBtnRecord()
            }
            R.id.btn_stop -> {
                onClickBtnStop()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (view?.id) {
            R.id.btn_record -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        (view as ImageView).imageAlpha = 200
                    }
                    MotionEvent.ACTION_UP -> {
                        (view as ImageView).imageAlpha = 255
                    }
                }
            }
            R.id.btn_stop -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        (view as ImageView).imageAlpha = 200
                    }
                    MotionEvent.ACTION_UP -> {
                        (view as ImageView).imageAlpha = 255
                    }
                }
            }
        }
        return false
    }
}