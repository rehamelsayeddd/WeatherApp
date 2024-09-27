package com.example.weatherapp.Favourite.View

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.ForecastApiState
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModel
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModelFactory
import com.example.weatherapp.FavouriteState
import com.example.weatherapp.Home.View.DailyAdapter
import com.example.weatherapp.Home.View.HomeFragment
import com.example.weatherapp.Home.View.HomeFragment.Companion.API_KEY
import com.example.weatherapp.Home.View.TodayAdapter
import com.example.weatherapp.Home.ViewModel.HomeViewModel
import com.example.weatherapp.Home.ViewModel.HomeViewModelFactory
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.databinding.FragmentFavouriteDetailsBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class FavouriteDetailsFragment : Fragment() {

    private var _binding: FragmentFavouriteDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var favouriteViewModel: FavouriteViewModel
    private lateinit var repository: WeatherRepository
    lateinit var HomeviewModelFactory: HomeViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelFactory: FavouriteViewModelFactory
    private lateinit var favoriteViewModel: FavouriteViewModel
    private lateinit var sharedPrefManager: SharedPreferenceManager
    private lateinit var todayAdapter: TodayAdapter
    private lateinit var dailyAdapter: DailyAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Repository and ViewModels
        val remoteData = RemoteData()
        val localData = LocalData(requireContext())
        repository = WeatherRepository.getInstance(remoteData, localData)

        viewModelFactory = FavouriteViewModelFactory(repository)
        favoriteViewModel =
            ViewModelProvider(this, viewModelFactory).get(FavouriteViewModel::class.java)

        HomeviewModelFactory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, HomeviewModelFactory).get(HomeViewModel::class.java)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavouriteDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments from the bundle
        val cityName = arguments?.getString("cityName") ?: ""
        val countryName = arguments?.getString("countryName") ?: ""
        val latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        val longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0

        sharedPrefManager   = SharedPreferenceManager.getInstance(requireContext())

        // Initialize adapters
        todayAdapter = TodayAdapter(sharedPrefManager)
        dailyAdapter = DailyAdapter(sharedPrefManager)

        // Fetch weather data for the saved location
        fetchWeatherData(latitude, longitude)

        // Set up RecyclerView
        binding.todayDetailsRecView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.FivedaysRec.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Observe the forecast state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is ForecastApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.scrollView.visibility = View.GONE
                    }
                    is ForecastApiState.Success -> {
                        binding.scrollView.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        updateUI(state.data)
                    }
                    is ForecastApiState.Error -> {
                        binding.progressBar.visibility = View.GONE // Hide progress bar on error
                        binding.scrollView.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
      //  viewModel.fetchWeather(latitude, longitude, HomeFragment.API_KEY, "en", "metric")

        // Get the saved wind unit from SharedPreferences
        val windUnit = SharedPreferenceManager.getInstance(requireContext()).getWindUnit("wind_speed", "imperial")
        // Determine the unit to pass to the API based on the saved preference
        val unitParameter = if (windUnit == "metric") "metric" else "imperial"

        // Get the saved temperature unit from SharedPreferences
        val tempUnit = SharedPreferenceManager.getInstance(requireContext()).getTempUnit("temperature_unit", "celsius")


        // Determine the unit to pass to the API based on the saved temperature preference
        val unitForAPI = when (tempUnit) {
            "imperial" -> "imperial" // Fahrenheit and mph for wind
            else -> "metric" // Celsius and m/s for wind
        }

        // Fetch weather data using the selected unit for the API (metric or imperial)
        viewModel.fetchWeather(latitude, longitude, API_KEY, "en", unitForAPI)
    }

    private fun updateUI(weatherData: CurrentWeatherResponse) {
        binding.tvCity.text = "${weatherData.city.name}, ${weatherData.city.country}"
        val dateFormat = java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale.getDefault())
        val formattedDate = dateFormat.format(java.util.Date(weatherData.list[0].dt * 1000L))
        binding.tvDayFormat.text = formattedDate
        binding.tvStatus.text = weatherData.list[0].weather[0].description




        //Get temperature unit preference
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
        //  binding.tvTemp.text = "${weatherData.list[0].main.temp}°C"



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

       // binding.windPercent.text = "${weatherData.list[0].wind.speed} m/h"

        // Load the weather icon
        val iconCode = weatherData.list[0].weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
        Picasso.get().load(iconUrl).into(binding.weatherImgView)


        // Update RecyclerViews 
        todayAdapter.submitList(weatherData.list.subList(0, 8))
        binding.todayDetailsRecView.adapter = todayAdapter

        val dailyForecast = getFiveDayForecast(weatherData.list)
        dailyAdapter.submitList(dailyForecast)
        binding.FivedaysRec.adapter = dailyAdapter
    }

    private fun getFiveDayForecast(weatherList: List<ForeCastData>): List<ForeCastData> {
        val filteredList = mutableListOf<ForeCastData>()
        val currentDay = ""
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(System.currentTimeMillis())

        for (forecast in weatherList) {
            val forecastDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(forecast.dt * 1000L)

            if (forecastDate == currentDate) continue

            if (forecastDate != currentDay) {
                filteredList.add(forecast)
            }

            if (filteredList.size == 5) break
        }

        return filteredList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
