package com.example.whatsapptranslate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_language_selection)

            val spinnerLanguages: Spinner = findViewById(R.id.spinner_languages)
            val btnSave: Button = findViewById(R.id.btn_save_language)

            // Configuración del Spinner
            val languages = arrayOf("Inglés a Español")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguages.adapter = adapter

            btnSave.setOnClickListener {
                // Aquí guardamos la selección de traducción en SharedPreferences
                val selectedTranslation = spinnerLanguages.selectedItem.toString()
                saveTranslationPreference(selectedTranslation)

                // Activar la API de traducción de inglés a español
                activateTranslationAPI()

                // Redirigir a la siguiente actividad
                startActivity(Intent(this, PermissionSettingsActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e("LanguageSelectionActivity", "Error en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la aplicación: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun saveTranslationPreference(translation: String) {
        val sharedPreferences = getSharedPreferences("WhatsAppTranslatePrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selected_translation", translation)
        editor.apply()
    }

    private fun activateTranslationAPI() {
        // Aquí puedes agregar el código para activar la API de traducción de inglés a español
        // Por ahora, solo mostraremos un mensaje de log
        Log.d("LanguageSelectionActivity", "API de traducción de inglés a español activada")
    }
}
