package com.tkachenko.audionotesvk.models.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.models.services.AudioNotesService
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class Player(private val context: Context) {

    private val dir: String = context.externalCacheDir?.absolutePath!!
    private var filename: String = ""
    private var extension: String = ""

    private lateinit var serviceIntent: Intent

    private var mutableLiveData: MutableLiveData<String> = MutableLiveData()
    val liveData: LiveData<String> = mutableLiveData

    fun startServiceRecorder() {
        serviceIntent = Intent(context, AudioNotesService::class.java)
        context.registerReceiver(updateTime, IntentFilter(AudioNotesService.TIMER_UPDATE))
        startTimer()
        startWritingAudioFileToDir()
    }

    fun stopServiceRecorder() {
        serviceIntent.action = ACTION_STOP_FOREGROUND
        context.startService(serviceIntent)
    }

    private fun startWritingAudioFileToDir() {
        filename = "sound"
        extension = "pcm"

        val os = FileOutputStream("$dir/$filename.$extension")

        val livedata = AudioNoteRecorder.bytesLiveData
        livedata.observeForever { buffer ->
            os.write(buffer, 0, 8192)
        }
    }

    fun saveFileToDir(fileName: String): Boolean {
        return renameRecordedAudioFile(fileName)
    }

    private fun renameRecordedAudioFile(newFilename: String): Boolean {
        val sourceFile = File(dir, "$filename.$extension")
        val destFile = File(dir, "$newFilename.$extension")
        return sourceFile.renameTo(destFile)
    }

    fun noSaveFileToDir() {
        deleteRecordedAudioFile()
    }

    private fun deleteRecordedAudioFile() {
        val sourceFile = File(dir, "$filename.$extension")
        if (sourceFile.exists()) sourceFile.delete()
    }

    private fun startTimer() {
        val time = -1.0
        serviceIntent.putExtra(AudioNotesService.TIME_EXTRA, time)
        serviceIntent.action = AudioNotesService.TIME_EXTRA
        context.startService(serviceIntent)
        Log.i("TAG", "Start")
    }

    fun showNotification() {
        serviceIntent.action = SHOW_NOTIFICATION
        context.startService(serviceIntent)
    }

    fun cancelNotification() {
        serviceIntent.action = CANCEL_NOTIFICATION
        context.startService(serviceIntent)
    }

    private val updateTime: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val time = intent.getDoubleExtra(AudioNotesService.TIME_EXTRA, -1.0)
            mutableLiveData.postValue(getTimeStringFromDouble(time))
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
        const val ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND"
        const val SHOW_NOTIFICATION = "SHOW_NOTIFICATION"
        const val CANCEL_NOTIFICATION = "CANCEL_NOTIFICATION"
    }
}