import com.example.weatherapp.ForecastModel.City
import com.example.weatherapp.ForecastModel.Coord
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.Repository.WeatherRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var fakeLocalData: FakeLocalData
    private lateinit var fakeRemoteData: FakeRemoteData
    private lateinit var weatherRepository: WeatherRepository

    @Before
    fun setUp() {
        fakeLocalData = FakeLocalData()
        fakeRemoteData = FakeRemoteData()
        weatherRepository = WeatherRepository(fakeRemoteData, fakeLocalData)
    }

    @Test
    //test the function if it reterives the data from remote source
    fun `fetchFiveDaysInfo returns weather data from remote source`() = runBlocking {
        // Arrange
        val expectedWeatherResponse = CurrentWeatherResponse(
            id = 1, // Make sure this is provided correctly
            city = City(
                id = 12345,
                name = "Cairo",
                country = "EG", // Use the correct country code
                population = 10000000, // Example value, replace as needed
                coord = Coord(lat = 30.033, lon = 31.233), // Provide Coord object
                sunrise = 0, // Provide default  values
                sunset = 0, // Provide default  values
                timezone = 2
            ),
            longitude = 31.233,
            latitude = 30.033,
            cnt = 1,
            isFav = 0,
            isALert = 0,
            cod = "200",
            list = mutableListOf(),  // Populate with necessary forecast data
            message = 0,
            timestamp = null
        )

        fakeRemoteData.weatherResponse = expectedWeatherResponse

        // Act
        val result = weatherRepository.fetchFiveDaysInfo(30.033, 31.233, "metric", "api_key", "ar")

        // Assert
        assertEquals(expectedWeatherResponse, result.first())
    }

    @Test
    //Test fetchFiveDaysInfo to return exception of no weather dataset when the response is failed

    fun `fetchFiveDaysInfo returns exception response when remote source fails`() = runBlocking {
        // Arrange
        fakeRemoteData.weatherResponse = null // Simulate no data

        // Act
        val result = weatherRepository.fetchFiveDaysInfo(30.033, 31.233, "metric", "api_key", "ar")

        // Assert
        assertEquals(0, result.firstOrNull()?.cnt) // Expecting cnt to be 0 based on the default values provided
    }

    @Test
    //return weather from database
    fun `getWeatherData returns cached weather data from local source`() = runBlocking {
        // Arrange
        val expectedWeatherResponse = CurrentWeatherResponse(
            id = 1,
            city = City(
                id = 12345, // Add appropriate values
                name = "Cairo",
                country = "EG", // Use the correct country code
                population = 10000000, // Example value
                coord = Coord(lat = 30.033, lon = 31.233), // Provide Coord object
                sunrise = 0, // Provide default or appropriate values
                sunset = 0, // Provide default or appropriate values
                timezone = 2
            ),
            longitude = 31.233,
            latitude = 30.033,
            cnt = 1,
            isFav = 0,
            isALert = 0,
            cod = "200",
            list = mutableListOf(), // Populate with necessary forecast data
            message = 0,
            timestamp = null
        )

        fakeLocalData.insertWeatherData(expectedWeatherResponse)

        // Act
        val result = weatherRepository.getWeatherData()

        // Assert
        assertEquals(expectedWeatherResponse, result.first())
    }

    @Test
    //to ensure it correclty saves weather response to local storage
    fun `insertFavourite adds a favorite location to local storage`() = runBlocking {
        // Arrange
        val favoriteWeatherResponse = CurrentWeatherResponse(
            id = 1,
            city = City(
                id = 12345, // Add appropriate values
                name = "Cairo",
                country = "EG", // Use the correct country code
                population = 10000000, // Example value
                coord = Coord(lat = 30.033, lon = 31.233), // Provide Coord object
                sunrise = 0, // Provide default or appropriate values
                sunset = 0, // Provide default or appropriate values
                timezone = 2
            ),
            longitude = 31.233,
            latitude = 30.033,
            cnt = 1,
            isFav = 1,
            isALert = 0,
            cod = "200",
            list = mutableListOf(),
            message = 0,
            timestamp = null
        )

        // Act
        weatherRepository.insertFavourite(favoriteWeatherResponse)

        // Assert
        val favorites = fakeLocalData.getFavourite().first()
        assertEquals(1, favorites.size)
        assertEquals(favoriteWeatherResponse, favorites[0])
    }

    @Test
    fun `insertFavourite handles null or default values correctly`() = runBlocking {
        // Arrange: Create a CurrentWeatherResponse with null or default values
        val favoriteWithDefaults = CurrentWeatherResponse(
            id = 0,  // Default id
            city = City(
                id = 0, // Default city id
                name = "", // Default name
                country = "", // Default country code
                population = 0, // Default population
                coord = Coord(lat = 0.0, lon = 0.0), // Default coordinates
                sunrise = 0, // Default sunrise
                sunset = 0, // Default sunset
                timezone = 0 // Default timezone
            ),
            longitude = 0.0, // Default longitude
            latitude = 0.0, // Default latitude
            cnt = 0, // Default count
            isFav = 1, // Mark as favorite
            isALert = 0, // No alert
            cod = "200", // Default code
            list = mutableListOf(), // Empty list
            message = 0, // Default message
            timestamp = null // Default timestamp
        )

        // Act: Call insertFavourite with this object
        weatherRepository.insertFavourite(favoriteWithDefaults)

        // Assert: Check that the favorite is added correctly
        val favorites = fakeLocalData.getFavourite().first()
        assertEquals(1, favorites.size) // Expecting size to be 1
        assertEquals(favoriteWithDefaults, favorites[0]) // The added favorite should match
    }

}
