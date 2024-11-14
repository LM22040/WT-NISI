package com.example.whatsapptranslate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var fabTranslate: FloatingActionButton
    private var isTranslationActive = false

    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppPreferences.init(this)

        if (AppPreferences.setupCompleted) {
            startActivity(Intent(this, ActivationActivity::class.java))
        } else {
            startActivity(Intent(this, PermissionSettingsActivity::class.java))
        }
        finish()
    }

    private fun toggleTranslation() {
        isTranslationActive = !isTranslationActive
        if (isTranslationActive) {
            activateTranslation()
        } else {
            deactivateTranslation()
        }
    }

    private fun activateTranslation() {
        // Mostrar el cuadro de diálogo de traducción
        val dialogFragment = TranslationDialogFragment()
        dialogFragment.show(supportFragmentManager, "TranslationDialog")
    }

    private fun deactivateTranslation() {
        // Cerrar el cuadro de diálogo de traducción si está abierto
        val dialogFragment = supportFragmentManager.findFragmentByTag("TranslationDialog") as? TranslationDialogFragment
        dialogFragment?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                // El permiso fue concedido, ahora puedes mostrar la burbuja flotante
                val intent = Intent("com.example.whatsapptranslate.SHOW_FLOATING_BUBBLE")
                sendBroadcast(intent)
                Toast.makeText(this, "Traducción habilitada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede mostrar la burbuja flotante.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
