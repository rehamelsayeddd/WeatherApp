package com.example.weatherapp.Favourite.ViewModel

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse

// Sealed class for representing the FAVOURITE_FRAGMENT

sealed class FavouriteState {
    object Loading : FavouriteState()
    data class Success(val favourites: List<CurrentWeatherResponse>) : FavouriteState() // Updated to List
    data class Error(val message: String) : FavouriteState()
}
