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
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tkjen.weather.R
import com.tkjen.weather.data.model.HourWeather
import com.tkjen.weather.data.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.*
import com.tkjen.weather.databinding.ActivityWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.tkjen.weather.utils.NetworkHelper
import android.widget.FrameLayout


@AndroidEntryPoint
class WeatherActivity : AppCompatActivity(R.layout.activity_weather), OnMapReadyCallback {

    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        checkAndRequestLocationPermissions()
        setupObservers()
        setupMapView(savedInstanceState)

    }



    private fun setupUI() {
        enableEdgeToEdge()
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Thiết lập RecyclerView
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        // Thiết lập RecyclerView cho thời tiết theo giờ
        binding.recyclerHourly.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        // Thiết lập RecyclerView cho dự báo theo ngày
        binding.recyclerDailyForecast.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observer cho dữ liệu thời tiết
        viewModel.weather.observe(this) { response ->
            updateWeatherUI(response)
        }

        // Observer cho danh sách thời tiết theo giờ
        viewModel.hourlyList.observe(this) { hourlyList ->
            (binding.recyclerHourly.adapter as? HourlyWeatherAdapter)?.updateData(hourlyList)
                ?: run {
                    binding.recyclerHourly.adapter = HourlyWeatherAdapter(hourlyList)
                }
        }

        // Observer cho dự báo theo ngày
        viewModel.dailyForecast.observe(this) { dailyList ->
            (binding.recyclerDailyForecast.adapter as? ForecastAdapter)?.updateData(dailyList)
                ?: run {
                    binding.recyclerDailyForecast.adapter = ForecastAdapter(dailyList)
                }
        }

        // Observer cho thông báo lỗi
        viewModel.errorMessage.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        // Observer cho trạng thái mạng
        viewModel.isOffline.observe(this) { isOffline ->
            binding.lnWifiOff.apply {
                visibility = if (isOffline) View.VISIBLE else View.INVISIBLE
               binding.tvLastUpdated.text = "Last update: ${getCurrentTime()}"
            }
        }

    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun updateWeatherUI(response: WeatherResponse) {
        binding.apply {
            tvLocation.text = response.location.name
            locationName.text = response.location.name
            tvTemperatureValue.text = "${response.current.temp_c}°"
            tvWeather.text = response.current.condition.text
            locationTemperature.text = " ${response.current.temp_c}"
        }

        updateDetailedWeatherInfo(response)
        updateForecastInfo(response)
    }

    private fun updateDetailedWeatherInfo(response: WeatherResponse) {
        binding.apply {
            val currentHourData = getCurrentHourData(response)
            currentHourData?.let {
                contenthumidity.text = "Current dew point: ${it.dewpoint_c} °C"
            }

            val uv = response.current.uv
            val progressPercent = ((uv / 11f) * 100).toInt().coerceIn(0, 100)
            uvProgress.progress = progressPercent
            uvLevel.text = viewModel.getUIlevel(uv)
            uvValue.text = "$uv"

            windSpeed.text = response.current.wind_kph.toString()
            windDirectionArrow.rotation = (response.current.wind_degree + 180).toFloat() % 360

            humidityValue.text = "${response.current.humidity}%"
            feelsLike.text = "${response.current.feelslike_c}°"
            contentfeelsLike.text = "${response.current.condition.text} - Feels like ${response.current.feelslike_c}°"
        }
    }

    private fun updateForecastInfo(response: WeatherResponse) {
        val todayForecast = response.forecast?.forecastday?.firstOrNull()
        todayForecast?.let {
            binding.apply {
                tvHighLowTemp.text = "H:${it.day.maxtemp_c.toInt()}° L:${it.day.mintemp_c.toInt()}°"
                
                sunsetTime.text = "Sunset: ${it.astro.sunset}"
                sunriseTime.text = it.astro.sunrise
                
                currentRainfall.text = "${response.current.precip_mm} mm"
                expectedRainfall.text = "${it.day.totalprecip_mm} mm expected in next 24h."

                // Update sun position
                updateSunPosition(it.astro.sunrise, it.astro.sunset)
            }
        }
    }

    private fun updateSunPosition(sunrise: String, sunset: String) {
        try {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sunriseTime = formatter.parse(sunrise)
            val sunsetTime = formatter.parse(sunset)
            val currentTime = Calendar.getInstance().time

            // Calculate total day duration in milliseconds
            val totalDayDuration = sunsetTime.time - sunriseTime.time
            
            // Calculate current position relative to sunrise
            val currentPosition = currentTime.time - sunriseTime.time
            
            // Calculate percentage of day progress (0.0 to 1.0)
            val progress = (currentPosition.toFloat() / totalDayDuration.toFloat()).coerceIn(0f, 1f)
            
            // Get the FrameLayout width to calculate margin
            binding.sunPosition.post {
                val parentFrame = binding.sunPosition.parent as? FrameLayout
                parentFrame?.let { frame ->
                    val frameWidth = frame.width
                    val sunSize = binding.sunPosition.width
                    
                    // Calculate margin based on progress (0 to frameWidth - sunSize)
                    val margin = (progress * (frameWidth - sunSize)).toInt()
                    
                    // Update sun position
                    val params = binding.sunPosition.layoutParams as FrameLayout.LayoutParams
                    params.marginStart = margin
                    binding.sunPosition.layoutParams = params
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherActivity", "Error updating sun position", e)
        }
    }

    private fun getCurrentHourData(response: WeatherResponse): HourWeather? {
        val hours = response.forecast?.forecastday?.firstOrNull()?.hour ?: return null
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentEpochMillis = System.currentTimeMillis()

        return hours.minByOrNull { hourItem ->
            val date = sdf.parse(hourItem.time)
            val epochMillis = date?.time ?: 0L
            kotlin.math.abs(epochMillis - currentEpochMillis)
        }
    }

    private fun setupMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
        // Thêm click listeners
        binding.mapView.setOnClickListener { openMapWithCurrentLocation() }
        binding.seemore.setOnClickListener { openMapWithCurrentLocation() }
        
        // Thêm lifecycle callbacks cho MapView
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.apply {
                    // Cấu hình map
                    uiSettings.apply {
                        isZoomControlsEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isCompassEnabled = true
                        isMyLocationButtonEnabled = true
                        isMapToolbarEnabled = true
                    }
                    
                    // Thêm marker với animation
                    addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Your Location")
                            .snippet("Current temperature: ${binding.locationTemperature.text}")
                    )?.showInfoWindow()

                    // Di chuyển camera với animation
                    animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f),
                        1000,
                        null
                    )

                    // Thêm style cho map
                    // setMapStyle(
                    //     MapStyleOptions.loadRawResourceStyle(
                    //         this@WeatherActivity,
                    //         R.raw.map_style
                    //     )
                    // )
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
                }
            }
        }

    private fun checkAndRequestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("WeatherActivity", "Location permission already granted")
                fetchLastLocationAndLoadWeather()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Log.d("WeatherActivity", "Showing permission rationale")
                showPermissionRationaleDialog()
            }
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
    @SuppressLint("MissingPermission")
    private fun fetchLastLocationAndLoadWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("WeatherActivity", "Attempted to fetch location without permission (should not happen).")
            checkAndRequestLocationPermissions()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLon = "${location.latitude} , ${location.longitude}"
                    Log.d("WeatherActivity", "Fetched location: $latLon")
                    binding.icMenuToolbar.setOnClickListener{
                        val location = LatLng(location.latitude, location.longitude)
                        val intent = Intent(this, WeatherListActivity::class.java).apply {
                            putExtra("lat", location.latitude)
                            putExtra("lon", location.longitude)
                        }
                        startActivity(intent)
                    }
                    viewModel.loadWeather(latLon)
                    Toast.makeText(this, "Location: $latLon", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("WeatherActivity", "Last location is null.")
                    Toast.makeText(this, "Could not retrieve current location.", Toast.LENGTH_LONG).show()
                    viewModel.loadWeather("Ho Chi Minh")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WeatherActivity", "Failed to get location", e)
                Toast.makeText(this, "Failed to get location.", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Permission rationale cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Permission Denied")
            .setMessage("Location permission was denied. To use this feature, please enable it in the app settings or allow location access to see local weather.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission denied dialog cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}