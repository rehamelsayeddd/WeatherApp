package com.example.weatherapp.Home.View

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.SharedPreferenceManager
import com.example.weatherapp.databinding.ItemTodayDetailsBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale


class TodayAdapter(private val sharedPrefManager: SharedPreferenceManager) : ListAdapter<ForeCastData, TodayAdapter.TodayViewHolder>(
    HourlyWeatherDiffCallback()
) {

   // private lateinit var sharedPrefManager: SharedPreferenceManager

    inner class TodayViewHolder(private val binding: ItemTodayDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foreCastData: ForeCastData) {
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = dateFormat.format(java.util.Date(foreCastData.dt * 1000L))
            binding.tvTime.text = formattedTime

            // Get temperature unit preference
            val tempUnit = sharedPrefManager.getTempUnit("temperature_unit", "celsius")

            // Convert temperature based on the unit
            val temperatureText = when (tempUnit.lowercase(Locale.ROOT)) {
                "fahrenheit" -> "${convertToFahrenheit(foreCastData.main.temp)}°F"
                "kelvin" -> "${convertToKelvin(foreCastData.main.temp)} K"
                else -> "${foreCastData.main.temp.toInt()}°C" // Default to Celsius
            }
            binding.tvTemp.text = temperatureText

            // Load the weather icon into imageView
            val iconUrl = "https://openweathermap.org/img/w/${foreCastData.weather[0].icon}.png"
            Picasso.get().load(iconUrl).into(binding.weatherImgView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayViewHolder {
        val binding = ItemTodayDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private fun convertToFahrenheit(temp: Double): Int {
    return ((temp * 9 / 5) + 32).toInt()
}

private fun convertToKelvin(temp: Double): Int {
    return (temp + 273.15).toInt()
}

class HourlyWeatherDiffCallback : DiffUtil.ItemCallback<ForeCastData>() {
    override fun areItemsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem == newItem
    }
}

//class TodayAdapter : ListAdapter<ForeCastData, TodayAdapter.TodayViewHolder>(
//    HourlyWeatherDiffCallback()
//) {
//
//    private lateinit var sharedPrefManager: SharedPreferenceManager
//
//    fun setSharedPrefManager(manager: SharedPreferenceManager) {
//        this.sharedPrefManager = manager
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayViewHolder {
//        val binding = ItemTodayDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return TodayViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: TodayViewHolder, position: Int) {
//        val hourlyWeather = getItem(position)
//        holder.bind(hourlyWeather)
//    }
//
//    class TodayViewHolder(private val binding: ItemTodayDetailsBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(foreCastData: ForeCastData) {
//            val dateFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
//            val formattedTime = dateFormat.format(java.util.Date(foreCastData.dt * 1000L))
//
//            binding.tvTime.text = formattedTime
//
//           // binding.tvTemp.text = "${foreCastData.main.temp}°C"
//            val tempUnit = sharedPrefManager.getTempUnit("temperature_unit", "Celsius")
//            // Directly set the temperature based on the selected unit
//            val temperatureText = when (tempUnit.lowercase(Locale.ROOT)) {
//                "fahrenheit" -> "${convertToFahrenheit(foreCastData.main.temp)}°F"
//                "kelvin" -> "${convertToKelvin(foreCastData.main.temp)} K"
//                else -> "${foreCastData.main.temp.toInt()}°C" // Default to Celsius
//            }
//            binding.tvTemp.text = temperatureText
//
//
//            val iconUrl = "https://openweathermap.org/img/w/${foreCastData.weather[0].icon}.png"
//            Picasso.get().load(iconUrl).into(binding.weatherImgView)
//        }
//    }
//}
//
//private fun convertToFahrenheit(temp: Double): Int {
//    return ((temp * 9/5) + 32).toInt() // Convert from Kelvin to Fahrenheit
//}
//
//private fun convertToKelvin(temp: Double): Int {
//    return (temp + 273.15).toInt() // Assuming the API already returns Kelvin
//}
//
//class HourlyWeatherDiffCallback : DiffUtil.ItemCallback<ForeCastData>() {
//    override fun areItemsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
//        // Assuming 'dateTime' is unique and can be used as an identifier
//        return oldItem.dt == newItem.dt
//    }
//
//    override fun areContentsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
//        return oldItem == newItem
//    }
//}
