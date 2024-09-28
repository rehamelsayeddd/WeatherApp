package com.example.weatherapp.Network

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow

interface InterfaceRemoteData {
    suspend fun getFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse>

    suspend fun getALerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi>
}