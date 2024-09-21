package com.example.weatherapp.Database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow

interface WeatherDao {

    //insert the weather data by network fetching
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherData: CurrentWeatherResponse)

    //get the homefragment when offline
    @Query("SELECT * FROM weather_data where isFav =0 ORDER BY id DESC LIMIT 1")
    fun getWeatherData(): Flow<CurrentWeatherResponse>

    //inserting favourite
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavourite(favorite: CurrentWeatherResponse)

    //reteriving all favourite weather
    @Query("SELECT * FROM weather_data WHERE isFav = 1")
    fun getFavourite(): Flow<List<CurrentWeatherResponse>>

    //delete specific favourite detaiilss
    @Query("DELETE FROM weather_data WHERE isFav = 1 AND longitude = :longitude AND latitude = :latitude")
    suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertALertData(weatherData: OneCallApi)

    //inserting data of alert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlert(alert: AlertData)

    //reterives the saved alerts
    @Query("SELECT * FROM datay")
    fun getAlertsData(): Flow<List<AlertData>>

    //deleting specific alert
    @Delete
    suspend fun deleteAlertDataAll(alert: AlertData)


}