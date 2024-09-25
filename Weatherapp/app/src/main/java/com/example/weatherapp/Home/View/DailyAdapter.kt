package com.example.weatherapp.Home.View

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.databinding.ItemWeeklistBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*



class DailyAdapter : ListAdapter<ForeCastData, DailyAdapter.DailyViewHolder>(
    DailyWeatherDiffCallback()
) {

    inner class DailyViewHolder(val binding: ItemWeeklistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foreCastData: ForeCastData) {
            // Set data to views
            val formattedDate = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
                .format(Date(foreCastData.dt * 1000L))
            binding.dayName.text = formattedDate // Format the timestamp to a readable date

            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(Date(foreCastData.dt * 1000L))
            binding.tvTime.text = formattedTime // Set the formatted time

            binding.tvTemp.text = "${foreCastData.main.temp.toInt()}°C" // Convert to string

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

class DailyWeatherDiffCallback : DiffUtil.ItemCallback<ForeCastData>() {
    override fun areItemsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem == newItem
    }
}
