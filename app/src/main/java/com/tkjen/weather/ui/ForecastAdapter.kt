package com.tkjen.weather.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import com.tkjen.weather.data.api.DayForecast
import com.tkjen.weather.databinding.ItemDayForecastBinding


class ForecastAdapter(
    private val items: List<DayForecast>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(private val binding: ItemDayForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DayForecast) {
            binding.tvDayName.text = item.dayName
            binding.ivWeatherIcon.setImageResource(item.iconResId)
            binding.tvLowTemp.text = "${item.lowTemp}°"
            binding.tvHighTemp.text = "${item.highTemp}°"

            binding.flTempBarContainer.post {
                val maxBarWidth = binding.flTempBarContainer.width
                val range = item.highTemp - item.lowTemp
                if (range <= 0) return@post

                val currentPercent = (item.currentTemp - item.lowTemp).toFloat() / range
                val gradientWidth = (currentPercent * maxBarWidth).toInt()

                // Gradient bar
                binding.viewTempBarGradient.layoutParams.width = gradientWidth
                binding.viewTempBarGradient.requestLayout()

                // Dot position
                val dotParams = binding.viewTempIndicatorDot.layoutParams as FrameLayout.LayoutParams
                dotParams.marginStart = gradientWidth - binding.viewTempIndicatorDot.width / 2
                binding.viewTempIndicatorDot.layoutParams = dotParams


            }

            if (item.percentage <= 0) {
                binding.tvPrecipitationPercentage.visibility = View.GONE
            } else {
                binding.tvPrecipitationPercentage.visibility = View.VISIBLE
                binding.tvPrecipitationPercentage.text = "${item.percentage}%"
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemDayForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}