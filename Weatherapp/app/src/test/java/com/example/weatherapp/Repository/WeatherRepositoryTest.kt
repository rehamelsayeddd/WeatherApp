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
// Test inserting a new unique favourite location
    fun `insertFavourite adds a new unique favorite location`() = runBlocking {
        // Arrange: Insert an initial favorite location
        val initialFavoriteWeatherResponse = CurrentWeatherResponse(
            id = 1,
            city = City(
                id = 12345,
                name = "Cairo",
                country = "EG",
                population = 10000000,
                coord = Coord(lat = 30.033, lon = 31.233),
                sunrise = 0,
                sunset = 0,
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

        // Insert the initial favorite
        weatherRepository.insertFavourite(initialFavoriteWeatherResponse)

        // Arrange: Prepare a new favorite location
        val newFavoriteWeatherResponse = CurrentWeatherResponse(
            id = 2,
            city = City(
                id = 67890,
                name = "Alexandria",
                country = "EG",
                population = 5000000,
                coord = Coord(lat = 31.2156, lon = 29.9553),
                sunrise = 0,
                sunset = 0,
                timezone = 2
            ),
            longitude = 29.9553,
            latitude = 31.2156,
            cnt = 1,
            isFav = 1,
            isALert = 0,
            cod = "200",
            list = mutableListOf(),
            message = 0,
            timestamp = null
        )

        // Act: Insert the new favorite
        weatherRepository.insertFavourite(newFavoriteWeatherResponse)

        // Assert: Check the size of the favorites list
        val favorites = fakeLocalData.getFavourite().first()
        assertEquals(2, favorites.size) // There should be two favorites now
        assertEquals(initialFavoriteWeatherResponse, favorites[0]) // Check the first favorite
        assertEquals(newFavoriteWeatherResponse, favorites[1]) // Check the second favorite
    }

}
