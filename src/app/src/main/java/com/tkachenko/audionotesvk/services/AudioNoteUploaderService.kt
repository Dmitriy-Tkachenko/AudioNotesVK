package com.tkachenko.audionotesvk.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.repository.VKApi
import com.tkachenko.audionotesvk.views.activities.AudioNotesActivity

private const val TAG = "AudioNoteUploadService"
private const val NOTIFICATION_ID = 102
private const val CHANNEL_ID = "SERVICE_UPLOAD_CHANNEL"
private const val CHANNEL_NAME = "SERVICE_UPLOAD"

class AudioNoteUploaderService: LifecycleService() {
    private val vkApi: VKApi by lazy { VKApi() }
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
            setContentTitle("Загрузка файла")
            setContentText("Это займет некоторое время")
            setSmallIcon(R.drawable.ic_stop_24)
            setContentIntent(pendingIntent)
            setOngoing(true)
        }
    }
    private var isShowNotification = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d(TAG, "onStartCommand")

        if (intent?.action != null) {
            if (intent.action.equals(START_UPLOAD, ignoreCase = true)) {
                val title = intent.getStringExtra(TITLE).toString()
                startUploadAudioNote(title)
                startNotification()
            }
            if (intent.action.equals(STOP_UPLOAD, ignoreCase = true)) {
                stopNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopForeground(true)
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startUploadAudioNote(title: String) {
        vkApi.uploadAudioNote(title)
        vkApi.uploadLiveData.observe(this) {
            if (it) {
                Toast.makeText(this, "Файл загружен", Toast.LENGTH_SHORT).show()
                stopNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopForeground(true)
                }
                stopSelf()
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
        notificationManager.cancel(NOTIFICATION_ID)
        isShowNotification = false
    }

    companion object {
        const val START_UPLOAD = "START_UPLOAD"
        const val STOP_UPLOAD = "STOP_UPLOAD"
        const val TITLE = "TITLE"
    }
}