package com.tkjen.weather.data.mapper

import com.tkjen.weather.data.local.entity.CachedWeatherEntity
import com.tkjen.weather.data.model.Condition
import com.tkjen.weather.data.model.Current
import com.tkjen.weather.data.model.Location
import com.tkjen.weather.data.model.WeatherResponse

object WeatherMapper {

    // Hàm chuyển đổi từ API Response sang Room Entity
    fun mapResponseToEntity(response: WeatherResponse): CachedWeatherEntity {
        return CachedWeatherEntity(
            locationName = response.location.name,
            temperature = response.current.temp_c,
            weatherDescription = response.current.condition.text,
            weatherIconUrl = response.current.condition.icon,
            uv = response.current.uv,
            humidity = response.current.humidity,
            timestamp = System.currentTimeMillis()
        )
    }
    // Hàm chuyển đổi từ Room Entity sang Model sử dụng (WeatherResponse hoặc model riêng)
    fun mapEntityToResponse(entity: CachedWeatherEntity): WeatherResponse {
        return WeatherResponse(
            location = Location(
                name = entity.locationName,
                region = "",
                country = "",
                lat = 0.0,
                lon = 0.0,
                tz_id = "",
                localtime = ""
            ),
            current = Current(
                last_updated = "",
                temp_c = entity.temperature,
                is_day = 0,
                condition = Condition(
                    text = entity.weatherDescription,
                    icon = entity.weatherIconUrl,
                    code = 0
                ),
                wind_kph = 0.0,
                wind_mph = 0.0,
                wind_degree = 0,
                wind_dir = "",
                pressure_mb = 0.0,
                humidity = entity.humidity,
                cloud = 0,
                feelslike_c = 0.0,
                vis_km = 0.0,
                uv = entity.uv,
                air_quality = null,
                precip_mm = 0.0
            ),
            forecast = null
        )
    }
}