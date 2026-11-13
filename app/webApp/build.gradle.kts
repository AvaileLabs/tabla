plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.app.shared)
            implementation(libs.ui)
        }
    }
}
