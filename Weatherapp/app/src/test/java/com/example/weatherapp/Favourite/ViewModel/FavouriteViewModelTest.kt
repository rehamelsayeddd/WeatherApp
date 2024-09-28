import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModel
import com.example.weatherapp.Favourite.ViewModel.FavouriteState
import com.example.weatherapp.ForecastModel.City
import com.example.weatherapp.ForecastModel.Coord
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.Repository.FakeRepositoryTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavouriteViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var fakeRepository: FakeRepositoryTest
    private lateinit var favouriteViewModel: FavouriteViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeRepositoryTest()
        favouriteViewModel = FavouriteViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the Main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `addFavourite should add a favorite and update state to Success`(): Unit = runBlocking {
        // Given
        val favorite =CurrentWeatherResponse(
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
        val observedStates = mutableListOf<FavouriteState>()

        // Launch a coroutine to collect state changes
        val job = launch(testDispatcher) {
            favouriteViewModel.favouriteState.collect { state ->
                observedStates.add(state)
            }
        }

        // When
        favouriteViewModel.addFavourite(favorite)

        // Then
        // Wait for the coroutine to collect values
        delay(100) // You can adjust the delay as necessary for your test timing

        // Check for loading state and then success state
        assert(observedStates.any { it is FavouriteState.Loading })
        assert(observedStates.last() is FavouriteState.Success)
        assert((observedStates.last() as FavouriteState.Success).favourites.contains(favorite)) // Assert the favorite was added

        // Clean up
        job.cancel() // Cancel the collection coroutine
    }

    @Test
    fun `removeFavourite should emit an error when no favorites exist`() = runBlocking {
        // Collect emitted states
        val observedStates = mutableListOf<FavouriteState>()

        // Launch a coroutine to collect state changes
        val job = launch(testDispatcher) {
            favouriteViewModel.favouriteState.collect { state ->
                observedStates.add(state)
                println("State emitted: $state") // Log each emitted state for debugging
            }
        }

        // When: Call the method to remove a favorite that doesn't exist
        val nonExistentLongitude = 31.234 // Example longitude
        val nonExistentLatitude = 30.034 // Example latitude
        favouriteViewModel.removeFavourite(nonExistentLongitude, nonExistentLatitude)

        // Allow some time for the state to update
        delay(200) // Adjust delay as needed for state updates

        // Check for loading state
        val loadingState = observedStates.find { it is FavouriteState.Loading }
        assertNotNull("Expected a loading state", loadingState) // Ensure a loading state was emitted

        // Check for error state
        val errorState = observedStates.lastOrNull()
        assertTrue("Expected an error state", errorState is FavouriteState.Error) // Check for error state
        assertEquals("No favorites found", (errorState as FavouriteState.Error).message) // Validate error message

        // Clean up
        job.cancel() // Cancel the collection coroutine
    }

}
