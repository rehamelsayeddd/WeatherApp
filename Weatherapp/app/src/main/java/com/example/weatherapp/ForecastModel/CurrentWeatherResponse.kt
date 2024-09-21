package com.example.weatherapp.ForecastModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "weather_data")
data class CurrentWeatherResponse(


    @PrimaryKey(autoGenerate = true)
@ColumnInfo(name = "id")  val id: Int,
    @ColumnInfo(name = "city")  val city: City,
    @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0,
    @ColumnInfo(name = "cnt")  val cnt: Int,
    @ColumnInfo(name = "isFav")  var isFav: Int = 0,
    @ColumnInfo(name = "isALert")  var isALert: Int = 0,
    @ColumnInfo(name = "cod")  val cod: String,
    @ColumnInfo(name = "forecastlist")  val list: MutableList<ForeCastData>,
    @ColumnInfo(name = "message") val message: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long? = null

)
