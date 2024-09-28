import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.OneCall.Model.AlertData
import com.example.weatherapp.Repository.FakeRepositoryTest
import com.example.weatherapp.Alert.ViewModel.AlertViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AlertViewModelTest {

    private lateinit var alertViewModel: AlertViewModel
    private lateinit var fakeRepository: FakeRepositoryTest

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        fakeRepository = FakeRepositoryTest() // Initialize the fake repository
        alertViewModel = AlertViewModel(fakeRepository) // Pass the fake repository to the ViewModel
    }

    @Test
    fun `insertOneALert adds an alert`() = runBlocking {
        val alertData = createSampleAlertData()

        // Assert that alerts list is initially empty
        assertTrue(alertViewModel.alerts.value.isNullOrEmpty())

        // Insert the alert
        alertViewModel.insertOneALert(alertData)

        // Wait for LiveData to update
        kotlinx.coroutines.delay(100)

        // Verify the alert was added
        val currentAlerts = alertViewModel.alerts.value
        assertEquals(1, currentAlerts?.size) // Check if one alert has been added
        assertTrue(currentAlerts?.contains(alertData) == true) // Verify that the added alert is present
    }


    @Test
    fun `deleteOneAlert removes an alert`() = runBlocking {
        val alertData = createSampleAlertData()
        alertViewModel.insertOneALert(alertData) // Insert the alert first

        alertViewModel.deleteOneAlert(alertData) // Now delete the alert

        // Get the current alerts from the ViewModel
        val currentAlerts = alertViewModel.alerts.value ?: emptyList()
        assertEquals(0, currentAlerts.size) // Expecting no alerts after deletion
    }

    private fun createSampleAlertData(): AlertData {
        return AlertData(
            fromTime = "12:00",
            fromDate = "2024-09-30",
            toTime = "13:00",
            toDate = "2024-09-30",
            milleTimeFrom = 1234567890,
            milleDateFrom = 1234567890,
            milleTimeTo = 1234567890,
            milleDateTo = 1234567890,
            requestCode = 1L,
            lontitude = "30.0",
            lattiude = "30.0"
        )
    }
}
