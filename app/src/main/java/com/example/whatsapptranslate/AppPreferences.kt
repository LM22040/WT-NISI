package com.example.whatsapptranslate

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "WhatsAppTranslatePreferences"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // Lista de claves para SharedPreferences
    private const val SETUP_COMPLETED = "setup_completed"
    private const val ACCESSIBILITY_REQUESTED = "accessibility_requested"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var setupCompleted: Boolean
        get() = preferences.getBoolean(SETUP_COMPLETED, false)
        set(value) = preferences.edit {
            it.putBoolean(SETUP_COMPLETED, value)
        }

    var accessibilityRequested: Boolean
        get() = preferences.getBoolean(ACCESSIBILITY_REQUESTED, false)
        set(value) = preferences.edit {
            it.putBoolean(ACCESSIBILITY_REQUESTED, value)
        }
}
