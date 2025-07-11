plugins {
    id("com.android.application")
    id("kotlin-android")
    // Temporarily disable complex plugins for testing
    // id("kotlin-kapt")
    // id("dagger.hilt.android.plugin")
    // id("kotlinx-serialization")
}

android {
    namespace = "com.gitissueapp.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gitissueapp.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug") // Use debug signing for now
        }
    }
    
    signingConfigs {
        getByName("debug") {
            // Debug signing (for development)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    // Absolutely minimal - no androidx libraries
    // Testing only
    testImplementation("junit:junit:4.13.2")
}