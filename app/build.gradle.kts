plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val signingStorePath = System.getenv("OPENATENDE_KEYSTORE_PATH")
val signingStorePassword = System.getenv("OPENATENDE_STORE_PASSWORD")
val signingKeyAlias = System.getenv("OPENATENDE_KEY_ALIAS")
val signingKeyPassword = System.getenv("OPENATENDE_KEY_PASSWORD")
val hasOpenAtendeSigning = listOf(
    signingStorePath,
    signingStorePassword,
    signingKeyAlias,
    signingKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "br.com.openatende"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.openatende"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }

    if (hasOpenAtendeSigning) {
        signingConfigs {
            create("openAtendeRelease") {
                storeFile = file(signingStorePath!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (hasOpenAtendeSigning) {
                signingConfig = signingConfigs.getByName("openAtendeRelease")
            }
        }
        getByName("release") {
            if (hasOpenAtendeSigning) {
                signingConfig = signingConfigs.getByName("openAtendeRelease")
            }
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
