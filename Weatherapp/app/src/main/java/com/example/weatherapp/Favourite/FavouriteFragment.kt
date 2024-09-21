package com.example.weatherapp.Favourite

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.ForecastModel.City
import com.example.weatherapp.Map.MapActivity
import com.example.weatherapp.R
import com.example.weatherapp.ForecastModel.Clouds
import com.example.weatherapp.ForecastModel.Coord
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.ForecastModel.Main
import com.example.weatherapp.ForecastModel.Wind
import com.example.weatherapp.OneCall.Model.Weather
import com.example.weatherapp.databinding.FragmentFavouriteBinding

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavouriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        adapter = FavouriteAdapter(requireContext()) { foreCastData ->
            // Handle delete action here
            adapter.removeFavorite(foreCastData)
        }
        binding.favRV.adapter = adapter
        binding.favRV.layoutManager = LinearLayoutManager(requireContext())

        // Set up FloatingActionButton click listener
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MAP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAP && resultCode == AppCompatActivity.RESULT_OK) {
            data?.let {
                val cityName = it.getStringExtra("cityName") ?: return
                val countryName = it.getStringExtra("countryName") ?: return // Get the country name



                // Create CurrentWeatherResponse instance
                val currentWeatherResponse = CurrentWeatherResponse(
                    id = 0, // Placeholder for ID
                    city = City(
                        name = cityName,
                        country = countryName,
                        coord = Coord(lat = 0.0, lon = 0.0), // Add placeholders for coord
                        population = 0, // Placeholder for population
                        sunrise = 0, // Placeholder for sunrise
                        sunset = 0, // Placeholder for sunset
                        timezone = 0 , id =0 // Placeholder for timezone
                    ),
                    longitude = 0.0,
                    latitude = 0.0,
                    cnt = 1,
                    isFav = 1,
                    isALert = 0,
                    cod = "200",
                    list = mutableListOf(),
                    message = 0
                )

                // Add the new favorite to the adapter
                adapter.addFavorite(currentWeatherResponse) // Assuming adapter handles CurrentWeatherResponse


            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_MAP = 1
    }
}
