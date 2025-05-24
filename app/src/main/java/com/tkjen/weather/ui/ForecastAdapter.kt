package com.tkjen.weather.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.tkjen.weather.data.local.DayForecast // Hoặc DayForecastUi nếu bạn dùng
import com.tkjen.weather.databinding.ItemDayForecastBinding

class ForecastAdapter(
    private var items: List<DayForecast>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(private val binding: ItemDayForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DayForecast, isLastItem: Boolean) { // Thêm isLastItem nếu bạn có divider
            binding.tvDayName.text = item.dayName
            binding.ivWeatherIcon.setImageResource(item.iconResId) // SỬ DỤNG ID TỪ XML
            binding.tvLowTemp.text = "${item.lowTemp}°"           // SỬ DỤNG ID TỪ XML
            binding.tvHighTemp.text = "${item.highTemp}°"          // SỬ DỤNG ID TỪ XML

            // Xử lý hiển thị text %
            if (item.percentage <= 0) {
                binding.tvPrecipitationPercentage.visibility = View.GONE
            } else {
                binding.tvPrecipitationPercentage.visibility = View.VISIBLE
                binding.tvPrecipitationPercentage.text = "${item.percentage}%"
            }

            // Logic thanh nhiệt kế
            binding.flTempBarContainer.post { // SỬ DỤNG ID TỪ XML
                try {
                    val maxBarWidth = binding.viewTempBarBaseBackground.width // SỬ DỤNG ID TỪ XML
                    if (maxBarWidth <= 0) {
                        Log.w("ForecastAdapter", "maxBarWidth is 0 or less for item: ${item.dayName}")
                        return@post
                    }

                    val range = item.highTemp - item.lowTemp

                    val gradientBar = binding.viewTempBarGradient // SỬ DỤNG ID TỪ XML
                    val dot = binding.viewTempIndicatorDot       // SỬ DỤNG ID TỪ XML

                    val gradientParams = gradientBar.layoutParams
                    val dotParams = dot.layoutParams as FrameLayout.LayoutParams

                    if (range > 0) {
                        val currentPercent = ((item.currentTemp - item.lowTemp).toFloat() / range).coerceIn(0f, 1f)
                        val gradientWidth = (currentPercent * maxBarWidth).toInt()

                        gradientParams.width = gradientWidth.coerceAtLeast(0) // Đảm bảo không âm

                        var dotMarginStart = gradientWidth - (dot.width / 2)
                        // Đảm bảo chấm tròn không đi ra ngoài bên trái (nếu gradientWidth rất nhỏ)
                        // và không đi ra ngoài bên phải
                        dotMarginStart = dotMarginStart.coerceIn(0, maxBarWidth - dot.width)
                        dotParams.marginStart = dotMarginStart

                    } else { // Trường hợp minTemp >= highTemp (hoặc bằng nhau)
                        if (item.currentTemp >= item.lowTemp) {
                            gradientParams.width = maxBarWidth
                            dotParams.marginStart = (maxBarWidth - (dot.width / 2)).coerceIn(0, maxBarWidth - dot.width)
                        } else {
                            gradientParams.width = 0
                            dotParams.marginStart = 0 // Chấm tròn ở đầu nếu nhiệt độ thấp hơn cả min/max
                        }
                    }
                    gradientBar.layoutParams = gradientParams
                    dot.layoutParams = dotParams

                } catch (e: Exception) {
                    Log.e("ForecastAdapter", "Error in temp bar post for item ${item.dayName}: ${e.message}")
                }
            }

            // Xử lý đường kẻ phân cách (NẾU BẠN THÊM VÀO XML)
            // Giả sử bạn thêm một View với id là "divider_line" vào cuối item_day_forecast.xml
            // binding.dividerLine.visibility = if (isLastItem) View.GONE else View.VISIBLE
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
        notifyDataSetChanged() // Quan trọng!
    }
}