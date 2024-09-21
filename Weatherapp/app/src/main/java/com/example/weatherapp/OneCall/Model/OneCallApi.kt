package com.example.weatherapp.OneCall.Model

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "alert_data", primaryKeys = ["lat","lon", "isALert"])

data class OneCallApi(

    @ColumnInfo(name = "alerts") val alerts: List<AlertOneCall>? = null,
    @ColumnInfo(name = "current")  val current: CurrentOneCall,
    @ColumnInfo(name = "isALert")  var isALert: Int = 0,
    @ColumnInfo(name = "lat")  var lat: Double,
    @ColumnInfo(name = "lon") var lon: Double,
    @ColumnInfo(name = "timezone") val timezone: String,
    @ColumnInfo(name = "timezone_offset") val timezone_offset: Int
)

