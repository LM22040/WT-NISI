package com.example.whatsapptranslate.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import android.util.Log

interface HuggingFaceApi {
    @POST("models/Helsinki-NLP/opus-mt-en-es")
    fun translate(
        @Header("Authorization") token: String,
        @Body input: Map<String, List<String>>
    ): Call<List<Map<String, String>>>
}

// Implementación del cliente de traducción con manejo de errores
fun translateTextWithApi(api: HuggingFaceApi, token: String, text: String, onTranslationSuccess: (String) -> Unit, onTranslationFailure: (String) -> Unit) {
    val input = mapOf("inputs" to listOf(text))

    api.translate(token, input).enqueue(object : Callback<List<Map<String, String>>> {
        override fun onResponse(call: Call<List<Map<String, String>>>, response: Response<List<Map<String, String>>>) {
            if (response.isSuccessful && response.body() != null) {
                val translations = response.body()
                val translatedText = translations?.get(0)?.get("translation_text") ?: "Error en la traducción"
                onTranslationSuccess(translatedText)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("HuggingFaceApi", "Error en la respuesta: $errorMsg")
                onTranslationFailure("Error en la traducción: $errorMsg")
            }
        }

        override fun onFailure(call: Call<List<Map<String, String>>>, t: Throwable) {
            Log.e("HuggingFaceApi", "Error al conectar con la API: ${t.message}")
            onTranslationFailure("Error al conectar con la API: ${t.message}")
        }
    })
}
