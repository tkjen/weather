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
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tkjen.weather.BuildConfig
import com.tkjen.weather.R
import java.text.SimpleDateFormat
import java.util.*
import com.tkjen.weather.databinding.ActivityWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(R.layout.activity_weather), OnMapReadyCallback {

    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestLocationPermissions()

        viewModel.weather.observe(this) { response ->
            val currentTemp = response.current.temp_c
            val location = response.location.name
            val content = response.current.condition.text
            val forecastdayText = response.forecast?.forecastday?.firstOrNull()?.day?.condition?.text
            binding.tvLocation.text = "$location"
            binding.locationName.text = "$location"
            binding.tvTemperatureValue.text = "$currentTemp\u00B0"
            binding.tvWeather.text = "$content"
            binding.locationTemperature.text = " $currentTemp"



            // Forecast
            val todayAstro = response.forecast?.forecastday?.firstOrNull()?.astro
            val sunrise = todayAstro?.sunrise ?: "--:--"
            val sunset = todayAstro?.sunset ?: "--:--"

            val hours = response.forecast?.forecastday?.firstOrNull()?.hour ?: emptyList()

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentEpochMillis = System.currentTimeMillis()

            val currentHourData = hours.minByOrNull { hourItem ->
                val date = sdf.parse(hourItem.time)
                val epochMillis = date?.time ?: 0L
                kotlin.math.abs(epochMillis - currentEpochMillis)
            }
            currentHourData?.let {
                val dewPointC = it.dewpoint_c
                val dewPointF = it.dewpoint_f
                Log.d("Weather", "Current dew point:: $dewPointC °C / $dewPointF °F lúc ${it.time}")
                binding.contenthumidity.text = "Current dew point: $dewPointC °C "
            }



            val uv = response.current.uv
            val fealsLike = response.current.feelslike_c
            // tong luong mua trong ngay
            val rainfall = response.forecast?.forecastday?.firstOrNull()?.day?.totalprecip_mm ?: 0.0

            val currentPrecip = response.current.precip_mm
            val forecastPrecip = response.forecast?.forecastday?.get(0)?.day?.totalprecip_mm

            binding.currentRainfall.text = "$currentPrecip mm"
            binding.expectedRainfall.text = "$forecastPrecip mm expected in next 24h."
            val windSpeed = response.current.wind_kph
            val windDegree = response.current.wind_degree.toFloat()
            val humidity = response.current.humidity

            val uiLevel = viewModel.getUIlevel(uv)

            val progressPercent = ((uv / 11f) * 100).toInt().coerceIn(0, 100)
            binding.sunsetTime.text = "$sunset"
            Log.d("WeatherActivity", "Sunrise: $sunrise, Sunset: $sunset")
            binding.uvProgress.progress = progressPercent
            binding.uvLevel.text = uiLevel
            binding.humidityValue.text = "$humidity%"

          //  binding.currentRainfall.text = "$rainfall mm"
            binding.windSpeed.text = windSpeed.toString()

            binding.windDirectionArrow.rotation = (windDegree + 180) % 360
            binding.feelsLike.text = "$fealsLike\u00B0"
            binding.uvValue.text = "$uv"
            binding.sunriseTime.text = "$sunrise"
            binding.contentfeelsLike.text = "$content - Feels like $fealsLike\u00B0"

            val todayForecast = response.forecast?.forecastday?.firstOrNull()
            todayForecast?.let {
                val highTemp = it.day.maxtemp_c
                val lowTemp = it.day.mintemp_c
                binding.tvHighLowTemp.text = "H:${highTemp.toInt()}° L:${lowTemp.toInt()}°"

            }
        }

//        lifecycleScope.launch {
//            val isRain =binding.tvWeather.text.contains("Rain", ignoreCase = true)
//            val isThunder = binding.tvWeather.text.contains("Thunder", ignoreCase = true) || binding.tvWeather.text.contains("Storm", ignoreCase = true)
//
//            when {
//                isRain && isThunder -> {
//                    binding.rainAnimation.visibility = View.VISIBLE
//                    binding.rainAnimation.playAnimation()
//
//                    binding.thunderAnimation.visibility = View.VISIBLE
//                    binding.thunderAnimation.playAnimation()
//                }
//                isThunder -> {
//                    binding.rainAnimation.visibility = View.GONE
//                    binding.rainAnimation.cancelAnimation()
//
//                    binding.thunderAnimation.visibility = View.VISIBLE
//                    binding.thunderAnimation.playAnimation()
//                }
//                isRain -> {
//                    binding.rainAnimation.visibility = View.VISIBLE
//                    binding.rainAnimation.playAnimation()
//
//                    binding.thunderAnimation.visibility = View.GONE
//                    binding.thunderAnimation.cancelAnimation()
//                }
//                else -> {
//                    binding.rainAnimation.visibility = View.GONE
//                    binding.rainAnimation.cancelAnimation()
//
//                    binding.thunderAnimation.visibility = View.GONE
//                    binding.thunderAnimation.cancelAnimation()
//                }
//            }
//        }


      //  viewModel.loadWeather("Ho Chi Minh") // Thay thế bằng vị trí mặc định nếu không có quyền
        Log.d("API_KEY", "WEATHER_API_KEY=${BuildConfig.WEATHER_API_KEY}")


        viewModel.hourlyList.observe(this) { list ->
            val adapterHourlyList = HourlyWeatherAdapter(list)
            binding.recyclerHourly.adapter = adapterHourlyList
        }
        viewModel.dailyForecast.observe(this){ list ->
            val adapterDailyForeCast = ForecastAdapter(list)
            binding.recyclerDailyForecast.adapter = adapterDailyForeCast

        }
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        binding.mapView.setOnClickListener {
            openMapWithCurrentLocation()
        }
        binding.seemore.setOnClickListener{
            openMapWithCurrentLocation()
        }
        binding.icMenuToolbar.setOnClickListener{
            val intent = Intent(this, WeatherListActivity::class.java)
            startActivity(intent)

        }

    }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.apply {
                    uiSettings.isZoomControlsEnabled = true
                    addMarker(MarkerOptions().position(currentLatLng).title("Your Location"))
                    moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                }
            } else {
                Toast.makeText(this, "Không thể lấy vị trí", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openMapWithCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(My Location)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lấy vị trí thất bại", Toast.LENGTH_SHORT).show()
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
                    val latLon = "${location.latitude} , ${location.longitude}"
                    Log.d("WeatherActivity", "Fetched location: $latLon")
                    // Gọi ViewModel để tải thời tiết với vị trí này
                    // viewModel.getCurrentWeather(latLon)
                    viewModel.loadWeather(latLon)
                    Toast.makeText(this, "Location: $latLon", Toast.LENGTH_SHORT).show() // Ví dụ hiển thị
                } else {
                    Log.w("WeatherActivity", "Last location is null.")
                    Toast.makeText(this, "Could not retrieve current location.", Toast.LENGTH_LONG).show()
                    // Cân nhắc: Tải dữ liệu mặc định
                    // viewModel.getCurrentWeather("Hanoi")
                    viewModel.loadWeather("Ho Chi Minh")
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