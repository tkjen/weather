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

        // --- BẮT ĐẦU THÊM GỠ LỖI ---
        println(">>> [BUILD.GRADLE] Bắt đầu xử lý API Key...")
        val apiKeyFromProperty: String? = if (project.hasProperty("weatherApiKey")) {
            project.property("weatherApiKey") as? String
        } else {
            null
        }

        val finalApiKey: String
        if (apiKeyFromProperty != null && apiKeyFromProperty.isNotBlank()) {
            finalApiKey = apiKeyFromProperty
            println(">>> [BUILD.GRADLE] Đã tìm thấy project property 'weatherApiKey'. Giá trị: '$finalApiKey'")
        } else {
            finalApiKey = "default_gradle_key" // Thay đổi giá trị mặc định này để phân biệt
            println(">>> [BUILD.GRADLE] KHÔNG tìm thấy project property 'weatherApiKey' hoặc giá trị rỗng. Sử dụng giá trị mặc định: '$finalApiKey'")
        }
        buildConfigField("String", "WEATHER_API_KEY", "\"$finalApiKey\"")
        println(">>> [BUILD.GRADLE] Đã đặt buildConfigField WEATHER_API_KEY là: '$finalApiKey'")

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
}