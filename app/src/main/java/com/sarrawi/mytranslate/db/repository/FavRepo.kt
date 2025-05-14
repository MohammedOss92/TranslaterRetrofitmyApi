package com.sarrawi.mytranslate.db.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.sarrawi.mytranslate.database.AppDatabase
import com.sarrawi.mytranslate.db.Dao.FavDao
import com.sarrawi.mytranslate.model.FavModel

class FavRepo(app: Application) {

    val favoriteImageDao: FavDao
    init {
        val database = AppDatabase.getDatabase(app)

        favoriteImageDao = database.favDao()
    }

    fun getAllFav(): List<FavModel> {
        return favoriteImageDao.getAllFavoritea()

    }

    suspend fun addFavorite(favoriteImage: FavModel) {
        favoriteImageDao.add_fav(favoriteImage)
    }

    suspend fun removeFavorite(favoriteImage: FavModel) {
        favoriteImageDao.deletefav(favoriteImage)
    }
}