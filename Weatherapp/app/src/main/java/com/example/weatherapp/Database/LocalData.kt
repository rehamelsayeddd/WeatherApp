package com.example.weatherapp.Database

import android.content.Context
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow

class LocalData(context: Context) {

    private val weatherDao : WeatherDao by lazy {
        val db : WeatherDatabase = WeatherDatabase.getInstance(context)
        db.getWeatherDAO()
    }

    // Insert weather data fetched from network into the database (HomeFragment)
    suspend fun insertWeatherData(weatherData: CurrentWeatherResponse) {
        weatherDao.insertWeatherData(weatherData)
    }

    // Retrieve weather data for HomeFragment when offline
    fun getWeatherData(): Flow<CurrentWeatherResponse> {
        return weatherDao.getWeatherData()
    }

    // Insert a location into the favorites list (FavouriteFragment)
    suspend fun insertFavourite(favorite: CurrentWeatherResponse) {
        weatherDao.insertFavourite(favorite)
    }

    // Retrieve all favorite locations
    fun getFavourite(): Flow<List<CurrentWeatherResponse>> {
        return weatherDao.getFavourite()
    }

    // Delete a specific favorite by its longitude and latitude
    suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double) {
        weatherDao.deleteFavouriteByLonLat(longitude, latitude)
    }

    // Insert weather alert data into the database
    suspend fun insertAlertData(weatherData: OneCallApi) {
        weatherDao.insertALertData(weatherData)
    }

    // Insert a specific alert
    suspend fun insertAlert(alert: AlertData) {
        weatherDao.insertAlert(alert)
    }

    // Retrieve all saved alerts
    fun getAlertsData(): Flow<List<AlertData>> {
        return weatherDao.getAlertsData()
    }

    // Delete a specific alert
    suspend fun deleteAlertDataAll(alert: AlertData) {
        weatherDao.deleteAlertDataAll(alert)
    }


    // ---- Location-related methods ----

    // Save a location (latitude, longitude, cityName, countryName)
    suspend fun saveLocation(location: LocationData) {
        weatherDao.saveLocation(location)
    }

    // Retrieve saved locations
    fun getSavedLocations(): Flow<List<LocationData>> {
        return weatherDao.getSavedLocations()
    }

    // Delete a specific location by its latitude and longitude
    suspend fun deleteLocationByCoordinates(latitude: Double, longitude: Double) {
        weatherDao.deleteLocationByCoordinates(latitude, longitude)
    }
}
