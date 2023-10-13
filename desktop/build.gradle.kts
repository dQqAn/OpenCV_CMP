import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.example"
version = "1.0-SNAPSHOT"


kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)

                runtimeOnly("io.insert-koin:koin-core:3.4.2")// https://mvnrepository.com/artifact/io.insert-koin/koin-core
                implementation("io.insert-koin:koin-core-coroutines:3.4.1")// https://mvnrepository.com/artifact/io.insert-koin/koin-core-coroutines
                implementation("io.insert-koin:koin-compose-jvm:1.0.4")// https://mvnrepository.com/artifact/io.insert-koin/koin-compose-jvm
                implementation("io.insert-koin:koin-compose:1.0.4")// https://mvnrepository.com/artifact/io.insert-koin/koin-compose
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.common.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OpenCV"
            packageVersion = "1.0.0"
        }
    }
}
