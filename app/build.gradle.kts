plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ir.ayantech.versioncontrolsample"
    compileSdk = 36

    defaultConfig {
        applicationId = "ir.ayantech.versioncontrolsample"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":versioncontrol"))
    implementation(libs.android.http.download.manager)
    implementation(libs.gson)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
}
