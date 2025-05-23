package com.tkjen.weather.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkjen.weather.data.api.WeatherRepository
import com.tkjen.weather.data.model.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    fun loadWeather(location: String) {  // <-- dùng forecast API luôn
        viewModelScope.launch {
            try {
                val result = repository.getForecastWeather(location)
                _weather.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

