package com.tkjen.weather.data.model

data class Forecastday(
    val date: String,
    val date_epoch: Long,
    val day: Day,
    val hour: List<HourWeather>
)
