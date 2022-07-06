package com.tkachenko.audionotesvk.audioplayer

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.tkachenko.audionotesvk.utils.Constants
import com.tkachenko.audionotesvk.InvalidFileNameException
import com.tkachenko.audionotesvk.MyApplication
import com.tkachenko.audionotesvk.R
import java.io.*

private const val TAG = "AudioNoteRecorder"

class AudioNoteRecorder {
    private var audioRecord: AudioRecord? = null
    private val bufferSize = 8192
    private val buffer = ByteArray(bufferSize)
    private var isRecording = false
    private var fileName = ""

    @SuppressLint("MissingPermission")
    fun createAudioRecorder() {
        Log.d(TAG, "record create")
        val audioSource = MediaRecorder.AudioSource.MIC
        val audioSampleRate = 16000
        val audioChannel = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val internalBufferSize = audioSampleRate * 30
        audioRecord = AudioRecord(audioSource, audioSampleRate, audioChannel, audioFormat, internalBufferSize)
    }

    fun recordStart() {
        Log.d(TAG, "record start")
        audioRecord?.startRecording().let {
            isRecording = true
            startWritingAudioFile()
        }
    }

    fun recordStop() {
        Log.d(TAG, "record stop")
        audioRecord?.let {
            isRecording = false
            it.stop()
            pcmToWav(File("${Constants.DIR}/$fileName.${Constants.EXT_PCM}"), File("${Constants.DIR}/$fileName.${Constants.EXT_WAV}"))
        }
    }

    fun recordRelease() {
        Log.d(TAG, "record release")
        audioRecord?.release()
    }

    fun saveAudioFile(newFilename: String) {
        renameRecordedAudioFile(newFilename)
    }

    fun noSaveFile(): Boolean {
        return deleteRecordedAudioFile()
    }

    fun getDefaultFileName(): String {
        return fileName
    }

    private fun startWritingAudioFile() {
        Log.i(TAG, "startWrite")
        setUniqueFile()
        val os = FileOutputStream("${Constants.DIR}/$fileName.${Constants.EXT_PCM}")
        Thread {
            while (isRecording) {
                audioRecord?.read(buffer, 0, bufferSize)
                os.write(buffer, 0, 8192)
            }
        }.start()
    }

    private fun setUniqueFile() {
        fileName = "Новая запись 1"

        var fileWav = File(Constants.DIR, "$fileName.${Constants.EXT_WAV}")
        var numbFile = 2

        while (fileWav.exists()) {
            fileName = "Новая запись $numbFile"
            fileWav = File(Constants.DIR, "$fileName.${Constants.EXT_WAV}")
            numbFile++
        }
    }

    private fun deleteRecordedAudioFile(): Boolean {
        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_WAV}")
        return if (sourceFile.exists()) {
            sourceFile.delete()
        } else false
    }

    private fun renameRecordedAudioFile(newFilename: String) {
        if (newFilename.isEmpty()) {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_empty))
        } else if (checkFileExist(newFilename)) {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_exist))
        }

        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_WAV}")
        val destFile = File(Constants.DIR, "$newFilename.${Constants.EXT_WAV}")

        if (sourceFile.renameTo(destFile)) {
            fileName = newFilename
        } else {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_error))
        }
    }

    private fun checkFileExist(newFileName: String): Boolean {
        if (newFileName == fileName) return false

        val file = File(Constants.DIR, "$newFileName.${Constants.EXT_WAV}")
        return file.exists()
    }

    private fun pcmToWav(pcmFile: File, wavFile: File) {
        var inp: FileInputStream? = null
        var out: FileOutputStream? = null
        val totalAudioLen: Long
        val totalDataLen: Long
        val longSampleRate: Long = 16000
        val channels = 1
        val byteRate: Long = (16 * 16000 * channels / 8).toLong()
        val data = ByteArray(30 * 16000)

        try {
            inp = FileInputStream(pcmFile)
            out = FileOutputStream(wavFile)
            totalAudioLen = inp.channel.size()
            totalDataLen = totalAudioLen + 36
            writeWavFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate)
            while (inp.read(data) != - 1) {
                out.write(data)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "convertWavFile: " + e.message)
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d(TAG, "IOException: " + e.message)
            e.printStackTrace()
        } finally {
            try {
                inp?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (pcmFile.isFile && pcmFile.exists()) {
                pcmFile.delete()
            }
        }
    }

    private fun writeWavFileHeader(out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] =  (totalDataLen and 0xff).toByte()
        header[5] =  ((totalDataLen shr 8) and 0xff).toByte()
        header[6] =  ((totalDataLen shr 16) and 0xff).toByte()
        header[7] =  ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (1 * 16 / 8).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        out.write(header, 0, 44)
    }
}