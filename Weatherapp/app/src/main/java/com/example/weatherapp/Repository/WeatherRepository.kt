package com.example.weatherapp.Repository

import android.util.Log
import com.example.weatherapp.Database.InterfaceLocalData
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.Network.InterfaceRemoteData
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow


class WeatherRepository(
    private val remoteData: InterfaceRemoteData,
    private val localData: InterfaceLocalData
) : IWeatherRepository {

    companion object {
        private var instance: WeatherRepository? = null

        fun getInstance(remoteData: RemoteData, localData: LocalData): WeatherRepository {
            return instance ?: synchronized(this) {
                val temp = WeatherRepository(remoteData, localData)
                instance = temp
                temp
            }
        }
    }

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
    override suspend fun fetchFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse> {
        return try {
            remoteData.getFiveDaysInfo(latitude, longitude, units, apiKey, lang)
        } catch (e: Exception) {
            Log.i("============ERRORRRRRR==========", "FETCH_FIVE_DAYS_ERROR error: " + e)
            emptyFlow()
        }

    }

    // Retrieve weather data for HomeFragment when offline
    override fun getWeatherData(): Flow<CurrentWeatherResponse> {
        return localData.getWeatherData()
    }

    // Fetch weather alerts from the network and save them to the local database
    override suspend fun getAlerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi> {
        return try {
            remoteData.getALerts(latitude, longitude, units, apiKey, lang)
        } catch (e: Exception) {
            Log.i("============ERRORRRRRR==========", "GET_ALERTS_ERROR error: " + e)

            emptyFlow()
        }

    }

    // Insert a favorite location into the database
    override suspend fun insertFavourite(favorite: CurrentWeatherResponse) {
        localData.insertFavourite(favorite)
    }

    // Retrieve all favorite locations
    override fun getFavourite(): Flow<List<CurrentWeatherResponse>> {
        return localData.getFavourite()
    }

    // Delete a specific favorite by its longitude and latitude
    override suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double) {
        localData.deleteFavouriteByLonLat(longitude, latitude)
    }

    // Insert an alert into the database
    override suspend fun insertAlert(alert: AlertData) {
        localData.insertAlert(alert)
    }

    // Retrieve all saved alerts
    override fun getAlertsData(): Flow<List<AlertData>> {
        return localData.getAlertsData()
    }

    // Delete a specific alert
    override suspend fun deleteAlert(alert: AlertData) {
        localData.deleteAlertDataAll(alert)
    }

    //Save a location
    override suspend fun saveLocation(
        latitude: Double,          // Correct type: Double for latitude
        longitude: Double,
        cityName: String,
        countryName: String
    ) {
        val locationData = LocationData(
            latitude = latitude,
            longitude = longitude,
            cityName = cityName,
            countryName = countryName
        )
        localData.saveLocation(locationData)
    }



    // Retrieve saved locations
    override fun getSavedLocations(): Flow<List<LocationData>> {
        return localData.getSavedLocations()
    }
}
