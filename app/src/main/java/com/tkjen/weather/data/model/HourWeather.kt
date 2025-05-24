package com.tkjen.weather.data.model

data class HourWeather(
    val time: String,
    val temp_c: Double,
    val condition: Condition
)
