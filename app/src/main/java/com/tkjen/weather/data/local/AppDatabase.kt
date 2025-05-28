package com.tkjen.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tkjen.weather.data.local.dao.WeatherDao
import com.tkjen.weather.data.local.entity.CachedWeatherEntity

@Database(entities = [CachedWeatherEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}