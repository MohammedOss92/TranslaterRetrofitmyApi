package com.sarrawi.mytranslate.db.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sarrawi.mytranslate.model.FavModel
import com.sarrawi.mytranslate.model.History

@Dao
interface FavDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add_fav(fav: FavModel)

    @Delete
    suspend fun deletefav(item:FavModel)

    @Query("DELETE FROM fav_table WHERE word = :word AND meaning = :meaning")
    suspend fun deleteByText(word: String, meaning: String)


    @Query("Update fav_table SET is_fav = :state where id =:ID")
    suspend fun update_favs(ID:Int,state:Boolean)

    @Query("select * from fav_table order by id desc ")
    fun getAllFavoriteas(): LiveData<List<FavModel>>

    @Query("SELECT * FROM fav_table WHERE word = :word AND meaning = :meaning LIMIT 1")
    suspend fun isFavorite(word: String, meaning: String): FavModel?

    @Query("""
    SELECT * FROM fav_table
    WHERE word LIKE '%' || :query || '%'
       OR meaning LIKE '%' || :query || '%'
    ORDER BY id DESC
""")
    fun searchFav(query: String): LiveData<List<FavModel>>



}