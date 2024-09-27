package com.example.weatherapp.Settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.weatherapp.Map.View.MapActivity
import com.example.weatherapp.R
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.View.HomeActivity
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefManager: SharedPreferenceManager
    companion object {
        const val MAP_REQUEST_CODE = 1001 // Unique request code for MapActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using View Binding
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize SharedPreferenceManager
        sharedPrefManager = SharedPreferenceManager.getInstance(requireContext())

        // Load the stored preference
        val windSpeedPreference = sharedPrefManager.getWindUnit("wind_speed", "imperial")
        val tempPreference = sharedPrefManager.getTempUnit("temperature_unit", "celsius")
        val locationPreference = sharedPrefManager.getlocationChoice("location_option", "GPS")
        val languagePreference = sharedPrefManager.getLanguage("language_key", "en")



        // Set the default selected radio button
        if (windSpeedPreference == "imperial") {
            binding.mileRbtn.isChecked = true
        } else {
            binding.metricRbtn.isChecked = true
        }

        // Set a listener for the Wind RadioGroup
        binding.windGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.mile_rbtn -> sharedPrefManager.saveWindUnit("wind_speed", "imperial") //km/hour
                R.id.metric_rbtn -> sharedPrefManager.saveWindUnit("wind_speed", "metric") //meter/sec
            }
        }

        // Set the default selected radio button based on preference
        binding.temperatureGroup.apply {
            when (tempPreference) {
                "celsius" -> binding.celsiusRbtn.isChecked = true
                "fahrenheit" -> binding.fahrenheitRbtn.isChecked = true
                "kelvin" -> binding.kelvinRbtn.isChecked = true
                else -> binding.celsiusRbtn.isChecked = true // Default to Celsius if no preference
            }
        }

        // Set a listener for the Temperature RadioGroup
        binding.temperatureGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.celsius_rbtn -> sharedPrefManager.saveTempUnit("temperature_unit", "celsius")
                R.id.fahrenheit_rbtn -> sharedPrefManager.saveTempUnit("temperature_unit", "fahrenheit")
                R.id.kelvin_rbtn -> sharedPrefManager.saveTempUnit("temperature_unit", "kelvin")
            }
        }

        // Set the default selected radio button for Location (GPS or Map)
        if (locationPreference == "GPS") {
            binding.gpsRbtn.isChecked = true
        } else {
            binding.mapRbtn.isChecked = true
        }

        // Set a listener for the Location RadioGroup
        binding.locationGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.gps_rbtn -> sharedPrefManager.savelocationChoice("location_option", "GPS")
                R.id.map_rbtn -> {
                    sharedPrefManager.savelocationChoice("location_option", "Map")
                    openMapActivity()
                }
            }
        }

        // Set the default selected radio button for language
        if (languagePreference == "ar") {
            binding.languageGroup.check(R.id.arabic_rbtn)
        } else {
            binding.languageGroup.check(R.id.english_rbtn) // Default to English
        }

        // Set a listener for the Language RadioGroup
        binding.languageGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.english_rbtn -> {
                    sharedPrefManager.saveLanguage("language_key", "en")
                    setLocale("en")
                }
                R.id.arabic_rbtn -> {
                    sharedPrefManager.saveLanguage("language_key", "ar")
                    setLocale("ar")
                }
            }
        }


        return binding.root
    }

    private fun setLocale(languageCode: String) {
        // Get the current resources and create a new Configuration object
        val resources = requireContext().resources
        val config = Configuration(resources.configuration)

        // Set the locale based on the provided language code
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        config.setLocale(locale)

        // Update the resources with the new configuration
        resources.updateConfiguration(config, resources.displayMetrics)

        // Set the layout direction based on the language
        ViewCompat.setLayoutDirection(
            requireActivity().window.decorView,
            if (languageCode == "ar") ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR
        )

        // Refresh the fragments to apply the new language
        replaceFragments(SettingsFragment())
        updateBottomNavigationBar()
    }
    private fun replaceFragments(fragment: Fragment) {
        val transaction = (context as HomeActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun updateBottomNavigationBar() {
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Get the current language from Shared Preferences
        val languagePreference = sharedPrefManager.getLanguage("language_key", "en")

        // Set the titles based on the current language
        if (languagePreference == "ar") {
            bottomNavigationView.menu.findItem(R.id.home).title = "الرئيسية"
            bottomNavigationView.menu.findItem(R.id.favourite).title = "المفضلة"
            bottomNavigationView.menu.findItem(R.id.alert).title = "تنبيهات"
            bottomNavigationView.menu.findItem(R.id.settings).title = "الإعدادات"
        } else {
            bottomNavigationView.menu.findItem(R.id.home).title = "Home"
            bottomNavigationView.menu.findItem(R.id.favourite).title = "Favourite"
            bottomNavigationView.menu.findItem(R.id.alert).title = "Alerts"
            bottomNavigationView.menu.findItem(R.id.settings).title = "Settings"
        }
    }


    // Function to open the MapActivity when the Map radio button is selected
    private fun openMapActivity() {
        val intent = Intent(requireContext(), MapActivity::class.java).apply {
            putExtra(MapActivity.REQUEST_TYPE, MapActivity.FROM_SETTINGS)
        }
        startActivityForResult(intent, MAP_REQUEST_CODE)
    }

    // Handle the result from MapActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            data?.let {
                val latitude = it.getDoubleExtra("latitude", 0.0)
                val longitude = it.getDoubleExtra("longitude", 0.0)

                // Validate latitude and longitude
                if (latitude != 0.0 && longitude != 0.0) {
                    sharedPrefManager.saveLocationCoordinates(latitude, longitude)
                } else {
                    // Handle invalid location data if necessary
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding to avoid memory leaks
        _binding = null
    }
}
