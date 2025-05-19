package com.sarrawi.mytranslate.db.vModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.mytranslate.db.repository.FavRepo
import com.sarrawi.mytranslate.model.FavModel
import com.sarrawi.mytranslate.model.History
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



    fun addFavorite(favoriteImage: FavModel) {
        println("Adding favorite image: $favoriteImage")
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavorite(favoriteImage)

        }
    }



    fun removeFavorite(word: String, meaning: String) = viewModelScope.launch {
        repository.removeFavorite(word, meaning)
    }

    fun update_fav(id: Int,state:Boolean) = viewModelScope.launch {
        repository.update_fav(id,state)
    }




    fun getFav(): LiveData<List<FavModel>> {
        Log.e("tessst","entred22")
//        viewModelScope.launch {
//          __response.postValue(msgsRepo.getAllFav())
//        }
        return repository.getAllFav()
    }

    fun isFavorite(word: String, meaning: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.isFavorite(word, meaning)
            callback(result)
        }
    }

    fun search(query: String): LiveData<List<FavModel>> {
        return repository.searchFav(query)
    }

}