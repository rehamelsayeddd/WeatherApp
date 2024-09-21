package com.example.weatherapp.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.databinding.ItemTodayDetailsBinding
import com.squareup.picasso.Picasso



class TodayAdapter : ListAdapter<ForeCastData, TodayAdapter.TodayViewHolder>(
    HourlyWeatherDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayViewHolder {
        val binding = ItemTodayDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayViewHolder, position: Int) {
        val hourlyWeather = getItem(position)
        holder.bind(hourlyWeather)
    }

    class TodayViewHolder(private val binding: ItemTodayDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foreCastData: ForeCastData) {
            val dateFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val formattedTime = dateFormat.format(java.util.Date(foreCastData.dt * 1000L))

            binding.tvTime.text = formattedTime
            binding.tvTemp.text = "${foreCastData.main.temp}°C"
            val iconUrl = "https://openweathermap.org/img/w/${foreCastData.weather[0].icon}.png"
            Picasso.get().load(iconUrl).into(binding.weatherImgView)
        }
    }
}

class HourlyWeatherDiffCallback : DiffUtil.ItemCallback<ForeCastData>() {
    override fun areItemsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        // Assuming 'dateTime' is unique and can be used as an identifier
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: ForeCastData, newItem: ForeCastData): Boolean {
        return oldItem == newItem
    }
}
