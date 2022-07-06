package com.tkachenko.audionotesvk.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.utils.Utils
import com.tkachenko.audionotesvk.audioplayer.AudioNoteRecorder
import com.tkachenko.audionotesvk.views.activities.AudioNotesActivity
import java.util.*

private const val TAG = "AudioNotesService"
private const val NOTIFICATION_ID = 100
private const val CHANNEL_ID = "SERVICE_RECORDER_CHANNEL"
private const val CHANNEL_NAME = "SERVICE_RECORDER"

class AudioNoteRecorderService: Service() {
    private val myBinder: IBinder = MyBinder()
    private val audioNoteRecorder: AudioNoteRecorder by lazy { AudioNoteRecorder() }
    private var timer: Timer? = null
    private var timerMutableLiveData: MutableLiveData<Double> = MutableLiveData()
    var timerLiveData: LiveData<Double> = timerMutableLiveData
    private val pendingIntent by lazy {
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, AudioNotesActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_MAX
            setContentTitle(StringBuilder(resources.getString(R.string.notification_title)).toString())
            setContentText("00:00")
            setSmallIcon(R.drawable.ic_stop_24)
            setContentIntent(pendingIntent)
            setOngoing(true)
        }
    }
    private var isShowNotification = false

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return myBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onRebind")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopRecord()
        stopTimer()
        stopNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        if (intent?.action != null) {
            if (intent.action.equals(START_SERVICE, ignoreCase = true)) {
                startRecord()
                startTimer()
                startNotification()
            }
            if (intent.action.equals(STOP_SERVICE, ignoreCase = true)) {
                stopRecord()
                stopTimer()
                stopNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopForeground(true)
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecord() {
        Log.d(TAG, "startRecord")
        audioNoteRecorder.createAudioRecorder()
        audioNoteRecorder.recordStart()
    }

    private fun stopRecord() {
        Log.d(TAG, "stopRecord")
        audioNoteRecorder.recordStop()
        audioNoteRecorder.recordRelease()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(TimeTask(-1.0), 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() = NotificationChannel(
        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        setSound(null, null)
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
        isShowNotification = true
    }

    private fun stopNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        isShowNotification = false
    }

    private fun updateNotification(notificationText: String? = null) {
        notificationText.let { notificationBuilder.setContentText(it) }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun saveFile(fileName: String) {
        audioNoteRecorder.saveAudioFile(fileName)
    }

    fun noSaveFile(): Boolean {
        return audioNoteRecorder.noSaveFile()
    }

    fun getDefaultFileName(): String {
        return audioNoteRecorder.getDefaultFileName()
    }

    private inner class TimeTask(private var time: Double): TimerTask() {
        override fun run() {
            time++
            timerMutableLiveData.postValue(time)

            if (isShowNotification) {
                updateNotification(Utils.getTimeStringFromDouble(time))
            }
        }
    }

    inner class MyBinder: Binder() {
        val service: AudioNoteRecorderService
            get() = this@AudioNoteRecorderService
    }

    companion object {
        const val START_SERVICE = "START_SERVICE"
        const val STOP_SERVICE = "STOP_SERVICE"
    }
}