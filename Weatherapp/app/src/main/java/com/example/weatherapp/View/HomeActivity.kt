package com.example.weatherapp.View

import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.weatherapp.Alert.AlertFragment
import com.example.weatherapp.Favourite.FavouriteFragment
import com.example.weatherapp.Home.HomeFragment
import com.example.weatherapp.Network.NetworkUtil
import com.example.weatherapp.R
import com.example.weatherapp.Settings.SettingsFragment
import com.example.weatherapp.databinding.ActivityHomeBinding

import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var networkReceiver: NetworkUtil
    private var isNetworkAvailable: Boolean = false
    private lateinit var navController: NavController
  //  private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enabling edge-to-edge display if necessary
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

     //   sharedPreferencesManager = SharedPreferencesManager.getInstance(this)
   //     sharedPreferencesManager.savelocationChoice(SharedKey.GPS.name, "gps")

        // Set locale based on saved preference
     //   val language = sharedPreferencesManager.getLanguage(SharedKey.LANGUAGE.name, "en")
       // setLocale(language)

        // Setup BottomNavigationView
        replaceFragment(HomeFragment())
        setupBottomNavigationView()

        // Setup network receiver
        networkReceiver = NetworkUtil { isConnected ->
            isNetworkAvailable = isConnected
            updateNetworkIndicator()
        }

        // Load HomeFragment as the initial fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (isNetworkAvailable) {
                binding.tvNetworkIndicator.visibility = View.GONE

                when (item.itemId) {
                    R.id.home -> replaceFragment(HomeFragment())
                    R.id.alert -> replaceFragment(AlertFragment())
                    R.id.settings -> replaceFragment(SettingsFragment())
                    R.id.favourite -> replaceFragment(FavouriteFragment())
                }

            } else {
                binding.tvNetworkIndicator.visibility = View.VISIBLE

                when (item.itemId) {
                    R.id.home -> replaceFragment(HomeFragment())
                    R.id.alert -> replaceFragment(AlertFragment())
                    R.id.settings -> replaceFragment(SettingsFragment())
                    R.id.favourite -> replaceFragment(FavouriteFragment())
                }
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }

    private fun updateNetworkIndicator() {
        binding.tvNetworkIndicator.visibility = if (isNetworkAvailable) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Ensure this matches your XML layout ID
            .addToBackStack(null)
            .commit()
    }

    private fun setLocale(language: String) {
        val resources = this.resources
        val config = Configuration(resources.configuration)
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        ViewCompat.setLayoutDirection(this.window.decorView, if (language == "ar") ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR)
    }

    private fun enableEdgeToEdge() {
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}
