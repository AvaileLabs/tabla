plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.dataframe)
    alias(libs.plugins.springBoot)
}

group = "com.availelabs"
version = "0.0.1-SNAPSHOT"
description = "tabla"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

dependencies {
    implementation(platform(libs.springBoot.bom))
    implementation(platform(libs.springAi.bom))
    implementation(platform(libs.springModulith.bom))
    implementation(platform(libs.kotlin.bom))

    implementation(libs.springBoot.starter)
    implementation(libs.springBoot.starterRestClient)
    implementation(libs.springAi.starterMcpServerWebmvc)
    implementation(libs.kotlin.reflect)
    implementation(libs.springModulith.starterCore)
    implementation(libs.kotlinx.dataframe)
    implementation(libs.kotlinx.datetime)


    testImplementation(libs.springBoot.starterTest)
    testImplementation(libs.kotlin.testJunit5)
    testImplementation(libs.springModulith.starterTest)
    developmentOnly(libs.springBoot.dockerCompose)
    testRuntimeOnly(libs.junit.platformLauncher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
