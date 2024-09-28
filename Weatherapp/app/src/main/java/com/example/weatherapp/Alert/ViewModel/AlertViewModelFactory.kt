package com.example.weatherapp.Alert.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.Repository.IWeatherRepository
import com.example.weatherapp.Repository.WeatherRepository

class AlertViewModelFactory(private val irepo: IWeatherRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            AlertViewModel(irepo) as T
        } else {
            throw IllegalArgumentException("ViewModel Class not found")
        }
    }
}