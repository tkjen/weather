package com.tkjen.weather.ui.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.tkjen.weather.data.model.DayForecast
import com.tkjen.weather.databinding.ItemDayForecastBinding

class ForecastAdapter(
    private var items: List<DayForecast>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(private val binding: ItemDayForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DayForecast, isLastItem: Boolean) {
            binding.apply {
                // Bind basic information
                tvDayName.text = item.dayName
                ivWeatherIcon.setImageResource(item.iconResId)
                tvLowTemp.text = "${item.lowTemp}°"
                tvHighTemp.text = "${item.highTemp}°"

                // Handle precipitation percentage visibility
                tvPrecipitationPercentage.visibility = if (item.percentage <= 0) View.GONE else View.VISIBLE
                if (item.percentage > 0) {
                    tvPrecipitationPercentage.text = "${item.percentage}%"
                }

                // Temperature bar logic
                flTempBarContainer.post {
                    try {
                        val maxBarWidth = viewTempBarBaseBackground.width
                        if (maxBarWidth <= 0) {
                            Log.w("ForecastAdapter", "maxBarWidth is 0 or less for item: ${item.dayName}")
                            return@post
                        }

                        val range = item.highTemp - item.lowTemp
                        val gradientBar = viewTempBarGradient
                        val dot = viewTempIndicatorDot

                        val gradientParams = gradientBar.layoutParams
                        val dotParams = dot.layoutParams as FrameLayout.LayoutParams

                        if (range > 0) {
                            val currentPercent = ((item.currentTemp - item.lowTemp).toFloat() / range).coerceIn(0f, 1f)
                            val gradientWidth = (currentPercent * maxBarWidth).toInt()

                            gradientParams.width = gradientWidth.coerceAtLeast(0)

                            var dotMarginStart = gradientWidth - (dot.width / 2)
                            dotMarginStart = dotMarginStart.coerceIn(0, maxBarWidth - dot.width)
                            dotParams.marginStart = dotMarginStart
                        } else {
                            if (item.currentTemp >= item.lowTemp) {
                                gradientParams.width = maxBarWidth
                                dotParams.marginStart = (maxBarWidth - (dot.width / 2)).coerceIn(0, maxBarWidth - dot.width)
                            } else {
                                gradientParams.width = 0
                                dotParams.marginStart = 0
                            }
                        }
                        gradientBar.layoutParams = gradientParams
                        dot.layoutParams = dotParams

                    } catch (e: Exception) {
                        Log.e("ForecastAdapter", "Error in temp bar post for item ${item.dayName}: ${e.message}")
                    }
                }
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
        holder.bind(items[position], position == items.size - 1)
    }

    override fun getItemCount(): Int {
        Log.d("ForecastAdapter", "getItemCount: ${items.size}")
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<DayForecast>) {
        Log.d("ForecastAdapter", "updateData called with ${newItems.size} items. Current items: ${items.size}")
        this.items = newItems
        notifyDataSetChanged()
    }
} 