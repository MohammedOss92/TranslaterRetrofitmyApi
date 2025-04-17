package com.sarrawi.mytranslate.model

import com.google.gson.annotations.SerializedName

data class TranslateResponse(
    @SerializedName("translated_text")
    val translated_text: String
)


