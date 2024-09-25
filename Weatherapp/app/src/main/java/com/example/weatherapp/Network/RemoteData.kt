package com.example.weatherapp.Network

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoteData {
    private val apiService = RetrofitInstance.api
     suspend fun getFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse> {
        val apiKey = "3f2c5a9a086fa7d7056043da97b35aae"

        return flowOf(apiService.getForeCast(latitude, longitude, units, apiKey, lang)) //return flow
    }


//     suspend fun getWeather(
//        lat: String?,
//        lon: String?,
//        lang: String?,
//        units: String?
//    ): CurrentWeatherResponse {
//        val root = apiService.getForeCast(lat,lon,lang,units)
//        return root.body()!!
//
//    }

     suspend fun getALerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi> {
        return flowOf( apiService.getAlerts(latitude, longitude , units, apiKey, lang))
    }
}