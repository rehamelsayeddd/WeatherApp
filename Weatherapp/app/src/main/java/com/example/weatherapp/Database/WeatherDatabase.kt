package com.example.weatherapp.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.ForecastModel.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi


@Database(entities = [CurrentWeatherResponse::class,OneCallApi::class, AlertData::class , LocationData::class] ,exportSchema = false, version = 1)

@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun getWeatherDAO(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null
        @Synchronized
        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, WeatherDatabase::class.java,
                    "utifei"
                ).build()
                INSTANCE = instance
                instance}
        }
    }
}