package com.tkjen.weather.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkjen.weather.R
import com.tkjen.weather.data.api.WeatherRepository
import com.tkjen.weather.data.local.DayForecast
import com.tkjen.weather.data.local.HourlyWeather
import com.tkjen.weather.data.model.HourWeather
import com.tkjen.weather.data.model.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    private val _hourlyList = MutableLiveData<List<HourlyWeather>>()
    val hourlyList: LiveData<List<HourlyWeather>> = _hourlyList

    private val _dailyForecast = MutableLiveData<List<DayForecast>>()
    val dailyForecast: LiveData<List<DayForecast>> = _dailyForecast

    fun loadWeather(location: String) {
        viewModelScope.launch {
            try {
                val result = repository.getForecastWeather(location,10)
                _weather.postValue(result)

                // Xử lý HourlyWeather
                val hours: List<HourWeather> = result.forecast
                    ?.forecastday
                    ?.firstOrNull()
                    ?.hour
                    ?: emptyList()



                val hourly = hours.map { hour ->
                    val formattedHour = hour.time.takeLast(5)
                    val temp = "${hour.temp_c.toInt()}°"
                    val iconUrl = "https:${hour.condition.icon}"
                    HourlyWeather(
                        hour = formattedHour,
                        temperature = temp,
                        weatherIconUrl = iconUrl
                    )
                }
                _hourlyList.postValue(hourly)

                // Xử lý DayForecast
                val currentTemp = result.current.temp_c.toInt()

                val daily = result.forecast
                    ?.forecastday
                    ?.map { forecastDay ->

                        val dayName = if (forecastDay == result.forecast.forecastday.first()) {
                            "Today"
                        } else {
                            val sdfInput = java.text.SimpleDateFormat(
                                "yyyy-MM-dd",
                                java.util.Locale.getDefault()
                            )
                            val sdfOutput = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                            val date = sdfInput.parse(forecastDay.date)
                            sdfOutput.format(date ?: java.util.Date())
                        }

                        val iconResId = mapIconToRes(forecastDay.day.condition.icon)

                        DayForecast(
                            dayName = dayName,
                            iconResId = iconResId,
                            percentage = forecastDay.day.daily_chance_of_rain ?: 0,
                            lowTemp = forecastDay.day.mintemp_c?.toInt() ?: 0,
                            highTemp = forecastDay.day.maxtemp_c?.toInt() ?: 0,
                            currentTemp = if (dayName == "Today") currentTemp else (forecastDay.day.avgtemp_c?.toInt() ?: 0)
                        )

                    } ?: emptyList()
                _dailyForecast.postValue(daily)


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun mapIconToRes(iconUrl: String): Int {
        val iconCode = iconUrl.substringAfterLast("/").substringBefore(".")
        Log.d("IconMapper", "iconCode: $iconCode")

        return when (iconCode) {
            "113" -> R.drawable.ic_sun                  // Clear sky, nắng
            "116" -> R.drawable.ic_partly_cloudy_day    // Partly cloudy (ban ngày)
            "119", "143", "122" -> R.drawable.ic_partly_cloudy_day  // Mây nhẹ, sương mù, nhiều mây
            "176", "263", "296" -> R.drawable.ic_light_rain  // Patchy rain, light rain
            "302", "308", "356" -> R.drawable.ic_heavy_rain  // Moderate/Heavy rain
            "200", "386", "389" -> R.drawable.ic_thunderstorm  // Thunderstorm with rain
            "113_night", "116_night" -> R.drawable.ic_partly_cloudy_night  // Ban đêm nếu API phân biệt
            else -> {
                Log.w("IconMapper", "Unmapped iconCode: $iconCode")
                R.drawable.ic_light_rain  // Fallback mặc định
            }
        }
    }

    fun getUIlevel(uv:Double): String {
        return when
        {
            uv <= 2 -> "Low"
            uv <= 5 -> "Moderate"
            uv <= 7 -> "High"
            uv <= 10 -> "Very High"
            else -> "Extreme"
        }

    }
    fun calculateDayDuration(sunrise: String?, sunset: String?): String {
        if (sunrise == null || sunset == null) return "--:--"

        return try {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sunriseTime = formatter.parse(sunrise)
            val sunsetTime = formatter.parse(sunset)

            val durationMillis = sunsetTime.time - sunriseTime.time
            val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

            String.format("%02d:%02d", hours, minutes)
        } catch (e: Exception) {
            "--:--"
        }
    }





}

