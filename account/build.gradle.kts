import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
    id("maven-publish")
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-shared-account/")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    group = "com.kape.android"
    version = "1.6.1"

    jvmToolchain(17)

    // Android
    android {
        namespace = "com.kape.account"

        compileSdk = 37
        minSdk = 21

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        withHostTestBuilder {}.configure {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:3.5.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
        }
        androidMain.dependencies {
            implementation("androidx.security:security-crypto:1.1.0")
            implementation("com.madgag.spongycastle:core:1.58.0.0")
            implementation("io.ktor:ktor-client-okhttp:3.5.2")
        }
    }
}
