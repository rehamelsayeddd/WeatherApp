package com.example.weatherapp.Repository

import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeRepositoryTest : IWeatherRepository {

    private val alertsList = mutableListOf<AlertData>()
    private val favoriteList = mutableListOf<CurrentWeatherResponse>()


    // Implement the fetchFiveDaysInfo method if needed
    override suspend fun fetchFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse> {
        // Implementation not required for alert testing
        return flow { } // Return an empty flow
    }

    override fun getWeatherData(): Flow<CurrentWeatherResponse> {
        // Implementation not required for alert testing
        return flow { }
    }

    override suspend fun getAlerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi> {
        // Implementation not required for alert testing
        return flow { }
    }

    override suspend fun insertFavourite(favorite: CurrentWeatherResponse) {
        favoriteList.add(favorite) // Add favorite to list
    }

    override fun getFavourite(): Flow<List<CurrentWeatherResponse>> {
        return flowOf(favoriteList) // Emit the current list of favorites
    }



    override suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double) {
        favoriteList.removeIf { it.longitude == longitude && it.latitude == latitude }
    }

    override suspend fun insertAlert(alert: AlertData) {
        alertsList.add(alert) // Add alert to the list
    }

    override fun getAlertsData(): Flow<List<AlertData>> {
        return flow { emit(alertsList) } // Emit the current list of alerts
    }

    override suspend fun deleteAlert(alert: AlertData) {
        alertsList.remove(alert) // Remove the alert from the list
    }

    override suspend fun saveLocation(
        latitude: Double,
        longitude: Double,
        cityName: String,
        countryName: String
    ) {
        // Implementation not required for alert testing
    }

    override fun getSavedLocations(): Flow<List<LocationData>> {
        // Implementation not required for alert testing
        return flowOf(emptyList())
    }
}
