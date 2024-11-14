package com.example.whatsapptranslate

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import android.view.animation.AnimationUtils
import com.google.mlkit.nl.languageid.LanguageIdentification
import android.util.Log
import android.os.Build

class WhatsAppAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var floatingBubble: View? = null
    private var isTranslating = false
    private lateinit var originalText: String
    private lateinit var showBubbleReceiver: BroadcastReceiver
    private var isWhatsAppForeground = false

    private val client = OkHttpClient()
    private val apiUrl = "https://api-inference.huggingface.co/models/Helsinki-NLP/opus-mt-en-es"
    private val apiKey = "TU_API_KEY_AQUI" // Reemplaza esto con tu API key de Hugging Face

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    override fun onCreate() {
        super.onCreate()
        registerShowBubbleReceiver()
    }

    private fun registerShowBubbleReceiver() {
        showBubbleReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.whatsapptranslate.SHOW_FLOATING_BUBBLE") {
                    if (Settings.canDrawOverlays(context)) {
                        showFloatingBubble()
                    } else {
                        Toast.makeText(context, "Permiso de superposición no concedido", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val filter = IntentFilter("com.example.whatsapptranslate.SHOW_FLOATING_BUBBLE")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(showBubbleReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(showBubbleReceiver, filter)
        }
    }

    // Verificación y mensaje en caso de que el servicio de accesibilidad no esté habilitado
    override fun onServiceConnected() {
        super.onServiceConnected()
        createFloatingBubble()
        showFloatingBubble()
    }

    private fun createFloatingBubble() {
        Log.d("WhatsAppService", "Intentando inflar la burbuja flotante")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        floatingBubble = inflater.inflate(R.layout.floating_bubble_layout, null)

        Log.d("WhatsAppService", "Burbuja inflada correctamente")

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        floatingBubble?.findViewById<ImageButton>(R.id.floating_bubble_button)?.setOnClickListener {
            toggleTranslation()
        }

        implementBubbleDrag(floatingBubble, params)

        windowManager?.addView(floatingBubble, params)
        floatingBubble?.visibility = View.GONE

        Log.d("WhatsAppService", "Burbuja flotante creada y añadida al WindowManager")
    }

    private fun showFloatingBubble() {
        floatingBubble?.visibility = View.VISIBLE
    }

    private fun hideFloatingBubble() {
        floatingBubble?.visibility = View.GONE
    }

    private fun toggleTranslation() {
        isTranslating = !isTranslating
        val bubbleButton = floatingBubble?.findViewById<ImageButton>(R.id.floating_bubble_button)

        if (isTranslating) {
            bubbleButton?.setImageResource(R.drawable.ic_translate_active)
            Toast.makeText(this, "Traducción activada", Toast.LENGTH_SHORT).show()
        } else {
            bubbleButton?.setImageResource(R.drawable.ic_translate)
            Toast.makeText(this, "Traducción desactivada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event?.packageName?.toString()
            if (packageName == "com.whatsapp") {
                if (!isWhatsAppForeground) {
                    isWhatsAppForeground = true
                    showFloatingBubble()
                }
            } else {
                if (isWhatsAppForeground) {
                    isWhatsAppForeground = false
                    hideFloatingBubble()
                }
            }
        }

        // Aquí puedes agregar la lógica para capturar y traducir texto cuando isWhatsAppForeground es true
    }

    override fun onInterrupt() {
        // Implementa la lógica para manejar interrupciones aquí
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingBubble?.let { windowManager?.removeView(it) }
        unregisterReceiver(showBubbleReceiver)
    }

    private fun implementBubbleDrag(view: View?, params: WindowManager.LayoutParams) {
        view?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(v, params)
                    true
                }
                else -> false
            }
        }
    }
}
