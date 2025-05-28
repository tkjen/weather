package com.tkjen.weather.ui

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tkjen.weather.R
import com.tkjen.weather.databinding.ActivitySearchBinding
import java.io.IOException
import java.util.Locale

class SearchActivity:AppCompatActivity(R.layout.activity_search) {

    private lateinit var binding:ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchInput()
        setupCancelButton()
        setupSuggestions() // Tạm thời giữ chỗ cho việc xử lý gợi ý
    }

    private fun setupSearchInput() {
        binding.searchInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val locationName = binding.searchInput.text.toString().trim()
                if (locationName.isNotEmpty()) {
                    searchLocation(locationName)
                } else {
                    Toast.makeText(this, "Vui lòng nhập tên địa điểm", Toast.LENGTH_SHORT).show()
                }
                true // Xử lý xong sự kiện
            } else {
                false // Chưa xử lý sự kiện
            }
        }
    }

    private fun setupCancelButton() {
        binding.cancelSearch.setOnClickListener {
            finish() // Đóng SearchActivity
        }
    }

    private fun setupSuggestions() {
        // TODO: Implement click listeners for suggestion items (e.g., Home Location)
        // When a suggestion is clicked, you might want to pass the location back to WeatherActivity
        // For example, using setResult() and finish()
    }

    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1) // Tìm tối đa 1 kết quả
            if (addresses != null && addresses.isNotEmpty()) {
                val latitude = addresses[0].latitude
                val longitude = addresses[0].longitude
                
                // Log tọa độ tìm được (có thể xóa sau khi test)
                Log.d("SearchActivity", "Tìm thấy vị trí: $locationName, Lat: $latitude, Lon: $longitude")

                // TODO: Xử lý kết quả tìm kiếm:
                // 1. Nếu bạn muốn hiển thị nhiều kết quả, dùng RecyclerView.
                // 2. Nếu chỉ lấy 1 kết quả và quay về màn hình chính:
                //    - Tạo Intent chứa tọa độ
                //    - Gọi setResult(Activity.RESULT_OK, intent)
                //    - finish()
                // 3. Nếu muốn hiển thị map ngay tại đây, thêm MapView vào layout và hiển thị marker.

                Toast.makeText(this, "Tìm thấy: $locationName", Toast.LENGTH_SHORT).show()

            } else {
                // Không tìm thấy địa điểm
                Toast.makeText(this, "Không tìm thấy địa điểm: $locationName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            // Xảy ra lỗi khi gọi Geocoder (ví dụ: không có kết nối mạng)
            e.printStackTrace()
            Toast.makeText(this, "Lỗi tìm kiếm vị trí: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}