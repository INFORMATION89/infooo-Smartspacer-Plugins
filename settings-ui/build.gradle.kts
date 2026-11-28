plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.infooo.smartspacer.settings"
    compileSdk = 37

    defaultConfig {
        minSdk = 29
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api(platform("androidx.compose:compose-bom:2025.10.00"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.material3:material3:1.5.0-rc01")
    api("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.animation:animation")
    implementation("dev.chrisbanes.haze:haze:1.7.3")
    api("com.kieronquinn.smartspacer:sdk-client:1.2")
}
