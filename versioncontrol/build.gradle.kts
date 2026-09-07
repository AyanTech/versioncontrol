plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "com.github.ayantech"
version = "0.6.3"

android {
    namespace = "ir.ayantech.versioncontrol"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.ayantech"
            artifactId = "versioncontrol"
            version = "0.6.3"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    compileOnly(libs.gson)
    compileOnly(libs.retrofit.converter.gson)
    compileOnly(libs.okhttp)
    compileOnly(libs.android.http.download.manager)
}
