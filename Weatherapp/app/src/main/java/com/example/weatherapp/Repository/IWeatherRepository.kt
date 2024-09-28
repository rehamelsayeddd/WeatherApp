package com.example.weatherapp.Repository

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow

interface IWeatherRepository {
    // Fetch five days forecast from the network and save it to local database
//    suspend fun fetchFiveDaysInfo(
//        latitude: Double,
//        longitude: Double,
//        units: String,
//        apiKey: String,
//        lang: String
//    ): Flow<CurrentWeatherResponse> {
//        val response = remoteData.getFiveDaysInfo(latitude, longitude, units, apiKey, lang)
//
//        // Collect the response and insert it into the local database
//        response.collect { weatherData ->
//            localData.insertWeatherData(weatherData)
//        }
//
//        return response
//    }
    suspend fun fetchFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse>

    // Retrieve weather data for HomeFragment when offline
    fun getWeatherData(): Flow<CurrentWeatherResponse>

    // Fetch weather alerts from the network and save them to the local database
    suspend fun getAlerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi>

    // Insert a favorite location into the database
    suspend fun insertFavourite(favorite: CurrentWeatherResponse)

    // Retrieve all favorite locations
    fun getFavourite(): Flow<List<CurrentWeatherResponse>>

    // Delete a specific favorite by its longitude and latitude
    suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double)

    // Insert an alert into the database
    suspend fun insertAlert(alert: AlertData)

    // Retrieve all saved alerts
    fun getAlertsData(): Flow<List<AlertData>>

    // Delete a specific alert
    suspend fun deleteAlert(alert: AlertData)

    //Save a location
    suspend fun saveLocation(
        latitude: Double,          // Correct type: Double for latitude
        longitude: Double,
        cityName: String,
        countryName: String
    )

    // Retrieve saved locations
    fun getSavedLocations(): Flow<List<LocationData>>
}