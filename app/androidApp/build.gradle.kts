plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.availelabs.tabla.app"
    compileSdk {
        version = release(libs.versions.android.compile.sdk.asProvider().get().toInt()) {
            minorApiLevel = libs.versions.android.compile.sdk.minor.get().toInt()
        }
    }

    defaultConfig {
        applicationId = "com.availelabs.tabla"
        minSdk = libs.versions.android.min.sdk.get().toInt()
        targetSdk = libs.versions.android.target.sdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.activity.compose)
}
