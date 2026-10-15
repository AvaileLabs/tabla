plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.dataframe)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.ksp)
}

group = "com.availelabs"
version = "0.0.1-SNAPSHOT"
description = "tabla"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(27)
    }
}

dependencies {
    implementation(platform(libs.springBoot.bom))
    implementation(platform(libs.springAi.bom))
    implementation(platform(libs.springModulith.bom))
    kapt(platform(libs.springBoot.bom))

    implementation(libs.springBoot.starter)
    implementation(libs.springBoot.starterRestClient)
    implementation(libs.springAi.starterMcpServerWebmvc)
    implementation(libs.kotlin.reflect)
    implementation(libs.springModulith.starterCore)
    runtimeOnly(libs.springModulith.runtime)
    implementation(libs.kotlinx.dataframe)
    implementation(libs.kotlinx.datetime)
    implementation(libs.jimmer.springBootStarter)
    ksp(libs.jimmer.ksp)
    runtimeOnly(libs.postgresql)
    implementation(libs.springBoot.starterFlyway)
    runtimeOnly(libs.flyway.postgresql)
    kapt(libs.springBoot.configurationProcessor)

    testImplementation(libs.springBoot.starterTest)
    testImplementation(libs.kotlin.testJunit5)
    testImplementation(libs.springModulith.starterTest)
    developmentOnly(libs.springBoot.dockerCompose)
    testRuntimeOnly(libs.junit.platformLauncher)
    testImplementation(libs.springBoot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.matching { it.name == "kspKotlin" }.configureEach {
    inputs.files(fileTree("src/main/dto") { include("**/*.dto") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
