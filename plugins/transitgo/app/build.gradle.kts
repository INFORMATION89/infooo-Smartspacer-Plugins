plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.marco.smartspacer.transitgo"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.marco.smartspacer.transitgo"
        minSdk = 29
        targetSdk = 36
        versionCode = 10301
        versionName = "1.3.1"
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
    implementation("com.kieronquinn.smartspacer:sdk-plugin:1.1.2")
    implementation("androidx.core:core-ktx:1.13.1")

    // Settings screen: Compose + Material 3 Expressive
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.compose.material3:material3:1.5.0-rc01")
    implementation("androidx.compose.material:material-icons-extended")
    // Blurred, translucent header over the list (backdrop blur on Android 12+, a tint before)
    implementation("dev.chrisbanes.haze:haze:1.7.3")
    implementation("androidx.activity:activity-compose:1.11.0")

    testImplementation("junit:junit:4.13.2")
}
