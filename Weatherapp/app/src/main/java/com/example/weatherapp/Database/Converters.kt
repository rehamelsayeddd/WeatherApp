package com.example.weatherapp.Database

import androidx.room.TypeConverter
import com.example.weatherapp.ForecastModel.City
import com.example.weatherapp.ForecastModel.Clouds
import com.example.weatherapp.ForecastModel.Coord
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.ForecastModel.Main
import com.example.weatherapp.ForecastModel.Weather
import com.example.weatherapp.ForecastModel.Wind
import com.example.weatherapp.OneCall.Model.AlertOneCall
import com.example.weatherapp.OneCall.Model.CurrentOneCall
import com.example.weatherapp.OneCall.Model.OneCallApi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    // Handle Weather List conversion
    @TypeConverter
    fun fromWeatherList(weatherList: List<Weather>?): String? {
        return gson.toJson(weatherList)
    }

    @TypeConverter
    fun toWeatherList(value: String?): List<Weather>? {
        val listType = object : TypeToken<List<Weather>?>() {}.type
        return gson.fromJson(value, listType)
    }

    // Handle ForecastData List conversion
    @TypeConverter
    fun fromForecastList(forecastList: List<ForeCastData>?): String? {
        return gson.toJson(forecastList)
    }

    @TypeConverter
    fun toForecastList(value: String?): List<ForeCastData>? {
        val listType = object : TypeToken<List<ForeCastData>?>() {}.type
        return gson.fromJson(value, listType)
    }

    // Handle Main (temperature, humidity, pressure) conversion
    @TypeConverter
    fun fromMain(main: Main?): String? {
        return gson.toJson(main)
    }

    @TypeConverter
    fun toMain(value: String?): Main? {
        return gson.fromJson(value, Main::class.java)
    }

    // Handle Clouds (all) conversion
    @TypeConverter
    fun fromClouds(clouds: Clouds?): Int? {
        return clouds?.all
    }

    @TypeConverter
    fun toClouds(value: Int?): Clouds? {
        return value?.let { Clouds(it) }
    }

    // Handle Wind conversion
    @TypeConverter
    fun fromWind(wind: Wind?): String? {
        return gson.toJson(wind)
    }

    @TypeConverter
    fun toWind(value: String?): Wind? {
        return gson.fromJson(value, Wind::class.java)
    }

    // Handle City conversion
    @TypeConverter
    fun fromCity(city: City?): String? {
        return gson.toJson(city)
    }

    @TypeConverter
    fun toCity(value: String?): City? {
        return gson.fromJson(value, City::class.java)
    }

    // Handle Coord conversion (City coordinates)
    @TypeConverter
    fun fromCityCoord(coord: Coord?): String? {
        return gson.toJson(coord)
    }

    @TypeConverter
    fun toCityCoord(value: String?): Coord? {
        return gson.fromJson(value, Coord::class.java)
    }

    // Handle Alert List conversion
    @TypeConverter
    fun fromAlertList(alerts: List<AlertOneCall>?): String? {
        return gson.toJson(alerts)
    }

    @TypeConverter
    fun toAlertList(value: String?): List<AlertOneCall>? {
        val listType = object : TypeToken<List<AlertOneCall>?>() {}.type
        return gson.fromJson(value, listType)
    }

    // Handle OneApiCall List conversion
    @TypeConverter
    fun fromApiCallList(apiCallList: List<OneCallApi>?): String? {
        return gson.toJson(apiCallList)
    }

    @TypeConverter
    fun toApiCallList(value: String?): List<OneCallApi>? {
        val listType = object : TypeToken<List<OneCallApi>?>() {}.type
        return gson.fromJson(value, listType)
    }

    // Handle Current conversion
    @TypeConverter
    fun fromCurrent(current: CurrentOneCall?): String? {
        return gson.toJson(current)
    }

    @TypeConverter
    fun toCurrent(value: String?): CurrentOneCall? {
        return gson.fromJson(value, CurrentOneCall::class.java)
    }

}