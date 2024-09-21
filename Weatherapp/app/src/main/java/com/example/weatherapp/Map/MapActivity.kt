package com.example.weatherapp.Map

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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.Network.RetrofitInstance
import com.example.weatherapp.R
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
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var saveLocationButton: Button
    private var selectedLatLng: LatLng? = null
    private var searchJob: Job? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchEditText: EditText
    private lateinit var geocoder: Geocoder

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 1
        const val API_KEY = "3f2c5a9a086fa7d7056043da97b35aae"
        const val MAPS_KEY = "AIzaSyATC4Zk0_xofsFUTm0GRIyNej3syHx5oro"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)
        saveLocationButton = findViewById(R.id.btn_save_location)
        searchEditText = findViewById(R.id.et_searchMap)

        // Initialize the MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        // Setup the save button listener
        saveLocationButton.setOnClickListener {
            selectedLatLng?.let {
                fetchWeatherData(it.latitude, it.longitude)
            } ?: Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
        }
        setupSearchEditText()
    }

    private fun setupSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel previous search job
                searchJob?.cancel()

                // Start a new search job with debounce
                searchJob = MainScope().launch {
                    val query = s.toString()
                    if (query.isNotEmpty()) {
                        delay(300) // Debounce
                        searchForLocation(query)
                    }
                }
            }
        })
    }

    private fun searchForLocation(locationName: String) {
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    moveCameraToLocation(latLng)
                    addMarkerAtLocation(latLng, address.getAddressLine(0))
                } else {
                    Toast.makeText(this, getString(R.string.notfoundlocation), Toast.LENGTH_SHORT).show()
                }
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

        // Get the current location and add a marker
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            selectedLatLng = place.latLng
            if (selectedLatLng != null) {
                addMarkerAtLocation(selectedLatLng!!, place.name)
                moveCameraToLocation(selectedLatLng!!)
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        // Coroutine to fetch weather data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getForeCast(
                    lat = latitude,
                    long = longitude,
                    units = "metric",
                    apiKey = API_KEY,
                    lang = "en"
                )

                val cityName = response.city.name
                val countryName = response.city.country // Assuming your API response includes country info

                // Prepare data to return
                val intent = Intent().apply {
                    putExtra("cityName", cityName)
                    putExtra("countryName", countryName) // Add country name
                }
                setResult(RESULT_OK, intent)
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WeatherError", "Error fetching weather data: ${e.message}")
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
        searchJob?.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
