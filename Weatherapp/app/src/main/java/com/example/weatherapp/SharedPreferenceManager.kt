package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.InternalCoroutinesApi

class SharedPreferenceManager private constructor(context: Context){
    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "MyPreferences"
        private var instance: SharedPreferenceManager? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): SharedPreferenceManager {
            return instance ?: kotlinx.coroutines.internal.synchronized(this) {
                instance ?: SharedPreferenceManager(context).also { instance = it }
            }
        }
    }

    //language settings
    fun getLanguage(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    fun saveLanguage(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getSavedMap(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }


    //save current location to pass it to alert
    fun saveLocationToAlert(key: String, longt: Double, lat:Double) {
        val editor = sharedPreferences.edit()
        editor.putString(key + "_longt", longt.toString())
        editor.putString(key + "_lat", lat.toString())
        editor.apply()
    }

    fun getLocationToAlert(key: String): Pair<Double, Double>? {
        val longtKey = key + "_longt"
        val latKey = key + "_lat"
        val longt = sharedPreferences.getString(longtKey, null)?.toDoubleOrNull()
        val lat = sharedPreferences.getString(latKey, null)?.toDoubleOrNull()

        if (longt != null && lat != null) {
            return Pair(longt, lat)
        }

        return null
    }

    //wind settings
    fun saveWindUnit(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getWindUnit(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }


    //temperature settings
    fun saveTempUnit(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun getTempUnit(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    //location choice

    fun savelocationChoice(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun getlocationChoice(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    //saving and getting the chosen location lon and lat

    fun saveLocationCoordinates(latitude: Double, longitude: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat("latitude", latitude.toFloat())
        editor.putFloat("longitude", longitude.toFloat())
        editor.apply()
    }

    fun getLocationCoordinates(): Pair<Double, Double> {
        val latitude = sharedPreferences.getFloat("latitude", 0.0f).toDouble()
        val longitude = sharedPreferences.getFloat("longitude", 0.0f).toDouble()
        return Pair(latitude, longitude)
    }







}


enum class SharedKey {
    LANGUAGE,
    GPS, //location choice ==> gps or map
    MAP, //type of the map
    Home, // save lan and long to home
    FAV,
    ALERT,
    UNITS,
    CURMAP,
    TEMP_UNIT,
    ALERT_TYPE,

}

