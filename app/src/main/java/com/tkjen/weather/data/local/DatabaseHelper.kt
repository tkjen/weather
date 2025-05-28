package com.tkjen.weather.data.local

import com.tkjen.weather.data.local.entity.CachedWeatherEntity

interface DatabaseHelper {
    suspend fun insertWeather(weather: CachedWeatherEntity)
    suspend fun getLatestWeather(): CachedWeatherEntity?
    // Bạn có thể thêm các phương thức khác cần thiết cho việc tương tác với DB
}