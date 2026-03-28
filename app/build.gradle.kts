plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "com.willbicks.notificationinspector"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.willbicks.notificationinspector"
    minSdk = 30
    targetSdk = 34
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  // Core Android
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.google.android.material:material:1.13.0")
  implementation("androidx.activity:activity-compose:1.8.2")

  // Compose BOM - manages versions for all Compose libraries
  val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
  implementation(composeBom)

  // Compose UI
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")

  // Lifecycle ViewModel with Compose
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

  // Debug tooling
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
