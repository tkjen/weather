package com.tkjen.weather

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Các cấu hình khác nếu cần

    }
}