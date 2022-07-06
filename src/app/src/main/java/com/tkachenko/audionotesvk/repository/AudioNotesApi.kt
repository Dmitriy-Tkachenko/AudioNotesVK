package com.tkachenko.audionotesvk.repository

import android.media.MediaMetadataRetriever
import com.tkachenko.audionotesvk.models.AudioNote
import com.tkachenko.audionotesvk.utils.Constants
import com.tkachenko.audionotesvk.utils.Utils
import java.io.File
import java.util.*

private const val TAG = "AudioNotesApi"

class AudioNotesApi {
    fun fetchAudioNotes(): List<AudioNote> {
        val audioNotes: MutableList<AudioNote> = mutableListOf()
        val files: Array<File>? = Constants.DIR?.let { File(it).listFiles() }

        files?.let {
            Arrays.sort(it) { f1, f2 -> (f2.lastModified().compareTo(f1.lastModified())) }
            files.forEach { file ->
                if (file.nameWithoutExtension.isNotEmpty()) {
                    val date = getData(file.absoluteFile)
                    val duration = getDuration(file.absoluteFile)
                    audioNotes.add(AudioNote(title = file.nameWithoutExtension, date = date, duration = duration, path = file.absolutePath))
                }
            }
        }
        return audioNotes
    }

    private fun getDuration(file: File): String {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        return Utils.getConvertMillisecondsToTimes(mediaMetadataRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong())
    }

    private fun getData(file: File): String {
        return Utils.getDateByPattern(file.lastModified())
    }
}