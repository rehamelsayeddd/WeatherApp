import com.example.weatherapp.Database.InterfaceLocalData
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.LocationData
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalData : InterfaceLocalData {

    var savedWeatherResponse: CurrentWeatherResponse? = null
    var favoritesList: List<CurrentWeatherResponse> = emptyList()

    override fun getWeatherData(): Flow<CurrentWeatherResponse> {
        return flowOf(savedWeatherResponse!!)
    }

    override suspend fun insertWeatherData(weatherData: CurrentWeatherResponse) {
        savedWeatherResponse = weatherData
    }

    override suspend fun insertFavourite(favorite: CurrentWeatherResponse) {
        favoritesList = favoritesList + favorite
    }

    override fun getFavourite(): Flow<List<CurrentWeatherResponse>> {
        return flowOf(favoritesList)
    }

    override suspend fun deleteFavouriteByLonLat(longitude: Double, latitude: Double) {
        favoritesList = favoritesList.filterNot { it.longitude == longitude && it.latitude == latitude }
    }

    // Implement other methods as needed
    override suspend fun insertAlertData(weatherData: OneCallApi) { /* Not implemented for fake */ }

    override suspend fun insertAlert(alert: AlertData) { /* Not implemented for fake */ }

    override fun getAlertsData(): Flow<List<AlertData>> = flow { emit(emptyList()) } // Example implementation

    override suspend fun deleteAlertDataAll(alert: AlertData) { /* Not implemented for fake */ }

    override suspend fun saveLocation(location: LocationData) { /* Not implemented for fake */ }

    override fun getSavedLocations(): Flow<List<LocationData>> = flow { emit(emptyList()) } // Example implementation

    override suspend fun deleteLocationByCoordinates(latitude: Double, longitude: Double) { /* Not implemented for fake */ }
}
