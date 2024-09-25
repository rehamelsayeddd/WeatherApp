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

    fun getLanguage(key: String, defaultValue: String): String { return sharedPreferences.getString(key, defaultValue) ?: defaultValue }

    fun getSavedMap(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }


    //save current location
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











}


enum class SharedKey {
    LANGUAGE,
    GPS, //location choice ==> gps or map
    MAP, //type of the map ==> home or fav or alert.
    Home, // save lan and long to home
    FAV, //save lan and long to fave
    ALERT,
    UNITS,
    CURMAP,
    TEMP_UNIT,
    ALERT_TYPE,



}

