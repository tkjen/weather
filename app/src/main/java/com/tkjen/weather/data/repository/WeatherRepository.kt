package com.tkjen.weather.data.repository

import com.tkjen.weather.data.api.WeatherApiService
import com.tkjen.weather.data.local.DatabaseHelper
import com.tkjen.weather.data.local.entity.CachedWeatherEntity
import com.tkjen.weather.data.model.Condition
import com.tkjen.weather.utils.NetworkHelper
import com.tkjen.weather.data.model.WeatherResponse
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.tkjen.weather.data.model.Location
import com.tkjen.weather.data.model.Current


@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val databaseHelper: DatabaseHelper,
    private val networkHelper: NetworkHelper,
    @Named("weather_api_key") private val apiKey: String
) {

    suspend fun getWeatherData(location: String): Result<WeatherResponse> {
        if (networkHelper.isNetworkConnected()) {
            try {
                // Lấy dữ liệu dự báo 10 ngày từ API (hoặc getCurrentWeather tùy logic chính của bạn)
                val response = apiService.getForecastWeather(apiKey, location, 10) // Sử dụng API key và phương thức đúng

                // Chuyển đổi và lưu vào cache
                val cachedEntity = convertResponseToEntity(response)
                databaseHelper.insertWeather(cachedEntity)

                return Result.Success(response)

            } catch (e: Exception) {
                // Xử lý lỗi API, thử lấy từ cache
                val cachedData = databaseHelper.getLatestWeather()
                return if (cachedData != null) {
                    Result.Success(convertEntityToResponse(cachedData))
                } else {
                    Result.Error("Failed to fetch data online and no offline data available: ${e.message}")
                }
            }
        } else {           // Không có mạng, lấy từ cache
            val cachedData = databaseHelper.getLatestWeather()
            return if (cachedData != null) {
                Result.Success(convertEntityToResponse(cachedData))
            } else {
                Result.Error("No network connection and no offline data available.")
            }
        }
    }

    // Bạn có thể thêm các phương thức khác nếu cần, ví dụ chỉ lấy current weather
    // suspend fun getCurrentWeather(location: String): Result<WeatherResponse> { ... }

    // Hàm chuyển đổi từ API Response sang Room Entity
    private fun convertResponseToEntity(response: WeatherResponse): CachedWeatherEntity {
        // TODO: Triển khai logic chuyển đổi đầy đủ
        return CachedWeatherEntity(
            locationName = response.location.name,
            temperature = response.current.temp_c,
            weatherDescription = response.current.condition.text,
            weatherIconUrl = response.current.condition.icon,
            uv = response.current.uv,
            humidity = response.current.humidity,
            timestamp = System.currentTimeMillis()
            // Thêm các trường khác nếu có
        )
    }

    // Hàm chuyển đổi từ Room Entity sang Model sử dụng (WeatherResponse hoặc model riêng)
    private fun convertEntityToResponse(entity: CachedWeatherEntity): WeatherResponse {
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
                humidity = 0,
                cloud = 0,
                feelslike_c = 0.0,
                vis_km = 0.0,
                uv = 0.0,
                air_quality = null,
                precip_mm = 0.0
            ),
            forecast = null
        )
    }


    // Sealed class để bọc kết quả (Success hoặc Error)
    sealed class Result<out T> {
        data class Success<out R>(val data: R) : Result<R>()
        data class Error(val message: String) : Result<Nothing>()
    }
} 