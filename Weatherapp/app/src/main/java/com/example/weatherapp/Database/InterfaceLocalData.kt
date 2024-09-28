package com.example.weatherapp.Database

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow

interface InterfaceLocalData {
    // Insert weather data fetched from network into the database (HomeFragment)
    suspend fun insertWeatherData(weatherData: CurrentWeatherResponse)

    // Retrieve weather data for HomeFragment when offline
    fun getWeatherData(): Flow<CurrentWeatherResponse>

    // Insert a location into the favorites list (FavouriteFragment)
    suspend fun insertFavourite(favorite: CurrentWeatherResponse)

    // Retrieve all favorite locations
    fun getFavourite(): Flow<List<CurrentWeatherResponse>>

    // Delete a specific favorite by its longitude and latitude
    suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double)

    // Insert weather alert data into the database
    suspend fun insertAlertData(weatherData: OneCallApi)

    // Insert a specific alert
    suspend fun insertAlert(alert: AlertData)

    // Retrieve all saved alerts
    fun getAlertsData(): Flow<List<AlertData>>

    // Delete a specific alert
    suspend fun deleteAlertDataAll(alert: AlertData)

    // Save a location (latitude, longitude, cityName, countryName)
    suspend fun saveLocation(location: LocationData)

    // Retrieve saved locations
    fun getSavedLocations(): Flow<List<LocationData>>

    // Delete a specific location by its latitude and longitude
    suspend fun deleteLocationByCoordinates(latitude: Double, longitude: Double)
}