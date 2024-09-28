package com.example.weatherapp.Favourite.View

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Database.LocalData
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModel
import com.example.weatherapp.Favourite.ViewModel.FavouriteViewModelFactory
import com.example.weatherapp.Favourite.ViewModel.FavouriteState
import com.example.weatherapp.ForecastModel.City
import com.example.weatherapp.ForecastModel.Coord
import com.example.weatherapp.Map.View.MapActivity
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.Network.RemoteData
import com.example.weatherapp.R
import com.example.weatherapp.Repository.WeatherRepository
import com.example.weatherapp.databinding.FragmentFavouriteBinding
import kotlinx.coroutines.launch

class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavouriteAdapter
    private lateinit var viewModel: FavouriteViewModel
    private lateinit var repository: WeatherRepository
    private lateinit var viewModelFactory: FavouriteViewModelFactory
    private lateinit var remoteData: RemoteData
    private lateinit var localData: LocalData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remoteData = RemoteData()
        localData = LocalData(requireContext())
        repository = WeatherRepository.getInstance(remoteData, localData)

        // Create an instance of FavouriteViewModelFactory with your repository
        viewModelFactory = FavouriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(FavouriteViewModel::class.java)


        // Set up RecyclerView
        adapter = FavouriteAdapter(requireContext(),
            { forecastData -> // onDelete
                viewModel.removeFavourite(forecastData.longitude, forecastData.latitude)
                adapter.removeFavorite(forecastData)
            },
            { forecastData -> // onClick
                // Create the Bundle for the new fragment
                val bundle = Bundle().apply {
                    putString("cityName", forecastData.city.name)
                    putString("countryName", forecastData.city.country)
                    putDouble("latitude", forecastData.latitude)
                    putDouble("longitude", forecastData.longitude)
                }

                // Create a new instance of FavouriteDetailsFragment
                val detailsFragment = FavouriteDetailsFragment().apply {
                    arguments = bundle
                }

                // Replace the current fragment with FavouriteDetailsFragment
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null) // Add to back stack to allow navigation back
                    .commit()
            }
        )
        binding.favRV.adapter = adapter
        binding.favRV.layoutManager = LinearLayoutManager(requireContext())

        // Observe the favourites from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favouriteState.collect { state ->
                when (state) {
                    is FavouriteState.Loading -> {
                        binding.animationView.visibility = View.VISIBLE
                        binding.favRV.visibility = View.GONE
                    }
                    is FavouriteState.Success -> {
                        binding.animationView.visibility = View.GONE
                        binding.favRV.visibility = View.VISIBLE
                        adapter.submitList(state.favourites) // Use the updated list
                    }
                    is FavouriteState.Error -> {
                        binding.animationView.visibility = View.VISIBLE
                        binding.favRV.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Set up FloatingActionButton click listener
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            activityResultLauncher.launch(intent) // Use the launcher to start the MapActivity
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val cityName = data.getStringExtra("cityName") ?: return@registerForActivityResult
                val countryName = data.getStringExtra("countryName") ?: return@registerForActivityResult
                val latitude = data.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)

                // Construct the CurrentWeatherResponse with the city and country names
                val currentWeatherResponse = CurrentWeatherResponse(
                    id = 0, // Placeholder for ID
                    city = City(
                        name = cityName,
                        country = countryName,
                        coord = Coord(lat = latitude, lon = longitude),
                        population = 0,
                        sunrise = 0,
                        sunset = 0,
                        timezone = 0,
                        id = 0
                    ),
                    longitude = longitude,
                    latitude = latitude,
                    cnt = 1,
                    isFav = 1,
                    isALert = 0,
                    cod = "200",
                    list = mutableListOf(),
                    message = 0
                )

                // Add the new favorite to the ViewModel
                viewModel.addFavourite(currentWeatherResponse)
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
