package com.sarrawi.mytranslate.repo

import com.sarrawi.mytranslate.api.ApiService
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.model.TranslateResponse

class TranslationRepository(private val apiService: ApiService) {

    fun translateTextcallback(request: TranslateRequest, callback: (TranslateResponse?) -> Unit) {
        try {
            val response = apiService.translateText(request)
            if (response.isSuccessful) {
                callback(response.body())
            } else {
                callback(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
        }
    }

    fun translateTextResponse(request: TranslateRequest): TranslateResponse? {
        return try {
            val response = apiService.translateText(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}