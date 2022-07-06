package com.tkachenko.audionotesvk.audioplayer

import android.media.*
import android.util.Log
import com.tkachenko.audionotesvk.utils.Constants
import com.tkachenko.audionotesvk.views.adapters.AudioNotesAdapter
import java.io.File
import java.io.FileInputStream

private const val TAG = "AudioNotePlayer"

class AudioNotePlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun createAudioPlayer() {
        mediaPlayer = MediaPlayer()
    }

    fun playStart(dir: String) {
        playStop()
        mediaPlayer?.apply {
            setDataSource(dir)
            prepare()
            start()
        }
    }

    fun playStop() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }

    fun onCompletion(onCompletionListener: MediaPlayer.OnCompletionListener) {
        mediaPlayer?.setOnCompletionListener(onCompletionListener)
    }

    fun getDuration() = mediaPlayer?.duration

    fun getCurrentPosition() = mediaPlayer?.currentPosition

    fun deleteAudioFile(fileName: String): Boolean {
        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_WAV}")
        return if (sourceFile.exists()) {
            sourceFile.delete()
        } else false
    }
}