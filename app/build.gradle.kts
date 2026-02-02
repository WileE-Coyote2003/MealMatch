plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mealmatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mealmatch"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }


    buildFeatures { viewBinding = true }
}

dependencies {
    // ✅ Compatible with compileSdk 34 + AGP 8.1.2
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    implementation("androidx.activity:activity-ktx:1.8.2")

    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// ✅ Prevent Gradle from silently upgrading to 1.12.x / 1.16.x
configurations.all {
    resolutionStrategy {
        force("androidx.activity:activity-ktx:1.8.2")
        force("androidx.activity:activity:1.8.2")
        force("androidx.core:core-ktx:1.12.0")
        force("androidx.core:core:1.12.0")
    }
}
