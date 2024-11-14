package com.example.whatsapptranslate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class TranslationDialogFragment : DialogFragment() {
    private lateinit var originalMessageView: TextView
    private lateinit var translatedMessageView: TextView
    private lateinit var responseEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_translation, container, false)

        originalMessageView = view.findViewById(R.id.tv_original_message)
        translatedMessageView = view.findViewById(R.id.tv_translated_message)
        responseEditText = view.findViewById(R.id.et_response)
        sendButton = view.findViewById(R.id.btn_send)

        sendButton.setOnClickListener {
            // Lógica para enviar la respuesta traducida
            val response = responseEditText.text.toString()
            // Aquí iría la lógica para traducir y enviar la respuesta
        }

        return view
    }

    fun updateOriginalMessage(message: String) {
        originalMessageView.text = message
    }

    fun updateTranslatedMessage(message: String) {
        translatedMessageView.text = message
    }
}
