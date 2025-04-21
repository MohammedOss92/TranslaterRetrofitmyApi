package com.sarrawi.mytranslate.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sarrawi.mytranslate.model.History

@Dao
interface HistoryDao {

    @Insert
    fun insertHistory(history: History)

    @Query("SELECT * FROM history ORDER BY id DESC")
    suspend fun getAllHistory2(): List<History>

    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<History>>

    @Delete
    suspend fun delete(history: History)

}
