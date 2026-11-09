plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.bhumaji.wordbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bhumaji.wordbuddy"
        minSdk = 26          // Android 8.0+
        targetSdk = 34
        // Set by the release workflow from the git tag (v3.1.0 -> 3.1.0 / 30100).
        versionCode = (project.findProperty("appVersionCode") as String?)?.toInt() ?: 30000
        versionName = (project.findProperty("appVersionName") as String?) ?: "3.0.0-dev"
    }

    // One permanent key (from GitHub secrets) so every new version installs as an
    // update over the old one and keeps her stars and photos.
    val keystorePath: String? = System.getenv("SIGNING_KEYSTORE_PATH")
    signingConfigs {
        create("release") {
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("SIGNING_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "wordbuddy"
                keyPassword = System.getenv("SIGNING_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (keystorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    testImplementation("junit:junit:4.13.2")
}
