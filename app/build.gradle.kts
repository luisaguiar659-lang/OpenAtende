plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "br.com.openatende"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.openatende"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    kotlinOptions { jvmTarget = "17" }
}
