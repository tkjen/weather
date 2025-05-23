package com.tkjen.weather.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tkjen.weather.R
import com.tkjen.weather.data.api.DayForecast
import com.tkjen.weather.data.api.HourlyWeather
import com.tkjen.weather.databinding.ActivityWeatherBinding

class WeatherActivity : AppCompatActivity(R.layout.activity_weather) {

    private lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sampleData = listOf(
            HourlyWeather("Now", R.drawable.ic_heavy_rain, "21°"),
            HourlyWeather("1AM", R.drawable.ic_thunderstorm, "20°"),
            HourlyWeather("2AM", R.drawable.ic_showers_with_sun, "19°"),
            HourlyWeather("3AM", R.drawable.ic_light_rain, "18°"),
            HourlyWeather("4AM", R.drawable.ic_partly_cloudy_night, "17°"),
            HourlyWeather("5AM", R.drawable.ic_thunderstorm, "16°"),
            HourlyWeather("6AM", R.drawable.ic_showers_with_sun, "15°"),
        )
        val forecastList = listOf(
            DayForecast("Today", R.drawable.ic_sun, 0,15, 29, 21),
            DayForecast("Mon", R.drawable.ic_heavy_rain,10, 18, 27, 23),
            DayForecast("Tue", R.drawable.ic_light_rain, 0,20, 25, 24),
            DayForecast("Wed", R.drawable.ic_heavy_rain,40, 19, 28, 22),
        )

        val adapterHourlyWeather = HourlyWeatherAdapter(sampleData)
        binding.recyclerHourly.adapter = adapterHourlyWeather
        binding.recyclerHourly.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapterForecast = ForecastAdapter(forecastList)
        binding.recyclerDailyForecast.adapter = adapterForecast
        binding.recyclerDailyForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}