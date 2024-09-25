package com.example.weatherapp.Alert.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.Repository.WeatherRepository

class AlertViewModelFactory(private val repo: WeatherRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            AlertViewModel(repo) as T
        } else {
            throw IllegalArgumentException("ViewModel Class not found")
        }
    }
}