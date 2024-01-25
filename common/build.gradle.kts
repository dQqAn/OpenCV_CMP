plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
//    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

group = "com.example"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget()
    jvm("desktop") {
        jvmToolchain(17)
    }
//    ios()
//    iosSimulatorArm64()
    /*listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "common"
            isStatic = true
        }
        *//*iosTarget.compilations {
            val main by getting {
                cinterops {
                    create("sha256") {
                        header(file("native/sha256/sha256.h"))
                    }
                }
            }
        }*//*
    }*/
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.ui)
                api(compose.animation)
                api(compose.material3) // api(compose.material)
                api(compose.materialIconsExtended)

                // https://mvnrepository.com/artifact/org.bytedeco/javacv
//                implementation("org.bytedeco:javacv:1.4.4")
                implementation("org.bytedeco:javacv:1.5.9")
                // https://mvnrepository.com/artifact/org.bytedeco/javacpp
                implementation("org.bytedeco:javacpp:1.5.9")

                runtimeOnly("io.insert-koin:koin-core:3.4.2")// https://mvnrepository.com/artifact/io.insert-koin/koin-core
                implementation("io.insert-koin:koin-core-coroutines:3.4.1")// https://mvnrepository.com/artifact/io.insert-koin/koin-core-coroutines

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) implementation(compose.components.resources)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")//https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.7.3") // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-rx3

            }
        }
        val androidMain by getting {
            dependencies {
                // https://mvnrepository.com/artifact/androidx.activity/activity-compose
                implementation("androidx.activity:activity-compose:1.7.2")

                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")

                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")// https://mvnrepository.com/artifact/androidx.lifecycle/lifecycle-viewmodel-compose

                // https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
//                implementation("io.reactivex.rxjava3:rxjava:3.1.8")

                implementation("com.google.accompanist:accompanist-permissions:0.33.2-alpha")

                val cameraxVersion = "1.3.0"
                implementation("androidx.camera:camera-core:${cameraxVersion}")
                implementation("androidx.camera:camera-camera2:${cameraxVersion}")
                implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
                implementation("androidx.camera:camera-video:${cameraxVersion}")
                implementation("androidx.camera:camera-view:${cameraxVersion}")
                implementation("androidx.camera:camera-extensions:${cameraxVersion}")

                // https://mvnrepository.com/artifact/org.bytedeco/javacpp-platform
//                implementation("org.bytedeco:javacpp-platform:1.5.9")
//                implementation("org.bytedeco:javacpp-platform:1.5.9:android-arm64")
//                implementation("org.bytedeco:javacpp-platform:1.5.9:android-arm")
//                implementation("org.bytedeco:javacpp-platform:1.5.9:android-x86")
//                implementation("org.bytedeco:javacpp-platform:1.5.9:android-x86_64")

                // https://mvnrepository.com/artifact/org.bytedeco/javacv-platform
//                implementation("org.bytedeco:javacv-platform:1.5.9")

//                implementation("org.bytedeco:javacpp:1.5.9:android-arm64")
//                implementation("org.bytedeco:javacpp:1.5.9:android-arm")
//                implementation("org.bytedeco:javacpp:1.5.9:android-x86")
//                implementation("org.bytedeco:javacpp:1.5.9:android-x86_64")

//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:android-arm64")
//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:android-arm")
//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:android-x86")
//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:android-x86_64")

                // https://mvnrepository.com/artifact/org.bytedeco/opencv
                implementation("org.bytedeco:opencv:4.7.0-1.5.9:android-x86")
                implementation("org.bytedeco:opencv:4.7.0-1.5.9:android-x86_64")
                implementation("org.bytedeco:opencv:4.7.0-1.5.9:android-arm64")
                implementation("org.bytedeco:opencv:4.7.0-1.5.9:android-arm")

                // https://mvnrepository.com/artifact/org.bytedeco/openblas
//                implementation("org.bytedeco:openblas:0.3.23-1.5.9")
                implementation("org.bytedeco:openblas:0.3.23-1.5.9:android-x86")
                implementation("org.bytedeco:openblas:0.3.23-1.5.9:android-x86_64")
                implementation("org.bytedeco:openblas:0.3.23-1.5.9:android-arm64")
                implementation("org.bytedeco:openblas:0.3.23-1.5.9:android-arm")

                // https://mvnrepository.com/artifact/org.pytorch/pytorch_android
                implementation("org.pytorch:pytorch_android:2.1.0")
                // https://mvnrepository.com/artifact/org.pytorch/pytorch_android_torchvision
                implementation("org.pytorch:pytorch_android_torchvision:2.1.0")
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)

//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:windows-x86")
//                implementation("org.bytedeco.javacpp-presets:opencv:4.0.1-1.4.4:windows-x86_64")

                implementation("org.bytedeco:opencv:4.7.0-1.5.9:windows-x86")
                implementation("org.bytedeco:opencv:4.7.0-1.5.9:windows-x86_64")

                implementation("org.bytedeco:openblas:0.3.23-1.5.9:windows-x86")
                implementation("org.bytedeco:openblas:0.3.23-1.5.9:windows-x86_64")
            }
        }
        /*val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }*/
    }

    /*cocoapods {
        summary = "Shared code for the sample"
        homepage = "https://github.com/JetBrains/compose-jb"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "common"
            isStatic = true
        }
    }*/
}

android {
    namespace = "com.example.common.src"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}