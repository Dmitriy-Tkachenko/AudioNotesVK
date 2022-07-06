package com.tkachenko.audionotesvk.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AudioNotesRepository {
    private var audioNotesApi = AudioNotesApi()
    //private var vkApi = VKApi()
    suspend fun fetchAudioNotes() = withContext(Dispatchers.IO) { audioNotesApi.fetchAudioNotes() }
    //fun uploadAudioNoteFile(file: File) { vkApi.uploadAudioNote(file) }
}