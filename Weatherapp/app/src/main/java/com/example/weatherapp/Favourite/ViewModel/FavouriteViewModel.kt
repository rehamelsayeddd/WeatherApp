package com.example.weatherapp.Favourite.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.FavouriteState
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.Repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.catch

class FavouriteViewModel(private val repository: WeatherRepository) : ViewModel() {

    // StateFlow to hold the current state of favorites
    private val _favouriteState = MutableStateFlow<FavouriteState>(FavouriteState.Loading)
    val favouriteState = _favouriteState.asStateFlow()

    init {
        // Load favourites from the repository when ViewModel is created
        loadFavourites()
    }

    // Load favorite locations from the repository
    private fun loadFavourites() {
        viewModelScope.launch {
            repository.getFavourite()
                .onStart {
                    _favouriteState.value = FavouriteState.Loading
                }
                .catch { exception ->
                    _favouriteState.value = FavouriteState.Error(exception.message ?: "Unknown error")
                }
                .collect { favouritesList ->
                    if (favouritesList.isNotEmpty()) {
                        _favouriteState.value = FavouriteState.Success(favouritesList)
                    } else {
                        _favouriteState.value = FavouriteState.Error("No favorites found")
                    }
                }
        }
    }

    // Function to add a favorite location
    fun addFavourite(favorite: CurrentWeatherResponse) {
        viewModelScope.launch {
            repository.insertFavourite(favorite)
            loadFavourites() // Refresh the list
        }
    }

    // Function to remove a favorite location
    fun removeFavourite(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            repository.deleteFavouriteByLonLat(longitude, latitude)
            loadFavourites() // Refresh the list
        }
    }
}
