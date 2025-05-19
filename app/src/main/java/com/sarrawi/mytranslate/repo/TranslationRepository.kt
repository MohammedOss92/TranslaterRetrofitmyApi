package com.sarrawi.mytranslate.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.sarrawi.mytranslate.api.ApiService
import com.sarrawi.mytranslate.dao.HistoryDao
import com.sarrawi.mytranslate.model.History
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.model.TranslateResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslationRepository(private val apiService: ApiService, private val historyDao: HistoryDao) {

    fun translateTextResponse(request: TranslateRequest, callback: (TranslateResponse?) -> Unit) {
        apiService.translateText(request).enqueue(object : Callback<TranslateResponse> {
            override fun onResponse(call: Call<TranslateResponse>, response: Response<TranslateResponse>) {
                if (response.isSuccessful) {
                    val translatedText = response.body()?.translated_text
                    callback(response.body())  // إعادة النتيجة في حالة النجاح

                    translatedText?.let {
                        val history = History(word = request.source_text, meaning = it,
                            sourceLang = request.source_language,
                            targetLang = request.target_language)
                        // نحفظها في الخلفية
                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.insertHistory(history)
                        }
                    }

//                    translatedText?.let {
//                        val history = History(word = request.source_text, meaning = it)
//                        historyDao.insertHistory(history)
//                    }
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


    fun getAllHistory(): LiveData<List<History>> = historyDao.getAllHistory()
    fun searchHistory(query: String): LiveData<List<History>> {
        return historyDao.searchHistory(query)
    }

    suspend fun delete(history: History) {
        historyDao.delete(history)
    }

    suspend fun updateIsFav(word: String, meaning: String, isFav: Boolean) {
        historyDao.updateIsFav(word, meaning, isFav)
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