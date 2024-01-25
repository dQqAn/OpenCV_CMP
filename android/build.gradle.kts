plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.8.0")

    runtimeOnly("io.insert-koin:koin-core:3.4.2")// https://mvnrepository.com/artifact/io.insert-koin/koin-core
    implementation("io.insert-koin:koin-core-coroutines:3.4.1")// https://mvnrepository.com/artifact/io.insert-koin/koin-core-coroutines
    implementation("io.insert-koin:koin-androidx-compose:3.4.5")// https://mvnrepository.com/artifact/io.insert-koin/koin-androidx-compose
    implementation("io.insert-koin:koin-android:3.4.2")// https://mvnrepository.com/artifact/io.insert-koin/koin-android
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

    packagingOptions {
        resources.excludes += "META-INF/native-image/**"
    }
}