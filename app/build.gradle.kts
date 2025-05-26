import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.tkjen.weather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tkjen.weather"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""

        println(">>> [BUILD.GRADLE] Bắt đầu xử lý API Key...")

    // Ưu tiên 1: Project property truyền từ command line hoặc CI/CD
        var apiKey: String? = if (project.hasProperty("weatherApiKey")) {
            project.property("weatherApiKey") as? String
        } else {
            null
        }

        if (!apiKey.isNullOrBlank()) {
            println(">>> [BUILD.GRADLE] Đã tìm thấy project property 'weatherApiKey'. Giá trị: '$apiKey'")
        } else {
            println(">>> [BUILD.GRADLE] KHÔNG tìm thấy project property. Thử đọc từ local.properties...")

            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                val props = Properties()
                props.load(localPropertiesFile.inputStream())
                apiKey = props.getProperty("weatherApiKey")

                if (!apiKey.isNullOrBlank()) {
                    println(">>> [BUILD.GRADLE] Đã tìm thấy 'weatherApiKey' trong local.properties. Giá trị: '$apiKey'")
                } else {
                    println(">>> [BUILD.GRADLE] Không tìm thấy hoặc rỗng trong local.properties.")
                }
            } else {
                println(">>> [BUILD.GRADLE] File local.properties không tồn tại.")
            }
        }

// Ưu tiên 3: fallback giá trị mặc định
        if (apiKey.isNullOrBlank()) {
            apiKey = "default_gradle_key"
            println(">>> [BUILD.GRADLE] Sử dụng API key mặc định: '$apiKey'")
        }

// Gán vào BuildConfig
        buildConfigField("String", "WEATHER_API_KEY", "\"$apiKey\"")
        println(">>> [BUILD.GRADLE] Đã đặt buildConfigField WEATHER_API_KEY là: '$apiKey'")



    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }



}
val lifecycle_version = "2.9.0"
val arch_version = "2.2.0"
val fragment_version = "1.8.6"
val room_version = "2.7.1"
val hilt_version = "2.51.1" // Sửa phiên bản
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    //fragment
    implementation("androidx.fragment:fragment-ktx:$fragment_version")

    // Hilt (Dùng KSP)
    implementation("com.google.dagger:hilt-android:$hilt_version")
    ksp("com.google.dagger:hilt-android-compiler:$hilt_version")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.google.maps.android:android-maps-utils:3.10.0")
//
    implementation ("com.airbnb.android:lottie:6.4.0")
}