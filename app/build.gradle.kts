plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mad"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.mad"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Add the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Add the Firebase Analytics dependency
    implementation("com.google.firebase:firebase-analytics")

    // Add the Firebase Authentication dependency
    implementation("com.google.firebase:firebase-auth")

    // Add the Cloud Firestore dependency
    implementation("com.google.firebase:firebase-firestore")
}