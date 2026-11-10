pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "tabla"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":server", ":core", ":app:shared", ":app:androidApp", ":app:desktopApp", ":app:webApp")
