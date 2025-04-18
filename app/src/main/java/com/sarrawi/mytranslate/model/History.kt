package com.sarrawi.mytranslate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // معرف فريد لكل سجل
    val word: String,  // الكلمة الأصلية
    val meaning: String // المعنى (الترجمة)
)
