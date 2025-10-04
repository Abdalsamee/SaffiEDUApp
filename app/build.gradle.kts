plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)

    id("com.google.gms.google-services")

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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // BOM يتحكم في الإصدارات
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.animation:animation")
    // ✅ Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ✅ DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ✅ Lifecycle ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.firebase.storage.ktx)
    kapt(libs.hilt.compiler)
    implementation(libs.dagger)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ✅ Coil
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // AndroidX Media3 (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")

    // Image Compression
    implementation("id.zelory:compressor:3.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("androidx.compose.material:material-icons-extended:1.4.3")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.firebase:firebase-messaging:23.4.1")
    implementation ("com.google.firebase:firebase-messaging-ktx:23.4.1")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    // Firebase
    implementation ("com.google.firebase:firebase-firestore-ktx:24.1.0")
    implementation ("com.google.firebase:firebase-storage-ktx:20.0.1")
    implementation ("com.google.firebase:firebase-auth-ktx:21.0.1")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.7.1")

    // Hilt
    implementation ("com.google.dagger:hilt-android:2.40.5")
    kapt ("com.google.dagger:hilt-compiler:2.40.5")


    implementation ("com.google.code.gson:gson:2.10.1") // أو آخر نسخة متاحة

    implementation ("com.google.firebase:firebase-appcheck-debug:17.1.2")
    // أو إذا كنت تستخدم Kotlin:
    implementation ("com.google.firebase:firebase-appcheck-ktx:17.1.2")

    // للتطبيقات الإنتاجية أضف أيضاً:
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:17.1.2")

    implementation ("com.google.accompanist:accompanist-swiperefresh:0.30.1")

}
