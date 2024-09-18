package com.example.weatherapp

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val cityName: String
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class Wind(
    @SerializedName("speed") val speed: Double
)

data class Clouds(
    @SerializedName("all") val cloudiness: Int
)

data class Weather(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)
