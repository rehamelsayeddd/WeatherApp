package com.example.weatherapp.ForecastModel

import com.example.weatherapp.OneCall.Model.Weather

data class ForeCastData(
    val clouds: Clouds,
    val dt: Int,
    val dt_txt: String,
    val main: Main,
    val visibility: Int,
    val weather: MutableList<Weather>,
    val wind: Wind
)

