plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.foxbrowser.mobile"
    compileSdk = 35
    defaultConfig { applicationId = "com.foxbrowser.mobile"; minSdk = 26; targetSdk = 35; versionCode = 100; versionName = "1.00" }
    buildFeatures { viewBinding = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += setOf("META-INF/DEPENDENCIES", "META-INF/LICENSE*") }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.mozilla.geckoview:geckoview:135.0.20250216192613")
    implementation("com.github.junrar:junrar:7.5.5")
}
