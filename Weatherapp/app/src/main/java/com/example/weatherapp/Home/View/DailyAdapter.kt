package com.example.weatherapp.Home.View

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.SharedKey
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.databinding.ItemWeeklistBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*
import android.content.Context



class DailyAdapter(private val sharedPrefManager: SharedPreferenceManager) : ListAdapter<ForeCastData, DailyAdapter.DailyViewHolder>(
    DailyWeatherDiffCallback()
) {

//    private lateinit var sharedPrefManager: SharedPreferenceManager
//    fun setSharedPrefManager(manager: SharedPreferenceManager) {
//        this.sharedPrefManager = manager
//    }

    inner class DailyViewHolder(val binding: ItemWeeklistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foreCastData: ForeCastData) {
            // Set data to views
            val formattedDate = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
                .format(Date(foreCastData.dt * 1000L))
            binding.dayName.text = formattedDate // Format the timestamp to a readable date

            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(foreCastData.dt * 1000L))
            binding.tvTime.text = formattedTime // Set the formatted time

        //    binding.tvTemp.text = "${foreCastData.main.temp.toInt()}°C" // Convert to string

            val tempUnit = sharedPrefManager.getTempUnit("temperature_unit", "Celsius")
            // Directly set the temperature based on the selected unit
            val temperatureText = when (tempUnit.lowercase(Locale.ROOT)) {
                "fahrenheit" -> "${convertToFahrenheit(foreCastData.main.temp)}°F"
                "kelvin" -> "${convertToKelvin(foreCastData.main.temp)} K"
                else -> "${foreCastData.main.temp.toInt()}°C" // Default to Celsius
            }
            binding.tvTemp.text = temperatureText

            // Load the weather icon into imageViewWS
            val iconUrl = "https://openweathermap.org/img/w/${foreCastData.weather[0].icon}.png"
            Picasso.get().load(iconUrl).into(binding.imageViewWS) // Use Picasso for image loading
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemWeeklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
// Utility function for temperature conversion
private fun convertToFahrenheit(temp: Double): Int {
    return ((temp * 9/5) + 32).toInt() // Convert from Kelvin to Fahrenheit
}

private fun convertToKelvin(temp: Double): Int {
    return (temp + 273.15).toInt() // Assuming the API already returns Kelvin
}
class DailyWeatherDiffCallback : DiffUtil.ItemCallback<ForeCastData>() {
    override fun areItemsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem == newItem
    }
}
