package com.tkjen.weather.data.api

import com.tkjen.weather.data.model.WeatherResponse
import com.tkjen.weather.utils.NetworkHelper
import javax.inject.Inject
import javax.inject.Named

class WeatherRepository @Inject constructor(
    private val api: WeatherApiService,
    @Named("weather_api_key") private val apiKey: String,
    private val networkHelper: NetworkHelper
) {
    suspend fun getCurrentWeather(location: String): WeatherResponse {
        return api.getCurrentWeather(apiKey, location)
    }
    suspend fun getForecastWeather(location: String): WeatherResponse {
        return api.getForecastWeather(apiKey, location,1)
    }
    suspend fun getForecastWeather(location: String, days: Int = 10): WeatherResponse {
        return api.getForecastWeather(apiKey, location, days)
    }


}
