package com.tkjen.weather.data.local

import com.tkjen.weather.data.local.dao.WeatherDao
import com.tkjen.weather.data.local.entity.CachedWeatherEntity
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DatabaseHelperImpl @Inject constructor (
    private val appDatabase: AppDatabase,
    private val weatherDao: WeatherDao // Inject WeatherDao
    ) : DatabaseHelper {

    override suspend fun insertWeather(weather: CachedWeatherEntity) {
        weatherDao.insertWeather(weather)
    }

    override suspend fun getLatestWeather(): CachedWeatherEntity? {
        return weatherDao.getLatestWeather()
    }

    // Bạn có thể thêm các hàm chuyển đổi từ WeatherResponse sang CachedWeatherEntity ở đây
    // hoặc xử lý chuyển đổi ở Repository layer.

}