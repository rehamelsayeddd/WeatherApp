package com.example.weatherapp.ForecastModel

data class Precipitation (
    val probability: String,
    val unit: String? = null,
    val value: String? = null,
    val type: String? = null
)

