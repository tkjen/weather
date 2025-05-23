package com.tkjen.weather.data.model

data class Current(
    val last_updated: String,
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_kph: Double,
    val pressure_mb: Double,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Double,
    val vis_km: Double,
    val uv: Double,
    val air_quality: AirQuality?
)
