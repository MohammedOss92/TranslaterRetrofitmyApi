package com.sarrawi.mytranslate.repo

import android.util.Log
import com.sarrawi.mytranslate.api.ApiService
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.model.TranslateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslationRepository(private val apiService: ApiService) {

    fun translateTextResponse(request: TranslateRequest, callback: (TranslateResponse?) -> Unit) {
        apiService.translateText(request).enqueue(object : Callback<TranslateResponse> {
            override fun onResponse(call: Call<TranslateResponse>, response: Response<TranslateResponse>) {
                if (response.isSuccessful) {
                    callback(response.body())  // إعادة النتيجة في حالة النجاح
                } else {
                    callback(null)  // في حالة الفشل
                }
            }

            override fun onFailure(call: Call<TranslateResponse>, t: Throwable) {
                Log.e("Translate", "Error: ${t.message}")
                callback(null)  // في حالة حدوث استثناء
            }
        })
    }

    suspend fun translateTextResponse(request: TranslateRequest): TranslateResponse? {
        return try {
            val response = apiService.translateText2(request)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Translate", "Exception: ${e.message}")
            null
        }
    }


//    fun translateTextReasponse(request: TranslateRequest): TranslateResponse? {
//        return try {
//            val response = apiService.translateText(request)
//            if (response.isSuccessful) {
//                response.body()
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//    fun translateTextResponse(request: TranslateRequest): TranslateResponse? {
//        return try {
//            Log.d("Translate", "Sending request: $request")  // إضافة Log لتتبع الطلب
//
//            val response = apiService.translateText(request)
//
//            if (response.isSuccessful) {
//                Log.d("Translate", "Response successful: ${response.body()}")
//                response.body()  // إرجاع الاستجابة في حالة النجاح
//            } else {
//                Log.e("Translate", "Response failed: ${response.code()} - ${response.message()}")
//                null  // في حالة الفشل
//            }
//        } catch (e: Exception) {
//            Log.e("Translate", "Error: ${e.message}")
//            e.printStackTrace()  // طباعة الاستثناء
//            null  // في حالة وجود استثناء
//        }
//    }

}