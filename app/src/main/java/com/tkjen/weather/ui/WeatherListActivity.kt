package com.tkjen.weather.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.tkjen.weather.R
import com.tkjen.weather.databinding.ActivityWeatherListBinding

class WeatherListActivity:AppCompatActivity(R.layout.activity_weather_list) {

    private lateinit var binding: ActivityWeatherListBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = ActivityWeatherListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}