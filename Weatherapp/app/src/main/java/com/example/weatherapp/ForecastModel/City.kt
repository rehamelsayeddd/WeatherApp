package com.example.weatherapp.ForecastModel

data class City(
    val name: String,
    val country: String,
    val coord: Coord,
    val id: Int,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)
