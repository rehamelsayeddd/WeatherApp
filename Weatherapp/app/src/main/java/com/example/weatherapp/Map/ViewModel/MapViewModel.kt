package com.example.weatherapp.Map.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.Map.View.MapActivity.Companion.API_KEY
import com.example.weatherapp.Network.RetrofitInstance
import com.example.weatherapp.Repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapViewModel(private val repository: WeatherRepository) : ViewModel() {

    // Method to fetch weather data from API
    fun fetchWeatherData(latitude: Double, longitude: Double): LiveData<CurrentWeatherResponse> {
        val result = MutableLiveData<CurrentWeatherResponse>()
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getForeCast(
                    lat = latitude,
                    long = longitude,
                    units = "metric",
                    apiKey = API_KEY,
                    lang = "en"
                )
                result.postValue(response)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching weather data: ${e.message}")
            }
        }
        return result
    }

    // Method to save location to database
    fun saveLocation(latitude: Double, longitude: Double, cityName: String, countryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveLocation(latitude, longitude, cityName, countryName)
            Log.d("MapViewModel", "Location saved: $cityName, $countryName")

        }
    }

    // Method to retrieve saved locations for flow to collect saved location
    fun getSavedLocations(): LiveData<List<LocationData>> {
        val savedLocations = MutableLiveData<List<LocationData>>()
        viewModelScope.launch {
            repository.getSavedLocations().collect { locations ->
                savedLocations.postValue(locations)
            }
        }
        return savedLocations
    }
}
