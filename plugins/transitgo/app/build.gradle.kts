plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.marco.smartspacer.transitgo"
    compileSdk = rootProject.extra["compile_sdk"] as Int

    defaultConfig {
        applicationId = "dev.marco.smartspacer.transitgo"
        minSdk = rootProject.extra["min_sdk"] as Int
        targetSdk = 36
        versionCode = 10301
        versionName = "1.3.1"
    }

    signingConfigs {
        System.getenv("SIGNING_KEYSTORE")?.let { keystore ->
            create("release") {
                storeFile = file(keystore)
                storePassword = System.getenv("SIGNING_PASSWORD")
                keyAlias = "smartspacer-plugins"
                keyPassword = System.getenv("SIGNING_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true; buildConfig = true }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":settings-ui"))
    implementation("com.kieronquinn.smartspacer:sdk-plugin:1.1.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.11.0")

    testImplementation("junit:junit:4.13.2")
}
