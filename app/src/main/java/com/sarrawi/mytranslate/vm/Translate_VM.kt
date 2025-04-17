package com.sarrawi.mytranslate.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.mytranslate.model.TranslateRequest
import com.sarrawi.mytranslate.repo.TranslationRepository
import kotlinx.coroutines.launch

class Translate_VM(private val translationRepository: TranslationRepository): ViewModel() {

    fun translate_vm(request: TranslateRequest) {
        viewModelScope.launch {
            val response = translationRepository.translateTextResponse(request)
            if (response != null) {
                // ✅ استخدم الترجمة هنا
                Log.d("Translation", response.translated_text)
            } else {
                // ❌ فشل الترجمة
                Log.e("Translation", "Translation failed")
            }
        }
    }


    fun translate_vm_callback(request: TranslateRequest) {
        translationRepository.translateTextcallback(request) { response ->
            if (response != null) {
                Log.d("Translation", response.translated_text)
            } else {
                Log.e("Translation", "Translation failed")
            }
        }
    }

}