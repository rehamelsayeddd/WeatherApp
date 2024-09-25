package com.example.weatherapp.Home.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.ForecastApiState
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.Network.WeatherApiService
import com.example.weatherapp.Repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    // StateFlow for holding the current UI state
    private val _forecastState = MutableStateFlow<ForecastApiState>(ForecastApiState.Loading)
    val forecastState: StateFlow<ForecastApiState> get() = _forecastState

    // Fetch weather from network or fallback to Room database
    fun fetchWeather(latitude: Double, longitude: Double, apiKey: String, lang: String, units: String) {
        viewModelScope.launch {
            try {
                _forecastState.value = ForecastApiState.Loading // Correct reference

                // Fetch data from network
                repository.fetchFiveDaysInfo(latitude, longitude, units, apiKey, lang).collect { weatherData ->
                    _forecastState.value = ForecastApiState.Success(weatherData) // Update to success

                    // Save the data in Room for offline access
                    repository.insertFavourite(weatherData)
                }
            } catch (e: Exception) {
                // In case of an error, fetch data from Room (offline mode)
                repository.getWeatherData().collect { offlineWeatherData ->
                    if (offlineWeatherData != null) {
                        _forecastState.value = ForecastApiState.Success(offlineWeatherData)
                    } else {
                        _forecastState.value = ForecastApiState.Error("Failed to load data")
                    }
                }
            }
        }
    }

    // Retrieve weather data for offline access (Room)
    fun getWeatherData(): Flow<CurrentWeatherResponse> {
        return repository.getWeatherData()
    }
}
