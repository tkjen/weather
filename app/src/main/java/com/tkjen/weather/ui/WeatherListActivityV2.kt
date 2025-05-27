package com.tkjen.weather.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.tkjen.weather.R
import com.tkjen.weather.databinding.ActivityWeatherListV2Binding

class WeatherListActivityV2 : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherListV2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityWeatherListV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Click on normal search bar
        binding.searchBarNormal.setOnClickListener { activateSearch() }

        // Click cancel button in search bar
        binding.cancelButton.setOnClickListener { deactivateSearch() }

        // Click cancel in search results
        binding.cancelSearch.setOnClickListener { hideSearchResults() }
    }

    private fun activateSearch() {
        // Animate search bar transformation
        binding.searchBarNormal.visibility = View.GONE
        binding.searchBarActive.visibility = View.VISIBLE

        // Focus on EditText and show keyboard
        binding.searchEditText.requestFocus()
        showKeyboard()

        // Animate to search results after short delay
        binding.searchEditText.postDelayed({ showSearchResults() }, 300)
        resetUIState()
    }

    private fun deactivateSearch() {
        // Hide keyboard
        hideKeyboard()

        // Reset search bar
        binding.searchBarActive.visibility = View.GONE
        binding.searchBarNormal.visibility = View.VISIBLE

        // Clear text
        binding.searchEditText.setText("")
    }

    private fun showSearchResults() {
        with(binding) {
            // Slide up animation for search results
            searchResultsLayout.visibility = View.VISIBLE
            searchResultsLayout.translationY = resources.displayMetrics.heightPixels.toFloat()

            val slideUp = ObjectAnimator.ofFloat(searchResultsLayout, "translationY", 0f)
            slideUp.duration = 300
            slideUp.interpolator = android.view.animation.DecelerateInterpolator()
            slideUp.start()

            // Hide main layout
            mainLayout.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mainLayout.visibility = View.GONE
                    }
                })

            // Focus on search input
            searchInput.requestFocus()
            showKeyboard()
        }
    }

    private fun hideSearchResults() {
        // Hide keyboard first
        hideKeyboard()

        with(binding) {
            // Cancel any running animations
            searchResultsLayout.animate().cancel()
            mainLayout.animate().cancel()

            // Slide down animation for search results
            val slideDown = ObjectAnimator.ofFloat(
                searchResultsLayout,
                "translationY",
                resources.displayMetrics.heightPixels.toFloat()
            )
            slideDown.duration = 300
            slideDown.interpolator = android.view.animation.AccelerateInterpolator()
            slideDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    searchResultsLayout.visibility = View.GONE
                    searchResultsLayout.translationY = 0f
                }
            })

            // Show main layout immediately, then animate alpha
            mainLayout.visibility = View.VISIBLE
            mainLayout.alpha = 0f

            // Start both animations
            slideDown.start()
            mainLayout.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(null) // Clear any previous listeners
                .start()

            // Reset search bars
            searchBarActive.visibility = View.GONE
            searchBarNormal.visibility = View.VISIBLE

            // Clear search text
            searchInput.setText("")
            searchEditText.setText("")
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService<InputMethodManager>()
        currentFocus?.let { view ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService<InputMethodManager>()
        // Delay một chút để đảm bảo view đã ready
        binding.searchEditText.postDelayed({
            imm?.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            binding.searchResultsLayout.visibility == View.VISIBLE -> {
                hideSearchResults()
            }
            binding.searchBarActive.visibility == View.VISIBLE -> {
                deactivateSearch()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewBinding tự động cleanup, không cần set null
    }
    private fun resetUIState() {
        with(binding) {
            // Cancel all animations
            searchResultsLayout.animate().cancel()
            mainLayout.animate().cancel()

            // Reset visibility
            searchResultsLayout.visibility = View.GONE
            mainLayout.visibility = View.VISIBLE
            mainLayout.alpha = 1f

            // Reset search bars
            searchBarActive.visibility = View.GONE
            searchBarNormal.visibility = View.VISIBLE

            // Clear text
            searchInput.setText("")
            searchEditText.setText("")

            // Reset translations
            searchResultsLayout.translationY = 0f
        }
    }
}