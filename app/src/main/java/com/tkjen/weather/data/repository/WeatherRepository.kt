    package com.tkjen.weather.data.repository

    import com.tkjen.weather.data.api.WeatherApiService
    import com.tkjen.weather.data.local.DatabaseHelper
    import com.tkjen.weather.data.mapper.WeatherMapper
    import com.tkjen.weather.utils.NetworkHelper
    import com.tkjen.weather.data.model.WeatherResponse
    import com.tkjen.weather.utils.Result
    import javax.inject.Inject
    import javax.inject.Named
    import javax.inject.Singleton

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
                    val cachedEntity = WeatherMapper.mapResponseToEntity(response)
                    databaseHelper.insertWeather(cachedEntity)

                    return Result.Success(response)

                } catch (e: Exception) {
                    // Xử lý lỗi API, thử lấy từ cache
                    val cachedData = databaseHelper.getLatestWeather()
                    return if (cachedData != null) {
                        Result.Success(WeatherMapper.mapEntityToResponse(cachedData))
                    } else {
                        Result.Error("Failed to fetch data online and no offline data available: ${e.message}")
                    }
                }
            } else {           // Không có mạng, lấy từ cache
                val cachedData = databaseHelper.getLatestWeather()
                return if (cachedData != null) {
                    Result.Success(WeatherMapper.mapEntityToResponse(cachedData))
                } else {
                    Result.Error("No network connection and no offline data available.")
                }
            }
        }




    }