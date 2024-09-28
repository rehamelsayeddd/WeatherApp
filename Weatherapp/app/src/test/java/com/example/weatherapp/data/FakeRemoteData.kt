import com.example.weatherapp.Network.InterfaceRemoteData
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.OneCall.Model.OneCallApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRemoteData : InterfaceRemoteData {

    var weatherResponse: CurrentWeatherResponse? = null
    var alertsResponse: OneCallApi? = null

    override suspend fun getFiveDaysInfo(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<CurrentWeatherResponse> {
        return flow {
            emit(weatherResponse ?: throw Exception("No weather data set"))
        }
    }

    override suspend fun getALerts(
        latitude: Double,
        longitude: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Flow<OneCallApi> {
        return flow {
            emit(alertsResponse ?: throw Exception("No alerts data set"))
        }
    }

    // Implement other methods as needed
}
