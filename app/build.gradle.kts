plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.blottermanagementsystem"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.blottermanagementsystem"
        minSdk = 26  // Increased from 24 to support Apache POI for Excel export
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Enable code shrinking, obfuscation, and optimization
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Optimize for performance
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
            
            // Additional optimizations for 15 new features
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        
        debug {
            // Keep debug build fast
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    
    // Optimize packaging
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
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
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    
    // Performance optimizations for 60 FPS
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    
    // Optimize APK splits for smaller downloads
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Coil for image loading
    implementation(libs.coil.compose)
    
    // Accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.systemuicontroller)
    
    // Lottie
    implementation(libs.lottie.compose)
    
    // Gson
    implementation(libs.gson)
    
    // Splash Screen
    implementation(libs.androidx.core.splashscreen)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Charts
    implementation(libs.mpandroidchart)
    
    // PDF
    implementation(libs.itext.pdf)
    
    // QR Code
    implementation(libs.zxing.core)
    implementation(libs.zxing.android)
    
    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // Apache POI for Excel export (requires minSdk 26)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    
    // Calendar View
    implementation("com.kizitonwose.calendar:compose:2.5.0")
    
    // Photo Editing/Annotation
    implementation("io.github.mr0xf00:easycrop:0.1.1")
    
    // WorkManager (Offline Mode & Sync)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Speech Recognition (Voice-to-Text)
    // Already included in Android SDK
    
    // Firebase Cloud Messaging (FCM) & Storage
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-storage-ktx") // ✅ Audio upload to cloud
    
    // Cloudinary for image upload
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}