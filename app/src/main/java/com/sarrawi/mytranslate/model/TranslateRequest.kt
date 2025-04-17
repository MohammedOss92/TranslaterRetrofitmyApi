package com.sarrawi.mytranslate.model

data class TranslateRequest(
    val source_language: String,
    val target_language: String,
    val source_text: String
)

