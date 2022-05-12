package com.tkachenko.audionotesvk.view.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.tkachenko.audionotesvk.R

private const val CALLBACK = "CALLBACK"

class AudioNoteSaveDialogFragment: DialogFragment() {
    private lateinit var inputLayoutFileName: TextInputLayout
    private lateinit var editTextFileName: EditText
    private lateinit var btnSaveFile: Button
    private lateinit var btnCancelSaveFile: Button
    private lateinit var callback: Callback

    fun attachCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState?.containsKey(CALLBACK) == true) {

        }
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

        btnSaveFile.setOnClickListener {
            /*if (editTextFileName.text.isEmpty() || editTextFileName.text.matches(Regex("\\s+"))) {
                inputLayoutFileName.error = "Введите название"
            }*/
            var s = editTextFileName.text.toString()
            val isCorrectFileName = callback.onClickBtnSave(s)
            if (!isCorrectFileName) {
                inputLayoutFileName.error = "Введите название"
            } else {
                dismiss()
            }
        }

        btnCancelSaveFile.setOnClickListener {
            callback.onClickBtnCancelSave()
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    interface Callback {
        fun onClickBtnSave(fileName: String): Boolean
        fun onClickBtnCancelSave()
    }
}