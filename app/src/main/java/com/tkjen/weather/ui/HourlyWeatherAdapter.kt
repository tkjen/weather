package com.tkjen.weather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tkjen.weather.data.api.HourlyWeather
import com.tkjen.weather.databinding.ItemHourBinding

class HourlyWeatherAdapter(
    private val items: List<HourlyWeather>
) : RecyclerView.Adapter<HourlyWeatherAdapter.HourlyViewHolder>() {

    inner class HourlyViewHolder(private val binding: ItemHourBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HourlyWeather) {
            binding.tvHour.text = item.hour
            binding.imgWeather.setImageResource(item.weatherIconRes)
            binding.tvTemp.text = item.temperature

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}