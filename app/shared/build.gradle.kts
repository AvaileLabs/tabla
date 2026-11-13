plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm()
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    wasmJs {
        browser()
    }
    android {
        namespace = "com.availelabs.tabla.app.shared"
        compileSdk {
            version = release(libs.versions.android.compile.sdk.asProvider().get().toInt()) {
                minorApiLevel = libs.versions.android.compile.sdk.minor.get().toInt()
            }
        }
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(libs.runtime)
            implementation(libs.ui)
            implementation(libs.foundation)

            implementation(libs.kotlinx.collections.immutable)
        }
    }
}
