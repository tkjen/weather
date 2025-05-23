package com.tkjen.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tkjen.weather.BuildConfig
import com.tkjen.weather.R
import com.tkjen.weather.data.local.DayForecast
import com.tkjen.weather.databinding.ActivityWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(R.layout.activity_weather) {

    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestLocationPermissions()

        viewModel.weather.observe(this) { response ->
            val currentTemp = response.current.temp_c
            val location = response.location.name

            binding.tvLocation.text = "$location"
            binding.tvTemperatureValue.text = "$currentTemp\u00B0"
            binding.tvWeather.text = response.current.condition.text
            binding.locationTemperature.text = " $currentTemp"

            // Forecast
            val todayAstro = response.forecast?.forecastday?.firstOrNull()?.astro
            val sunrise = todayAstro?.sunrise ?: "--:--"
            val uv = response.forecast
                ?.forecastday
                ?.firstOrNull()
                ?.day
                ?.uv

            binding.uvValue.text = "$uv"
            binding.sunriseTime.text = "$sunrise"
            val todayForecast = response.forecast?.forecastday?.firstOrNull()
            todayForecast?.let {
                val highTemp = it.day.maxtemp_c
                val lowTemp = it.day.mintemp_c
                binding.tvHighLowTemp.text = "H:${highTemp.toInt()}° L:${lowTemp.toInt()}°"

            }
        }
        viewModel.loadWeather("Chile") // Thay thế bằng vị trí mặc định nếu không có quyền
        Log.d("API_KEY", "WEATHER_API_KEY=${BuildConfig.WEATHER_API_KEY}")


        viewModel.hourlyList.observe(this) { list ->
            val adapterHourlyList = HourlyWeatherAdapter(list)
            binding.recyclerHourly.adapter = adapterHourlyList
        }
        viewModel.dailyForecast.observe(this){ list ->
            val adapterDailyForeCast = ForecastAdapter(list)
            binding.recyclerDailyForecast.adapter = adapterDailyForeCast

        }


    }
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            when {
                fineLocationGranted -> {
                    Log.d("WeatherActivity", "ACCESS_FINE_LOCATION permission granted")
                    fetchLastLocationAndLoadWeather()
                }
                coarseLocationGranted -> {
                    Log.d("WeatherActivity", "ACCESS_COARSE_LOCATION permission granted")
                    fetchLastLocationAndLoadWeather()
                }
                else -> {
                    Log.d("WeatherActivity", "Location permission denied")
                    showPermissionDeniedDialog()
                    // Cân nhắc: Tải dữ liệu mặc định nếu người dùng từ chối hoàn toàn
                    // viewModel.getCurrentWeather("Hanoi")
                }
            }
        }

    private fun checkAndRequestLocationPermissions() {
        when {
            // Kiểm tra xem quyền đã được cấp chưa
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Quyền đã được cấp
                Log.d("WeatherActivity", "Location permission already granted")
                fetchLastLocationAndLoadWeather()
            }
            // Kiểm tra xem có nên hiển thị giải thích cho người dùng không
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Log.d("WeatherActivity", "Showing permission rationale")
                showPermissionRationaleDialog()
            }
            // Yêu cầu quyền
            else -> {
                Log.d("WeatherActivity", "Requesting location permission")
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    @SuppressLint("MissingPermission") // Đã kiểm tra quyền ở checkAndRequestLocationPermissions
    private fun fetchLastLocationAndLoadWeather() {
        // Kiểm tra lại quyền một lần nữa để đảm bảo (dù không bắt buộc nếu logic trên đúng)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("WeatherActivity", "Attempted to fetch location without permission (should not happen).")
            // Có thể yêu cầu lại quyền hoặc xử lý lỗi
            checkAndRequestLocationPermissions() // Yêu cầu lại
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLon = "${location.latitude},${location.longitude}"
                    Log.d("WeatherActivity", "Fetched location: $latLon")
                    // Gọi ViewModel để tải thời tiết với vị trí này
                    // viewModel.getCurrentWeather(latLon)
                    Toast.makeText(this, "Location: $latLon", Toast.LENGTH_SHORT).show() // Ví dụ hiển thị
                } else {
                    Log.w("WeatherActivity", "Last location is null.")
                    Toast.makeText(this, "Could not retrieve current location.", Toast.LENGTH_LONG).show()
                    // Cân nhắc: Tải dữ liệu mặc định
                    // viewModel.getCurrentWeather("Hanoi")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherActivity", "Failed to get location", e)
                Toast.makeText(this, "Failed to get location.", Toast.LENGTH_LONG).show()
                // Cân nhắc: Tải dữ liệu mặc định
                // viewModel.getCurrentWeather("Hanoi")
            }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the Location permission to show you the weather for your current location. Please grant the permission.")
            .setPositiveButton("OK") { _, _ ->
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Người dùng hủy dialog giải thích, có thể tải dữ liệu mặc định
                // viewModel.getCurrentWeather("Hanoi")
                Toast.makeText(this, "Permission rationale cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Permission Denied")
            .setMessage("Location permission was denied. To use this feature, please enable it in the app settings or allow location access to see local weather.")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Mở cài đặt của ứng dụng
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Người dùng hủy dialog từ chối, có thể tải dữ liệu mặc định
                // viewModel.getCurrentWeather("Hanoi")
                Toast.makeText(this, "Permission denied dialog cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}