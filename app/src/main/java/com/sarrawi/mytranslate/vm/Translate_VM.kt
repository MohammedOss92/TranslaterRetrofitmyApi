package com.sarrawi.mytranslate.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.mytranslate.model.History
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.model.TranslateResponse
import com.sarrawi.mytranslate.repo.TranslationRepository
import kotlinx.coroutines.launch

class Translate_VM(private val translationRepository: TranslationRepository): ViewModel() {

    fun translate_vm2(request: TranslateRequest, onResult: (TranslateResponse?) -> Unit) {
        translationRepository.translateTextResponse(request, onResult)
    }

    fun deleteHistory(history: History) = viewModelScope.launch {
        translationRepository.delete(history)
    }
    fun updateIsFav(word: String, meaning: String, isFav: Boolean) = viewModelScope.launch {
        translationRepository.updateIsFav(word, meaning, isFav)
    }



    val allHistory: LiveData<List<History>> = translationRepository.getAllHistory()

    fun translate_vm22(request: TranslateRequest, onResult: (TranslateResponse?) -> Unit) {
        Log.d("Translate", "Starting translation for: $request")

        viewModelScope.launch {
            val result = translationRepository.translateTextResponse(request)
            if (result != null) {
                Log.d("Translate", "Translation successful: ${result.translated_text}")
            } else {
                Log.e("Translate", "Translation failed")
            }

            onResult(result)
        }
    }

    fun translate_vm(request: TranslateRequest) {
        Log.d("Translate", "Starting translation for: $request")
        translationRepository.translateTextResponse(request) { result ->
            if (result != null) {
                Log.d("Translate", "Translation successful: ${result.translated_text}")
                // هنا يمكنك استخدام `result.translated_text` لعرض الترجمة
            } else {
                Log.e("Translate", "Translation failed")
            }
        }
    }



//    fun trasnslate_vm(request: TranslateRequest) {
//        viewModelScope.launch {
//            val response = translationRepository.translateTextResponse(request)
//            if (response != null) {
//                // ✅ استخدم الترجمة هنا
//                Log.d("Translation", response.translated_text)
//            } else {
//                // ❌ فشل الترجمة
//                Log.e("Translation", "Translation failed")
//            }
//        }
//    }
//
//    fun transslate_vm(request: TranslateRequest): TranslateResponse? {
//        return translationRepository.translateTextResponse(request)
//    }
//
//    fun translate_vm(request: TranslateRequest): TranslateResponse? {
//        Log.d("Translate", "Starting translation for: $request")  // إضافة Log قبل بدء الترجمة
//
//        val result = translationRepository.translateTextResponse(request)
//
//        if (result != null) {
//            Log.d("Translate", "Translation successful: ${result.translated_text}")
//        } else {
//            Log.e("Translate", "Translation failed")
//        }
//
//        return result
//    }
//
//
//
//
//    fun translate_vm_callback(request: TranslateRequest) {
//        translationRepository.translateTextcallback(request) { response ->
//            if (response != null) {
//                Log.d("Translation", response.translated_text)
//            } else {
//                Log.e("Translation", "Translation failed")
//            }
//        }
//    }



}