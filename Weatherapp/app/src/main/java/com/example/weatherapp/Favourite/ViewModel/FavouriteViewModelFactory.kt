package com.example.weatherapp.Favourite.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.Repository.IWeatherRepository
import com.example.weatherapp.Repository.WeatherRepository

class FavouriteViewModelFactory(private val irepository: IWeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouriteViewModel(irepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
