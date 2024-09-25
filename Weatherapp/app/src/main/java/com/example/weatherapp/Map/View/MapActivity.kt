package com.example.weatherapp.Map.View

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.Favourite.View.FavouriteFragment
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModel
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModelFactory
import com.example.weatherapp.LocationData
import com.example.weatherapp.Map.ViewModel.MapViewModel
import com.example.weatherapp.Map.ViewModel.MapViewModelFactory
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.Network.RetrofitInstance
import com.example.weatherapp.R
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.View.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.Autocomplete
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var saveLocationButton: Button
    private var selectedLatLng: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchEditText: EditText
    private lateinit var geocoder: Geocoder
    private lateinit var searchQueryFlow: MutableSharedFlow<String>
    private lateinit var repository: WeatherRepository
    private lateinit var viewModelFactory: MapViewModelFactory
    private lateinit var remoteData: RemoteData
    private lateinit var localData: LocalData
    private lateinit var viewModel: MapViewModel


//    private val viewModel: MapViewModel by viewModels {
//        MapViewModelFactory(WeatherRepository.getInstance(remoteData, localData))
//    }

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 1
        const val API_KEY = "3f2c5a9a086fa7d7056043da97b35aae"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

      //  remoteData = RemoteData()
        //localData = LocalData(requireContext())
       // repository = WeatherRepository.getInstance(remoteData,localData)

        // Create an instance of FavouriteViewModelFactory with your repository
       // viewModelFactory = MapViewModelFactory(repository)
        //viewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)

        repository = WeatherRepository(RemoteData(), LocalData(this))
        viewModel = ViewModelProvider(this, MapViewModelFactory(repository)).get(MapViewModel::class.java)

        mapView = findViewById(R.id.map_view)
        saveLocationButton = findViewById(R.id.btn_save_location)
        searchEditText = findViewById(R.id.et_searchMap)

        searchQueryFlow = MutableSharedFlow(replay = 1)

        // Initialize the MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        setupSearchEditText()
        observeSearchQuery()

        // Setup the save button listener
        saveLocationButton.setOnClickListener {
            selectedLatLng?.let { latLng ->
                fetchWeatherData(latLng.latitude, latLng.longitude) { cityName, countryName ->
                    Log.d("MapActivity", "Saving location: lat=${latLng.latitude}, lon=${latLng.longitude}, city=$cityName, country=$countryName")

                    // Save the location
                    viewModel.saveLocation(latLng.latitude, latLng.longitude, cityName, countryName)

                    // Log success
                    Log.d("MapActivity", "Location saved successfully!")

                    // Prepare the result intent to pass back the data
                    val resultIntent = Intent().apply {
                        putExtra("cityName", cityName)
                        putExtra("countryName", countryName)
                        putExtra("latitude", latLng.latitude)
                        putExtra("longitude", latLng.longitude)
                    }
                    setResult(RESULT_OK, resultIntent) // Set the result code and data

                    // Close the MapActivity
                    finish()

                    Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
        }




    }

    private fun observeSearchQuery() {
        lifecycleScope.launch {
            searchQueryFlow
                .debounce(300) // Debounce to limit the frequency of emissions
                .filter { it.isNotBlank() } // Only proceed if the query is not empty
                .collect { query ->
                    Log.d("MapActivity", "Searching for: $query")
                    searchForLocation(query)
                }
        }
    }

    private fun setupSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    searchQueryFlow.emit(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchForLocation(locationName: String) {
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                moveCameraToLocation(latLng)
                addMarkerAtLocation(latLng, address.getAddressLine(0))
            } else {
                Toast.makeText(this, getString(R.string.notfoundlocation), Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String) {
        map.clear() // Clear previous markers
        map.addMarker(MarkerOptions().position(location).title(title))
        selectedLatLng = location
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getCurrentLocation()

        // Set up a click listener for the map to drop a pin
        map.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            addMarkerAtLocation(latLng, "Selected Location")
        }
    }

    private fun getCurrentLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    moveCameraToLocation(currentLatLng)
                    addMarkerAtLocation(currentLatLng, "Current Location")
                }
            }
        } else {
            // Handle permission request if necessary
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double, callback: (String, String) -> Unit) {
        viewModel.fetchWeatherData(latitude, longitude).observe(this) { response ->
            response?.let {
                val cityName = it.city?.name ?: "Unknown City"
                val countryName = it.city?.country ?: "Unknown Country"
                callback(cityName, countryName)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}


//    private fun setupSearchEditText() {
//        searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                // Cancel previous search job
//                searchJob?.cancel()
//
//                // Start a new search job with debounce
//                searchJob = MainScope().launch {
//                    val query = s.toString()
//                    if (query.isNotEmpty()) {
//                        delay(300) // Debounce
//                        searchForLocation(query)
//                    }
//                }
//            }
//        })
//    }

//    private fun searchForLocation(locationName: String) {
//        try {
//            val addresses = geocoder.getFromLocationName(locationName, 1)
//            if (addresses != null) {
//                if (addresses.isNotEmpty()) {
//                    val address = addresses[0]
//                    val latLng = LatLng(address.latitude, address.longitude)
//                    moveCameraToLocation(latLng)
//                    addMarkerAtLocation(latLng, address.getAddressLine(0))
//                } else {
//                    Toast.makeText(this, getString(R.string.notfoundlocation), Toast.LENGTH_SHORT).show()
//                }
//            }
//        } catch (e: IOException) {
//            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show()
//        }
//    }
