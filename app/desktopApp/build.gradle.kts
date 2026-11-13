plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.ui)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.availelabs.tabla.app.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}
