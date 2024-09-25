package com.example.weatherapp.Network

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.OneCall.Model.OneCallApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {


        @GET("2.5/forecast?")
        suspend fun getForeCast(
            @Query("lat") lat: Double,
            @Query("lon") long: Double,
            @Query("units") units: String,
            @Query("appid") apiKey: String,
            @Query("lang") lang: String

        ): CurrentWeatherResponse //3adeya w asebo 3ady mn gher ma a3mlo flow/stateflow

//        //for alerts data
        @GET("3.0/onecall")
        suspend fun getAlerts(  //to make alaram receiver if anything is happening to weather
            @Query("lat") lat: Double,
            @Query("lon") long: Double,
            @Query("units") units: String,
            @Query("appid") apiKey: String,
            @Query("lang") lang: String

        ): OneCallApi //by one call model



    }



//    // Current Weather
//    @GET("weather")
//    suspend fun getCurrentWeatherByCoordinates(
//        @Query("lat") latitude: Double,
//        @Query("lon") longitude: Double,
//        @Query("appid") apiKey: String
//    ): CurrentWeatherResponse
//
//    // 3-hour interval forecast (5 days)
//    @GET("forecast")
//    suspend fun getHourlyForecastByCoordinates(
//        @Query("lat") lat: Double,
//        @Query("lon") lon: Double,
//        @Query("appid") apiKey: String,
//        @Query("units") units: String = "metric"
//    ): HourlyForecastResponse
//
//    // Daily forecast (7 days)
//    @GET("onecall")
//    suspend fun getDailyForecastByCoordinates(
//        @Query("lat") lat: Double,
//        @Query("lon") lon: Double,
//        @Query("exclude") exclude: String = "minutely,hourly,alerts",
//        @Query("appid") apiKey: String,
//        @Query("units") units: String = "metric"
//    ): DailyForecastResponse
//}
//
