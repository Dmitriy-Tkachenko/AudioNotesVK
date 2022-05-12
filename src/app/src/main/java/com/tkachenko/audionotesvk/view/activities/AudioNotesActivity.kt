package com.tkachenko.audionotesvk.view.activities

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.player.Player
import com.tkachenko.audionotesvk.view.fragments.AudioNoteRecordFragment
import com.tkachenko.audionotesvk.view.fragments.AudioNoteSaveDialogFragment
import com.tkachenko.audionotesvk.view.fragments.AudioNotesFragment

private const val TAG = "AudioNotesActivity"
private const val TAG_FRAGMENT_1 = "AudioNotesFragment"
private const val TAG_FRAGMENT_2 = "AudioNoteRecordFragment"
private const val TAG_FRAGMENT_3 = "AudioNoteSaveDialogFragment"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class AudioNotesActivity: AppCompatActivity() {

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private lateinit var btnRecord: ImageView
    private lateinit var btnStop: ImageView
    private var audioNoteRecordFragmentOpened = false
    private lateinit var player: Player

    //private lateinit var resultReceiver: MyResultReceiver

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(TAG_FRAGMENT_2, audioNoteRecordFragmentOpened)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        audioNoteRecordFragmentOpened = savedInstanceState.getBoolean(TAG_FRAGMENT_2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val menuFragment = intent.getStringExtra("menuFragment")
        if (menuFragment != null) {
            if (menuFragment == "favoritesMenuItem") {
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, AudioNoteRecordFragment.newInstance(), TAG_FRAGMENT_2)
                    .commit()
            }
        }*/

        player = Player(context = this)

        btnRecord = findViewById(R.id.btn_record)
        btnStop = findViewById(R.id.btn_stop)

        val isFragmentContainerEmpty = savedInstanceState == null

        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, AudioNotesFragment.newInstance(), TAG_FRAGMENT_1)
                .commit()
        } else if (savedInstanceState?.containsKey(TAG_FRAGMENT_2) == true) {
            if (savedInstanceState.getBoolean(TAG_FRAGMENT_2)) {
                btnRecord.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
            }
        }

        btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
                // Добавить предупреждение
            } else {
                supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_1)?.let { it1 ->
                    val audioNoteRecordFragment = AudioNoteRecordFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_1, R.anim.exit_1)
                        .hide(it1)
                        .add(R.id.fragment_container, audioNoteRecordFragment ,TAG_FRAGMENT_2)
                        .commit()

                    it.visibility = View.GONE
                    btnRecord.visibility = View.GONE
                    btnStop.visibility = View.VISIBLE

                    audioNoteRecordFragmentOpened = true

                    player.startServiceRecorder()

                    player.liveData.observe(
                        this,
                        { time ->
                            audioNoteRecordFragment.updateUI(time.toString())
                        }
                    )
                }
            }
        }

        btnStop.setOnClickListener {
            audioNoteRecordFragmentOpened = false

            player.stopServiceRecorder()

            val audioNoteSaveDialogFragment = AudioNoteSaveDialogFragment()
            audioNoteSaveDialogFragment.show(supportFragmentManager, TAG)

            audioNoteSaveDialogFragment.attachCallback(object : AudioNoteSaveDialogFragment.Callback {
                override fun onClickBtnSave(fileName: String): Boolean {
                    supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_2)?.let { it1 ->
                        supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_1)?.let { it2 ->
                            supportFragmentManager
                                .beginTransaction()
                                .setCustomAnimations(R.anim.enter_2, R.anim.exit_2)
                                .remove(it1)
                                .show(it2)
                                .commit()

                            it.visibility = View.GONE
                            btnStop.visibility = View.GONE
                            btnRecord.visibility = View.VISIBLE
                        }
                    }
                    return player.saveFileToDir(fileName)
                }

                override fun onClickBtnCancelSave() {
                    supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_2)?.let { it1 ->
                        supportFragmentManager.findFragmentByTag(TAG_FRAGMENT_1)?.let { it2 ->
                            supportFragmentManager
                                .beginTransaction()
                                .setCustomAnimations(R.anim.enter_2, R.anim.exit_2)
                                .remove(it1)
                                .show(it2)
                                .commit()

                            it.visibility = View.GONE
                            btnStop.visibility = View.GONE
                            btnRecord.visibility = View.VISIBLE
                        }
                    }
                    player.noSaveFileToDir()
                }
            })
        }
    }

    private var bool = false

    override fun onPause() {
        super.onPause()
        player.showNotification()
        bool = true
    }

    override fun onResume() {
        super.onResume()
        if (bool) player.cancelNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.cancelNotification()
    }
}
