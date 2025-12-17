plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.coffeeshop"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.coffeeshop"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        // SỬA: Nâng cấp phiên bản Java lên 17 để tương thích tốt hơn
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // SỬA: JvmTarget cũng cần được nâng cấp
        jvmTarget = "17"
    }
    // THÊM: Bật tính năng View Binding để thay thế findViewById
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Glide for image loading
    // SỬA: Dùng cú pháp Kotlin DSL với dấu ngoặc kép
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // SỬA: Dùng "kapt" thay vì "annotationProcessor"
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // UI Components
    // SỬA: Dùng cú pháp Kotlin DSL với dấu ngoặc kép
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // THÊM: Firebase dependencies
    // Import Firebase BOM để quản lý versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)   // Cloud Messaging
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
}