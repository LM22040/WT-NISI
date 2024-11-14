package com.example.whatsapptranslate

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ActivationActivity : AppCompatActivity() {

    // Declarar el código de solicitud de permiso de overlay
    private val REQUEST_CODE_OVERLAY_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        AppPreferences.init(this)

        if (!AppPreferences.setupCompleted) {
            startActivity(Intent(this, PermissionSettingsActivity::class.java))
            finish()
            return
        }

        // Verificar si todos los permisos están concedidos
        if (!allPermissionsGranted()) {
            // Si no están todos los permisos, volver a PermissionSettingsActivity
            startActivity(Intent(this, PermissionSettingsActivity::class.java))
            finish()
            return
        }

        // Verificar el permiso de overlay (ventana flotante)
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        val btnOpenWhatsapp = findViewById<Button>(R.id.btn_open_whatsapp)
        btnOpenWhatsapp.setOnClickListener {
            launchWhatsApp()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "El permiso de ventana flotante es necesario.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permiso de ventana flotante concedido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityServiceName = packageName + "/" + WhatsAppAccessibilityService::class.java.canonicalName
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(accessibilityServiceName) == true
    }

    private fun promptEnableAccessibilityService() {
        Toast.makeText(this, "Por favor, activa el servicio de accesibilidad para WhatsApp Translate.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun launchWhatsApp() {
        val whatsappPackageName = "com.whatsapp"
        val whatsappBusinessPackageName = "com.whatsapp.w4b"

        try {
            // Primero intenta abrir WhatsApp regular
            if (isPackageInstalled(whatsappPackageName)) {
                openApp(whatsappPackageName)
                return
            }

            // Si WhatsApp regular no está instalado, intenta con WhatsApp Business
            if (isPackageInstalled(whatsappBusinessPackageName)) {
                openApp(whatsappBusinessPackageName)
                return
            }

            // Si ninguna versión está instalada, abre la Play Store
            openPlayStore(whatsappPackageName)
        } catch (e: Exception) {
            Log.e("ActivationActivity", "Error al abrir WhatsApp: ${e.message}")
            Toast.makeText(this, "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show()
            openPlayStore(whatsappPackageName)
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun openApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            if (intent != null) {
                startActivity(intent)
                Log.d("ActivationActivity", "Aplicación lanzada correctamente: $packageName")
            } else {
                throw Exception("No se pudo crear el intent para: $packageName")
            }
        } catch (e: Exception) {
            Log.e("ActivationActivity", "Error al abrir la aplicación: ${e.message}")
            // Si falla, intenta el método alternativo
            openAppAlternative(packageName)
        }
    }

    private fun openAppAlternative(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }

            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            if (resolveInfo.isNotEmpty()) {
                val activityInfo = resolveInfo[0].activityInfo
                val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
                intent.component = componentName
                startActivity(intent)
                Log.d("ActivationActivity", "Aplicación lanzada con método alternativo: $packageName")
            } else {
                throw Exception("No se encontraron actividades para: $packageName")
            }
        } catch (e: Exception) {
            Log.e("ActivationActivity", "Error en método alternativo: ${e.message}")
            openPlayStore(packageName)
        }
    }

    private fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Si la Play Store no está instalada, abre en el navegador
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(webIntent)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        // Implementa esta función para verificar todos los permisos necesarios
        return Settings.canDrawOverlays(this) && isAccessibilityServiceEnabled()
    }

    private fun enableTranslation() {
        val intent = Intent("com.example.whatsapptranslate.SHOW_FLOATING_BUBBLE")
        sendBroadcast(intent)
    }
}
