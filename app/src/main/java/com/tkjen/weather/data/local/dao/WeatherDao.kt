package com.tkjen.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tkjen.weather.data.local.entity.CachedWeatherEntity

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: CachedWeatherEntity)

    @Query("SELECT * FROM cached_weather LIMIT 1")
    suspend fun getLatestWeather(): CachedWeatherEntity?

    @Query("DELETE FROM cached_weather")
    suspend fun deleteAllWeather()

    // Bạn có thể thêm các phương thức truy vấn khác nếu cần (ví dụ: theo locationName)
} 