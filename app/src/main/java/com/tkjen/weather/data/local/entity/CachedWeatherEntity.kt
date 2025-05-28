package com.tkjen.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationName: String,
    val temperature: Double,
    val weatherDescription: String,
    val weatherIconUrl: String,
    val uv :Double,
    val humidity: Int,
    val timestamp: Long // Thời gian lưu trữ để kiểm tra dữ liệu cũ
    // Thêm các trường dữ liệu khác bạn muốn lưu
) 