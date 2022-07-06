package com.tkachenko.audionotesvk.views.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tkachenko.audionotesvk.repository.AudioNotesRepository
import com.tkachenko.audionotesvk.models.AudioNote
import java.io.File

class AudioNotesViewModel: ViewModel() {
    private lateinit var audioNotes: List<AudioNote>
    private lateinit var unit: Unit
    fun fetchAudioNotes() = liveData {
        if (!::audioNotes.isInitialized) {
            audioNotes = AudioNotesRepository().fetchAudioNotes()
            emit(audioNotes)
        } else {
            emit(audioNotes)
        }
    }
}