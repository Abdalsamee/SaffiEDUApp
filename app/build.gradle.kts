plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.saffieduapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.saffieduapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

// dependencies in app/build.gradle.kts

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // BOM يتحكم في الإصدارات
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // أنت تستخدم Material 3

    // ✅ Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ✅ DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ✅ Lifecycle ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")

<<<<<<< HEAD
    // ✅ Hilt
=======
    // Hilt
>>>>>>> integration&UI
    implementation(libs.hilt.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    kapt(libs.hilt.compiler)
    implementation(libs.dagger)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

<<<<<<< HEAD
    // ✅ Accompanist System UI Controller (لتغيير لون الستاتس بار)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.5-beta")

    // ✅ Testing
=======
    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // Accompanist for status bar
    //implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")



    // Testing
>>>>>>> integration&UI
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
