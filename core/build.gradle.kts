plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    wasmJs {
        browser()
    }
    android {
        namespace = "com.availelabs.tabla.core"
        compileSdk {
            version = release(libs.versions.android.compile.sdk.asProvider().get().toInt()) {
                minorApiLevel = libs.versions.android.compile.sdk.minor.get().toInt()
            }
        }
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
}
