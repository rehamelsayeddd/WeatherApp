package com.example.weatherapp.Home.View

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.ForecastApiState
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.Home.ViewModel.HomeViewModel
import com.example.weatherapp.Home.ViewModel.HomeViewModelFactory
import com.example.weatherapp.Network.NetworkUtil
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.Network.RetrofitInstance
import com.example.weatherapp.Network.WeatherApiService
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.SharedKey
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1000
      const val API_KEY = "3f2c5a9a086fa7d7056043da97b35aae"

    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var todayAdapter: TodayAdapter
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var viewModel: HomeViewModel
    lateinit var repository: WeatherRepository
    lateinit var remoteData: RemoteData
    lateinit var localData: LocalData
    lateinit var viewModelFactory: HomeViewModelFactory
    private var mainLongitude:Double =0.0
    private var mainLatitude: Double =0.0
    private lateinit var sharedPrefManager: SharedPreferenceManager




    private val weatherApiService: WeatherApiService by lazy {
        RetrofitInstance.api
    }


    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            checkLocationSettings()
        } else {
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()

    }





    private fun checkAndRequestPermissions() {
        val sharedPrefManager = SharedPreferenceManager.getInstance(requireContext())
        val locationPreference = sharedPrefManager.getlocationChoice("location_option", "GPS")

        if (locationPreference == "GPS") {
            // Proceed with GPS location
            if (!checkPermission()) {
                requestPermission()
            } else if (!isLocationEnabled(requireContext())) {
                enableLocation()
            } else {
                getFreshLocation()
            }
        } else {
            // Load saved coordinates directly
            val (latitude, longitude) = sharedPrefManager.getLocationCoordinates()
            fetchWeatherData(latitude, longitude)
        }
    }



    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getFreshLocation() {
        if (checkPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    handleLocation(location)
                } else {
                    requestCoarseLocation() // Fallback if last location is null
                }
            }
        } else {
            requestPermission()
        }
    }

    private fun requestCoarseLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = object : android.location.LocationListener {
                override fun onLocationChanged(location: Location) {
                    handleLocation(location)
                    locationManager.removeUpdates(this) // Stop updates after getting the location
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } else {
            requestPermission()
        }
    }

    private fun enableLocation() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getFreshLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            } else {
                Toast.makeText(requireContext(), "Location settings are required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleLocation(location: Location) {
        if (!isAdded) return // Check if fragment is attached before accessing context

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        if (addresses?.isNotEmpty() == true) {
            val cityName = addresses[0].locality ?: "Unknown city"
            binding.tvCity.text = cityName


            mainLongitude = location.longitude
            mainLatitude= location.latitude
            // Save location to SharedPreferences
            val sharedPrefManager = SharedPreferenceManager.getInstance(requireContext())
          //  sharedPrefManager.saveLocationToAlert("current_location", location.longitude, location.latitude)

            sharedPrefManager.saveLocationCoordinates(mainLatitude, mainLongitude) // Store the location coordinates


            sharedPrefManager.saveLocationToAlert(SharedKey.ALERT.name, mainLongitude,mainLatitude)


            // Fetch weather data after getting the location
          //  fetchWeatherData(location.latitude, location.longitude)

            fetchWeatherData(mainLatitude, mainLongitude)

        } else {
            Toast.makeText(requireContext(), "Unable to get address from location.", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPermission() {
        requestLocationPermissionsLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getFreshLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            } else {
                Toast.makeText(requireContext(), "Location settings are required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                getFreshLocation()
            } else {
                Toast.makeText(requireContext(), "Location settings are required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }
    // Filter the forecast to get the first entry of each day for the next 5 days
    private fun getFiveDayForecast(weatherList: List<ForeCastData>): List<ForeCastData> {
        val filteredList = mutableListOf<ForeCastData>()
        var currentDay = ""
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

        // Iterate through the weather data
        for (forecast in weatherList) {
            // Convert the forecast's timestamp to a date string (just the day)
            val forecastDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(forecast.dt * 1000L)

            // Skip the current day
            if (forecastDate == currentDate) {
                continue
            }

            // If we haven't added this day yet, add the forecast to the list
            if (forecastDate != currentDay) {
                filteredList.add(forecast)
                currentDay = forecastDate
            }

            // Stop once we have 5 days of data
            if (filteredList.size == 5) {
                break
            }
        }

        return filteredList
    }



    private fun fetchWeatherData(latitude: Double, longitude: Double) {
      //  viewModel.fetchWeather(latitude, longitude, API_KEY, "en", "metric")

        // Get the saved wind unit from SharedPreferences
        val windUnit = SharedPreferenceManager.getInstance(requireContext()).getWindUnit("wind_speed", "imperial")
        // Determine the unit to pass to the API based on the saved preference
        val unitParameter = if (windUnit == "metric") "metric" else "imperial"

        // Get the saved temperature unit from SharedPreferences
        val tempUnit = SharedPreferenceManager.getInstance(requireContext()).getTempUnit("temperature_unit", "celsius")


        // Get the saved language preference from SharedPreferences
        val languagePreference = SharedPreferenceManager.getInstance(requireContext()).getLanguage("language", "")
        val languageForAPI = when (languagePreference) {
            "ar" -> "ar" // Arabic
            "en" -> "en" // English
            else -> ""
        }
        Log.d("WeatherAPI", "Fetching weather data for latitude: $latitude, longitude: $longitude, language: $languageForAPI")



        // Determine the unit to pass to the API based on the saved temperature preference
        val unitForAPI = when (tempUnit) {
            "imperial" -> "imperial" // Fahrenheit and mph for wind
            else -> "metric" // Celsius and m/s for wind
        }

        // Fetch weather data using the selected unit for the API (metric or imperial)
       // viewModel.fetchWeather(latitude, longitude, API_KEY, "en", unitForAPI)

        viewModel.fetchWeather(latitude, longitude, API_KEY, languageForAPI, unitForAPI)

        }

    private fun updateUI(weatherData: CurrentWeatherResponse) {
        Log.d("WeatherAPI", "Weather data: $weatherData")

        // Update todayAdapter with the current day's details
        todayAdapter.submitList(weatherData.list.subList(0, 8)) // Assuming this is the current day's data

        // Update dailyAdapter with the 5-day forecast
        val dailyForecast = getFiveDayForecast(weatherData.list)
        dailyAdapter.submitList(dailyForecast)

        val languagePreference = SharedPreferenceManager.getInstance(requireContext()).getLanguage("language_key", "")

        // Update general UI components
      //  binding.tvCity.text = "${weatherData.city.name}, ${weatherData.city.country}"

        // Update based on the language preference
        val cityName = weatherData.city.name // The name should reflect the selected language
        val countryName = weatherData.city.country // The country should also reflect the selected language
        val weatherStatus = weatherData.list[0].weather[0].description // Should reflect the selected language

        Log.d("WeatherAPI", "City: $cityName, Country: $countryName, Status: $weatherStatus")

        binding.tvCity.text = "$cityName, $countryName"
        binding.tvStatus.text = weatherStatus


        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(weatherData.list[0].dt * 1000L))
        binding.tvDayFormat.text = formattedDate



        // binding.tvStatus.text = weatherData.list[0].weather[0].description

        // Get temperature unit preference
        val tempUnit = SharedPreferenceManager.getInstance(requireContext()).getTempUnit("temperature_unit", "celsius")

        // Convert temperature based on the unit
        val temp = weatherData.list[0].main.temp
        val tempText = when (tempUnit) {
            "celsius" -> "${temp}°C" // Celsius
            "fahrenheit" -> "${(temp * 9/5 + 32).toInt()}°F" // Fahrenheit
            "kelvin" -> "${(temp + 273.15).toInt()}K" // Kelvin
            else -> "${temp}°C" // Default to Celsius
        }
        binding.tvTemp.text = tempText

        //binding.tvTemp.text = "${weatherData.list[0].main.temp}°C"

        binding.pressurePercent.text = "${weatherData.list[0].main.pressure} hPa"
        binding.humidityPercent.text = "${weatherData.list[0].main.humidity}%"



        // Get wind unit preference
        val windUnit = SharedPreferenceManager.getInstance(requireContext()).getWindUnit("wind_speed", "imperial")

        // Convert wind speed based on the unit
        val windSpeed = weatherData.list[0].wind.speed
        val windSpeedText = if (windUnit == "metric") {
            "${windSpeed} m/s" // wind speed in meters/second
        } else {
            // Convert m/s to mph (1 m/s = 2.23694 mph)
            "${(windSpeed * 2.23694).toInt()} mile/h"
        }
        binding.windPercent.text = windSpeedText


        //  binding.windPercent.text = "${weatherData.list[0].wind.speed} m/h"

        val iconCode = weatherData.list[0].weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
        Picasso.get().load(iconUrl).into(binding.weatherImgView)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        remoteData = RemoteData()
        localData = LocalData(requireContext())
        repository = WeatherRepository.getInstance(remoteData,localData)
        viewModelFactory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView called")
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    ///////////////////////////////////////////////

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun fetchWeatherDataWithFallback(latitude: Double, longitude: Double) {
        if (isNetworkAvailable()) {
            // Show loading indicator while fetching data from API
            binding.progressBar.visibility = View.VISIBLE
            binding.scrollView2.visibility = View.GONE

            // Try to fetch weather data from the API
            fetchWeatherData(latitude, longitude)
        } else {
            // No internet connection, show loading offline data message
            Toast.makeText(requireContext(), "No internet connection. Loading offline data.", Toast.LENGTH_SHORT).show()

            // Fetch data from Room and update UI
            loadOfflineWeatherData()
        }
    }
    private fun loadOfflineWeatherData() {
        // Show loading indicator while fetching data from Room
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView2.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.getWeatherData().collect { weatherData ->
                if (weatherData != null) {
                    // Update UI with cached data from Room
                    updateUI(weatherData)

                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView2.visibility = View.VISIBLE
                } else {
                    // No cached data available, hide the loading spinner
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "No cached data available.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)


        sharedPrefManager   = SharedPreferenceManager.getInstance(requireContext())

        // Initialize adapters
        todayAdapter = TodayAdapter(sharedPrefManager)

        dailyAdapter = DailyAdapter(sharedPrefManager)

        // Set up RecyclerViews
        binding.todayDetailsRecView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.todayDetailsRecView.adapter = todayAdapter

        binding.FivedaysRec.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.FivedaysRec.adapter = dailyAdapter


        lifecycleScope.launchWhenStarted {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is ForecastApiState.Loading -> {
                        if (isNetworkAvailable()) {
                            // Network is available, so show loading indicator
                            binding.progressBar.visibility = View.VISIBLE
                            binding.scrollView2.visibility = View.GONE
                        } else {
                            // No internet, load offline data
                            Toast.makeText(requireContext(), "No internet connection. Loading offline data.", Toast.LENGTH_SHORT).show()
                            loadOfflineWeatherData()
                        }
                    }

                    is ForecastApiState.Success -> {
                        if (isNetworkAvailable()) {
                            // Network is available, hide loading indicator and update UI
                            binding.scrollView2.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                            updateUI(state.data)
                        } else {
                            // No internet, display cached data if available
                            Toast.makeText(requireContext(), "No internet connection. Displaying cached data.", Toast.LENGTH_SHORT).show()
                            loadOfflineWeatherData()
                        }
                    }

                    is ForecastApiState.Error -> {
                        if (isNetworkAvailable()) {
                            // Network is available but there's an error, hide the loading spinner and show error message
                            binding.progressBar.visibility = View.GONE
                            binding.scrollView2.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        } else {
                            // No internet and an error occurred, load offline data
                            Toast.makeText(requireContext(), "No internet connection. Loading offline data.", Toast.LENGTH_SHORT).show()
                            loadOfflineWeatherData()
                        }
                    }
                }
            }
        }
        // Check location permissions and settings
        checkAndRequestPermissions()
    }


}



//    private fun fetchWeatherData(latitude: Double, longitude: Double) {
//        lifecycleScope.launch {
//            try {
//                val weatherResponse = weatherApiService.getForeCast(
//                    lat = latitude,
//                    long = longitude,
//                    units = "metric",
//                    apiKey = API_KEY,
//                    lang = "en"
//                )
//
//                if (isAdded){
//                // Update UI with the fetched data
//                    binding.tvCity.text =
//                        "${weatherResponse.city.name}, ${weatherResponse.city.country}" // Display CityName, Country
//                binding.weatherImgView
//                val dateFormat =
//                    java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault())
//                val formattedDate =
//                    dateFormat.format(java.util.Date(weatherResponse.list[0].dt * 1000L))
//                binding.tvDayFormat.text = formattedDate // Set the formatted date
//                binding.tvStatus.text = weatherResponse.list[0].weather[0].description
//                binding.tvTemp.text = "${weatherResponse.list[0].main.temp}°C"
//                binding.pressurePercent.text = "${weatherResponse.list[0].main.pressure} hPa"
//                binding.humidityPercent.text = "${weatherResponse.list[0].main.humidity}%"
//                binding.windPercent.text = "${weatherResponse.list[0].wind.speed} km/h"
//
//                // Load the weather icon into the ImageView using Picasso
//                val iconCode = weatherResponse.list[0].weather[0].icon
//                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
//                Picasso.get().load(iconUrl).into(binding.weatherImgView)
//
//                // Update RecyclerViews with weather forecast data
//                todayAdapter.submitList(
//                    weatherResponse.list.subList(
//                        0,
//                        8
//                    )
//                ) // Assuming 3-hour interval data
//                // dailyAdapter.submitList(weatherResponse.list) // Daily forecast
//
//                // Grouping forecast data by date
//                val dailyForecast = getFiveDayForecast(weatherResponse.list.groupBy { forecast ->
//                    SimpleDateFormat(
//                        "yyyy-MM-dd",
//                        Locale.getDefault()
//                    ).format(Date(forecast.dt * 1000L))
//                }
//                    .map { (_, forecasts) -> forecasts.first() }) // Get the first forecast for each day
//
//                // Update RecyclerViews with the filtered daily forecast data
//                dailyAdapter.submitList(dailyForecast)
//
//            }
//
//            } catch (e: Exception) {
//                Toast.makeText(requireContext(), "Failed to fetch weather data: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

//    private fun checkAndRequestPermissions() {
//        if (!checkPermission()) {
//            requestPermission()
//        } else if (!isLocationEnabled(requireContext())) {
//            enableLocation()
//        } else {
//            getFreshLocation()
//        }
//    }

// Observe the forecast state
//        lifecycleScope.launchWhenStarted {
//            viewModel.forecastState.collect { state ->
//                when (state) {
//                    is ForecastApiState.Loading -> {
//                        // Show loading indicator
//                        binding.progressBar.visibility = View.VISIBLE
//                        binding.scrollView2.visibility = View.GONE
//
//
//                    }
//                    is ForecastApiState.Success -> {
//                        // Hide loading indicator and update UI
//                        binding.scrollView2.visibility = View.VISIBLE
//                        binding.progressBar.visibility = View.GONE
//                        updateUI(state.data)
//                    }
//                    is ForecastApiState.Error -> {
//                        // Hide loading indicator and show error message
//                        binding.progressBar.visibility = View.VISIBLE
//                        binding.scrollView2.visibility = View.GONE
//                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
//
//                        loadOfflineWeatherData()
//                    }
//                }
//            }
//        }