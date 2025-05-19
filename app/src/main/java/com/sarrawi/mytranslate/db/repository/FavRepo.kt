package com.sarrawi.mytranslate.db.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.db.Dao.FavDao
import com.sarrawi.mytranslate.model.FavModel
import com.sarrawi.mytranslate.model.History

class FavRepo(app: Application) {

    val favoriteImageDao: FavDao
    init {
        val database = AppDatabase.getDatabase(app)

        favoriteImageDao = database.favDao()
    }

    fun getAllFav(): LiveData<List<FavModel>> {
        return favoriteImageDao.getAllFavoriteas()
    }




    suspend fun addFavorite(favoriteImage: FavModel) {
        favoriteImageDao.add_fav(favoriteImage)
    }

    suspend fun removeFavorite(word: String, meaning: String)  {
        favoriteImageDao.deleteByText(word, meaning)
    }

    suspend fun update_fav(id: Int,state:Boolean) {

        favoriteImageDao.update_favs(id,state)
    }

    suspend fun isFavorite(word: String, meaning: String): Boolean {
        return favoriteImageDao.isFavorite(word, meaning) != null
    }

    fun searchFav(query: String): LiveData<List<FavModel>> {
        return favoriteImageDao.searchFav(query)
    }



}