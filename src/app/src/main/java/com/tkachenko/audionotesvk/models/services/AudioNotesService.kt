package com.tkachenko.audionotesvk.models.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import androidx.core.app.NotificationCompat
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.models.player.AudioNoteRecorder
import com.tkachenko.audionotesvk.models.player.Player.Companion.ACTION_STOP_FOREGROUND
import com.tkachenko.audionotesvk.models.player.Player.Companion.CANCEL_NOTIFICATION
import com.tkachenko.audionotesvk.models.player.Player.Companion.SHOW_NOTIFICATION
import com.tkachenko.audionotesvk.view.activities.AudioNotesActivity
import java.lang.StringBuilder
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "AudioNotesService"

class AudioNotesService: Service() {

    private val timer = Timer()
    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private val notificationID = 123
    private var isShowNotification = false

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AudioNoteRecorder.createAudioRecorder()
        AudioNoteRecorder.recordStart()
        AudioNoteRecorder.readBytes()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null) {
            if (intent.action.equals(ACTION_STOP_FOREGROUND, ignoreCase = true)) {
                stopForeground(true)
                stopSelf()
            }

            if (intent.action.equals(TIME_EXTRA, ignoreCase = true)) {
                val time = intent.getDoubleExtra(TIME_EXTRA, -1.0)
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            }

            if (intent.action.equals(SHOW_NOTIFICATION, ignoreCase = true)) {
                showNotification()
            }

            if (intent.action.equals(CANCEL_NOTIFICATION, ignoreCase = true)) {
                cancelNotification()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        AudioNoteRecorder.recordStop()
        AudioNoteRecorder.recordRelease()
        super.onDestroy()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, AudioNotesActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intentMainLanding, 0)

            val iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

            notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            builder = NotificationCompat.Builder(this, "service_channel")

            builder!!.setContentTitle(StringBuilder(resources.getString(R.string.notification_title)).toString())
                .setContentText("00:00:00")
                .setSmallIcon(R.drawable.ic_stop_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder!!.setLargeIcon(Bitmap.createScaledBitmap(iconNotification, 128, 128, false))
            }

            notificationManager?.notify(notificationID, builder!!.build())
            isShowNotification = true
        }
    }

    private fun cancelNotification() {
        notificationManager?.cancel(notificationID)
        isShowNotification = false
    }

    private inner class TimeTask(private var time: Double): TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATE)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)

            if (isShowNotification) {
                builder?.setContentText(getTimeStringFromDouble(time))
                notificationManager?.notify(notificationID, builder?.build())
            }
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)

    companion object {
        const val TIMER_UPDATE = "TIMER_UPDATE"
        const val TIME_EXTRA = "TIME_EXTRA"
    }


    /*private lateinit var audioNoteRecorder: AudioNoteRecorder
    private lateinit var resultReceiver: ResultReceiver
    private var l = false

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        audioNoteRecorder = AudioNoteRecorder()
        audioNoteRecorder.createAudioRecorder()
        audioNoteRecorder.recordStart()
        //saveFile()
        l = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras != null) {
            intent.getStringExtra(Constants.EXTRA_FILE_NAME).let { fileName ->
                intent.getParcelableExtra<ResultReceiver>(Constants.EXTRA_RESULT_RECEIVER).let { myResultReceiver ->
                    resultReceiver = myResultReceiver!!

                    val bool = audioNoteRecorder.getTest(fileName.toString())
                    val bundle = Bundle()
                    bundle.putBoolean("compValue", bool)
                    resultReceiver.send(100, bundle)

                    Log.i(TAG, bundle.toString())
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        audioNoteRecorder.recordStop()
        l = false
        audioNoteRecorder.recordRelease()
    }

    /*private fun saveFile() {
        // Добавить проверку
        val bufferSize = 8192
        val os = FileOutputStream(baseContext.externalCacheDir?.absolutePath + "/sound.pcm")

        val audioRecord = audioNoteRecorder.get()

        val buffer = ByteArray(bufferSize)

        Thread {
            while (l) {
                audioRecord.read(buffer, 0, bufferSize)

                os.write(buffer, 0, bufferSize)
            }
        }.start()
    }*/*/
}