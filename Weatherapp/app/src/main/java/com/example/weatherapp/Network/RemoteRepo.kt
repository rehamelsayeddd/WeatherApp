package com.example.weatherapp.Network

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoteRepo {
    private val apiService = RetrofitInstance.api
     suspend fun getFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse> {
        val apiKey = "3f2c5a9a086fa7d7056043da97b35aae"

        return flowOf(apiService.getForeCast(latitude, longitude, units, apiKey, lang))
    }
}