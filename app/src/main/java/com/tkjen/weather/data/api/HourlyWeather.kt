package com.tkjen.weather.data.api

data class HourlyWeather(
    val hour: String,
    val weatherIconRes: Int,
    val temperature: String
)

