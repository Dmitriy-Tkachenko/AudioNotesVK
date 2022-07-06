package com.tkachenko.audionotesvk.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.utils.Utils
import com.tkachenko.audionotesvk.audioplayer.AudioNotePlayer
import com.tkachenko.audionotesvk.repository.VKApi
import com.tkachenko.audionotesvk.views.activities.AudioNotesActivity
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "AudioNotePlayerService"
private const val NOTIFICATION_ID = 101
private const val CHANNEL_ID = "SERVICE_PLAYER_CHANNEL"
private const val CHANNEL_NAME = "SERVICE_PLAYER"

class AudioNotePlayerService: Service() {
    private val vkApi: VKApi by lazy { VKApi() }
    private val myBinder: IBinder = MyBinder()
    private val audioNotePlayer: AudioNotePlayer by lazy { AudioNotePlayer() }
    private var timer: Timer? = null
    private val maxProgressMutableLiveData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    private val progressMutableLiveData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val maxProgressLiveData = maxProgressMutableLiveData
    val progressLiveData = progressMutableLiveData

    private var callback: Callback? = null

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
            setContentTitle("")
            setContentText("00:00")
            setSmallIcon(R.drawable.ic_stop_24)
            setContentIntent(pendingIntent)
            setOngoing(true)
        }
    }
    private var isShowNotification = false
    private var title = ""

    fun attachCallback(callback: Callback) {
        this.callback = callback
    }

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
        stopPlay()
        stopTimer()
        stopNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        audioNotePlayer.createAudioPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        if (intent?.action != null) {
            if (intent.action.equals(START_PLAY, ignoreCase = true)) {
                title = intent.getStringExtra(TITLE).toString()
                startPlay(title)
                startTimer()
                startNotification()
                updateContentTitleNotification(title)
            }
            if (intent.action.equals(STOP_PLAY, ignoreCase = true)) {
                Log.d(TAG, "stop")
                stopPlay()
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

    private fun startPlay(title: String) {
        audioNotePlayer.playStart("${externalCacheDir?.absolutePath!!}/$title.wav")
        audioNotePlayer.onCompletion {
            maxProgressLiveData.postValue(0)
            callback?.onCompletion()
            it?.reset()
            it?.stop()
            stopTimer()
            stopNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true)
            }
            stopSelf()
        }
    }

    private fun stopPlay() {
        maxProgressLiveData.postValue(0)
        audioNotePlayer.playStop()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(TimeTask(), 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    fun deleteFilee(title: String) {
        audioNotePlayer.deleteAudioFile(title)
    }

    private inner class TimeTask : TimerTask() {
        private val duration: Int? = audioNotePlayer.getDuration()?.div(1000)

        init {
            maxProgressMutableLiveData.postValue(duration)
        }

        override fun run() {
            val currentProgress = audioNotePlayer.getCurrentPosition()?.toDouble()?.div(1000)?.roundToInt()
            if (currentProgress != null) {
                if (currentProgress >= 0)
                    progressMutableLiveData.postValue(currentProgress)

                if (isShowNotification) {
                    updateNotification("${Utils.getTimeStringFromInt(currentProgress)} / ${Utils.getTimeStringFromInt(duration!!)}")
                }
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
        }
        notificationManager.cancel(NOTIFICATION_ID)
        notificationBuilder.clearPeople()
        isShowNotification = false
    }

    private fun updateNotification(notificationText: String? = null) {
        notificationText.let { notificationBuilder.setContentText(it) }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateContentTitleNotification(title: String) {
        notificationBuilder.setContentTitle(title)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    inner class MyBinder: Binder() {
        val service: AudioNotePlayerService
            get() = this@AudioNotePlayerService
    }

    interface Callback {
        fun onCompletion()
    }

    companion object {
        const val START_PLAY = "START_PLAY"
        const val STOP_PLAY = "STOP_PLAY"
        const val START_UPLOAD = "START_UPLOAD"
        const val STOP_UPLOAD = "STOP_UPLOAD"
        const val TITLE = "TITLE"
    }
}