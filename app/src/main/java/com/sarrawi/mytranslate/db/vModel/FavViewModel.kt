package com.sarrawi.mytranslate.db.vModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.mytranslate.db.repository.FavRepo
import com.sarrawi.mytranslate.model.FavModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavViewModel(private val repository: FavRepo) : ViewModel() {

    private val _favoriteImages = MutableLiveData<List<FavModel>>()
    val favoriteImages: LiveData<List<FavModel>> = _favoriteImages

    private val _favorite = MutableLiveData<List<FavModel>>()
    val favorite: LiveData<List<FavModel>> = _favorite

    private var __response = MutableLiveData<List<FavModel>>()
    val responseMsgsFav: MutableLiveData<List<FavModel>>
        get() = __response

    init {
        // Initialize _favoriteImages with data from the repository
        viewModelScope.launch(Dispatchers.IO) {
            val images = repository.getAllFav()
            _favoriteImages.postValue(images)
        }
    }

    fun addFavorite(favoriteImage: FavModel) {
        println("Adding favorite image: $favoriteImage")
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavorite(favoriteImage)
            // Update _favoriteImages after adding a new image
            val images = repository.getAllFav()
            _favoriteImages.postValue(images)
            println("Favorite image added successfully.")
        }
    }

    fun removeFavorite(favorite: FavModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFavorite(favorite)
            // Update _favoriteImages after removing an image
            val images = repository.getAllFav()
            _favoriteImages.postValue(images)
        }
    }

    fun updateImages() {
        viewModelScope.launch {
            val images = repository.getAllFav()
            println("Favorite images from the database: $images")
            _favoriteImages.postValue(images)
        }
    }

    fun getAllFavoriteImages() {
        viewModelScope.launch {
            val images = repository.getAllFav()
            println("Favorite images from the database: $images")
            _favoriteImages.postValue(images)
        }
    }
}