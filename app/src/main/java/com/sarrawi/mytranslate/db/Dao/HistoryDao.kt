package com.sarrawi.mytranslate.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sarrawi.mytranslate.model.History

@Dao
interface HistoryDao {

    @Insert
    fun insertHistory(history: History)

    @Query("SELECT * FROM history ORDER BY id DESC")
    suspend fun getAllHistory(): List<History>
}
