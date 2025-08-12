plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.cicd"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cicd"
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.dash)
    implementation(libs.facebook.shimmer)

    // for notification that makes suitable notification for play music features
    implementation(libs.media)
    
//    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
//    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
//    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'
}