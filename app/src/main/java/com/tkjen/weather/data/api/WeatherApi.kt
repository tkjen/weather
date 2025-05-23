package com.tkjen.weather.data.api

import com.tkjen.weather.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String
    ): WeatherResponse

    @GET("forecast.json")
    suspend fun getForecastWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 1
    ): WeatherResponse

}
