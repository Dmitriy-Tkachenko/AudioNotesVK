package com.tkachenko.audionotesvk.models.player

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val TAG = "AudioNoteRecorder"

object AudioNoteRecorder {
    private var audioRecord: AudioRecord? = null
    private var isReading = false
    private const val bufferSize = 8192
    private val buffer = ByteArray(bufferSize)

    private var bytesMutableLiveData: MutableLiveData<ByteArray> = MutableLiveData()
    var bytesLiveData: LiveData<ByteArray> = bytesMutableLiveData

    @SuppressLint("MissingPermission")
    fun createAudioRecorder(){
        Log.i(TAG, "record create")
        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minInternalBufferSize = AudioRecord.getMinBufferSize(
            sampleRate, channelConfig, audioFormat)
        val internalBufferSize = minInternalBufferSize * 4

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
            sampleRate, channelConfig, audioFormat, internalBufferSize)
    }

    fun recordStart() {
        Log.i(TAG, "record start")
        isReading = true
        audioRecord?.startRecording()
    }

    fun recordStop() {
        Log.i(TAG, "record stop")
        isReading = false
        audioRecord?.stop()
    }

    fun readBytes() {
        Log.i(TAG, "read")
        Thread {
            while (isReading) {
                audioRecord?.read(buffer, 0, bufferSize)
                bytesMutableLiveData.postValue(buffer)
                var s = ""
                for (buff in buffer) {
                    s += "$buff, "
                }
                Log.i(TAG, s)
            }
        }.start()
    }

    fun recordRelease() {
        Log.i(TAG, "record release")
        isReading = false
        audioRecord?.release()
    }

    /*private val bufferSize = 8192
    private var isReading = false
    private lateinit var audioRecord: AudioRecord

    @SuppressLint("MissingPermission")
    fun createAudioRecorder() {
        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minInternalBufferSize = AudioRecord.getMinBufferSize(
            sampleRate, channelConfig, audioFormat)
        val internalBufferSize = minInternalBufferSize * 4
        /*Log.i(TAG, "minInternalBufferSize = $minInternalBufferSize " +
                "internalBufferSize = $internalBufferSize " +
                "bufferSize = $bufferSize")(*/

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
        sampleRate, channelConfig, audioFormat, internalBufferSize)
    }

    fun recordStart() {
        Log.i(TAG, "record start")
        audioRecord.startRecording()
        val recordingStage = audioRecord.recordingState
        Log.i(TAG, "recordingState = $recordingStage")
    }

    fun recordStop() {
        Log.i(TAG, "record stop")
        audioRecord.stop()
    }

    fun getTest(fileName: String): Boolean {
        return fileName != ""
    }

    /*fun saveFile(): Int {
        /*Log.i(TAG, "read start")
        isReading = true
        Thread {
            val buffer = ByteArray(bufferSize)

            while (isReading) {
                readCount = audioRecord.read(buffer, 0, bufferSize)
            }
        }.start()*/

        /*isReading = true

        val buffer = ShortArray(bufferSize)*/

        val readResult: Int = audioRecord.read(buffer, 0, bufferSize)

        /*for (i in 0..readResult) {
            dos.writeShort(buffer[i].toInt())
        }*/
        return readResult
    }*/

    fun readStop() {
        Log.i(TAG, "read stop")
        isReading = false
    }

    fun recordRelease() {
        isReading = false
        audioRecord.release()
    }*/
}