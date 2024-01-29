plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.application")
}

group = "com.example"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation("androidx.activity:activity-compose:1.8.2")

                runtimeOnly("io.insert-koin:koin-core:3.5.3")// https://mvnrepository.com/artifact/io.insert-koin/koin-core
                implementation("io.insert-koin:koin-core-coroutines:3.5.3")// https://mvnrepository.com/artifact/io.insert-koin/koin-core-coroutines
                implementation("io.insert-koin:koin-androidx-compose:3.5.3")// https://mvnrepository.com/artifact/io.insert-koin/koin-androidx-compose
                implementation("io.insert-koin:koin-android:3.5.3")// https://mvnrepository.com/artifact/io.insert-koin/koin-android

            }
        }
    }
}

android {
    namespace = "com.example.opencv"
    testNamespace = "com.example.testOpencv"

    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.opencv"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources.excludes += "META-INF/native-image/**"
    }
}