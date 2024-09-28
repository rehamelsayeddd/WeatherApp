package com.example.weatherapp.Home.ViewModel

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse

// Sealed class for representing the HOME_FRAGMENT
sealed class ForecastApiState {
    object Loading : ForecastApiState()
    data class Success(val data: CurrentWeatherResponse) : ForecastApiState()
    data class Error(val message: String) : ForecastApiState()
}
