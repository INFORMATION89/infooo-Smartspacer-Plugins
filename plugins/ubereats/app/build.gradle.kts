plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.marco.smartspacer.ubereats"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.marco.smartspacer.ubereats"
        minSdk = 29
        targetSdk = 36
        versionCode = 101
        versionName = "0.1.1"
    }

    // The permanent release key (shared with the other Smartspacer plugins), given by the release
    // workflow from GitHub secrets: SIGNING_KEYSTORE is the key file's path, SIGNING_PASSWORD its
    // password. Without them (local and CI test builds), release builds use the debug key.
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
            // Shrinking strips the unused Material icons (the settings use 6 of thousands)
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

    // buildConfig: BuildConfig.VERSION_NAME, shown in the Source code button
    buildFeatures { compose = true; buildConfig = true }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(libs.smartspacerSdkPlugin)
    implementation(libs.androidxCoreKtx)

    // Settings screen: Compose + Material 3 Expressive
    implementation(platform(libs.composeBom))
    implementation(libs.composeMaterial3)
    implementation(libs.composeMaterialIconsExtended)
    // Blurred, translucent header over the list (backdrop blur on Android 12+, a tint before)
    implementation(libs.haze)
    implementation(libs.activityCompose)

    testImplementation(libs.junit)
}
