plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.availelabs.tabla.app.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}
