package com.example.weatherapp.Favourite

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.ForecastModel.CurrentWeatherResponse
import com.example.weatherapp.ForecastModel.ForeCastData
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemFavouriteBinding
import com.squareup.picasso.Picasso

class FavouriteAdapter(
    private val context: Context,
    private val onDelete: (CurrentWeatherResponse) -> Unit // Listener for delete action
) : ListAdapter<CurrentWeatherResponse, FavouriteAdapter.FavouriteViewHolder>(DiffCallback()) {

    inner class FavouriteViewHolder(private val binding: ItemFavouriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weatherResponse: CurrentWeatherResponse) {
            binding.tvCity.text = weatherResponse.city.name
            binding.tvCountry.text = weatherResponse.city.country

            itemView.setOnLongClickListener {
                showDeletePopup(weatherResponse)
                true
            }
        }

        private fun showDeletePopup(weatherResponse: CurrentWeatherResponse) {
            // Inflate and show popup as before
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.refresh_favourite_item_action_layout, null)
            val myPopupWindow = PopupWindow(view, 400, 210, true)

            val start: View = view.findViewById(R.id.deleter)
            start.setOnClickListener {
                Toast.makeText(context, "Location deleted", Toast.LENGTH_SHORT).show()
                onDelete(weatherResponse) // Use the updated type
                myPopupWindow.dismiss()
            }

            myPopupWindow.showAsDropDown(itemView, 700, -100)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val binding = ItemFavouriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavouriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentWeatherResponse>() {
        override fun areItemsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse): Boolean {
            return oldItem.id == newItem.id // Assuming id is unique for each weather response
        }

        override fun areContentsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse): Boolean {
            return oldItem == newItem
        }
    }

    fun addFavorite(weatherResponse: CurrentWeatherResponse) {
        val currentList = currentList.toMutableList()
        currentList.add(weatherResponse)
        submitList(currentList)

    }


    fun removeFavorite(weatherResponse: CurrentWeatherResponse) {
        val currentList = currentList.toMutableList()
        currentList.remove(weatherResponse)
        submitList(currentList)
    }
}
