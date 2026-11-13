import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    alias(libs.plugins.tabla.kotlin.conventions)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.dataframe)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ksp)
}

group = "com.availelabs"
version = "0.0.1-SNAPSHOT"
description = "tabla"

dependencies {
    implementation(projects.core)

    // Dependency management (BOMs)
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.spring.ai.bom))
    implementation(platform(libs.spring.modulith.bom))
    kapt(platform(libs.spring.boot.dependencies))
    developmentOnly(platform(libs.spring.boot.dependencies))

    // Spring Boot
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.restclient)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.ai.starter.mcp.server.webmvc)
    implementation(libs.spring.modulith.starter.core)
    implementation(libs.spring.boot.starter.security.oauth2.client)
    implementation(libs.spring.boot.starter.security.oauth2.resource.server)
    runtimeOnly(libs.spring.modulith.runtime)
    kapt(libs.spring.boot.configuration.processor)
    developmentOnly(libs.spring.boot.docker.compose)

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.io.core)
    implementation(libs.kotlinx.io.bytestring)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dataframe)

    // Persistence and database migrations
    implementation(libs.jimmer.spring.boot.starter)
    implementation(libs.spring.boot.starter.flyway)
    ksp(libs.jimmer.ksp)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.database.postgresql)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.junit.platform.launcher)
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
