import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.dataframe)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.ksp)
}

group = "com.availelabs"
version = "0.0.1-SNAPSHOT"
description = "tabla"

dependencies {
    implementation(projects.core)

    // Dependency management (BOMs)
    implementation(platform(libs.springBoot.bom))
    implementation(platform(libs.springAi.bom))
    implementation(platform(libs.springModulith.bom))
    kapt(platform(libs.springBoot.bom))
    developmentOnly(platform(libs.springBoot.bom))

    // Spring Boot
    implementation(libs.springBoot.starter)
    implementation(libs.springBoot.starterRestClient)
    implementation(libs.springBoot.starterActuator)
    implementation(libs.springAi.starterMcpServerWebmvc)
    implementation(libs.springModulith.starterCore)
    runtimeOnly(libs.springModulith.runtime)
    kapt(libs.springBoot.configurationProcessor)
    developmentOnly(libs.springBoot.dockerCompose)

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinx.io.bytestring)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.dataframe)

    // Persistence and database migrations
    implementation(libs.jimmer.springBootStarter)
    implementation(libs.springBoot.starterFlyway)
    ksp(libs.jimmer.ksp)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.postgresql)

    // Testing
    testImplementation(libs.springBoot.starterTest)
    testImplementation(libs.kotlin.testJunit5)
    testImplementation(libs.springModulith.starterTest)
    testImplementation(libs.springBoot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.junit.platformLauncher)
}

tasks.matching { it.name == "kspKotlin" }.configureEach {
    inputs.files(fileTree("src/main/dto") { include("**/*.dto") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.named<BootRun>("bootRun") {
    workingDir = layout.projectDirectory.dir("..").asFile
}
