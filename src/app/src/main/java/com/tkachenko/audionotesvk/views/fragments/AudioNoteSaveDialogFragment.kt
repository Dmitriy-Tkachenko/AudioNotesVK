package com.tkachenko.audionotesvk.views.fragments

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.tkachenko.audionotesvk.InvalidFileNameException
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.services.AudioNoteRecorderService

private const val TAG = "AudioNoteSaveDialogFragment"

class AudioNoteSaveDialogFragment: DialogFragment() {
    private lateinit var inputLayoutFileName: TextInputLayout
    private lateinit var editTextFileName: EditText
    private lateinit var btnSaveFile: Button
    private lateinit var btnCancelSaveFile: Button
    private lateinit var callback: Callback
    private var serviceIntent: Intent? = null
    private var audioNotesService: AudioNoteRecorderService? = null
    private var isBound: Boolean? = null
    private val serviceConnection = object : ServiceConnection {
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            audioNotesService = (iBinder as AudioNoteRecorderService.MyBinder).service
            isBound = true
            setAudioFileName()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    fun attachCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceIntent = Intent(requireActivity(), AudioNoteRecorderService::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_save_audio, container)
        inputLayoutFileName = view.findViewById(R.id.il_file_name)
        editTextFileName = view.findViewById(R.id.et_file_name)
        btnSaveFile = view.findViewById(R.id.btn_save_file)
        btnCancelSaveFile = view.findViewById(R.id.btn_cancel_save_file)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

        btnSaveFile.setOnClickListener {
            saveAudioFile()
        }

        btnCancelSaveFile.setOnClickListener {
            noSaveAudioFile()
        }
    }

    override fun onStart() {
        super.onStart()
        bindAudioService()
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        saveAudioFile()
    }

    private fun bindAudioService() {
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindAudioService() {
        requireActivity().unbindService(serviceConnection)
    }

    private fun saveAudioFile() {
        val fileName = editTextFileName.text.toString()
        try {
            audioNotesService?.saveFile(fileName)
            callback.onClickBtnSave()
            unbindAudioService()
            dismiss()
        } catch (e: InvalidFileNameException) {
            inputLayoutFileName.error = e.message
        }
    }

    private fun noSaveAudioFile() {
        audioNotesService?.noSaveFile()
        callback.onClickBtnCancelSave()
        unbindAudioService()
        dismiss()
    }

    private fun setAudioFileName() {
        editTextFileName.setText(audioNotesService?.getDefaultFileName())
    }

    companion object {
        fun newInstance() = AudioNoteSaveDialogFragment()
    }

    interface Callback {
        fun onClickBtnSave()
        fun onClickBtnCancelSave()
    }
}