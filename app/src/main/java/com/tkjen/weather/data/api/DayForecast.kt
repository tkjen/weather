package com.tkjen.weather.data.api

data class DayForecast(
    val dayName: String,
    val iconResId: Int,
    val percentage:Int,
    val lowTemp: Int,
    val highTemp: Int,
    val currentTemp: Int
)
