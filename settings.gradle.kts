pluginManagement {
    includeBuild("build-logic")

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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "tabla"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":server", ":core", ":app:shared", ":app:androidApp", ":app:desktopApp", ":app:webApp")
