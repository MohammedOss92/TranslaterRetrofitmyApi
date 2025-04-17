package com.sarrawi.mytranslate.api

import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.model.TranslateResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/translate/")
    fun translateText(
        @Body request: TranslateRequest
    ): Response<TranslateResponse>
}