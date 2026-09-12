plugins {
    kotlin("jvm") version "2.4.20"
}

group = "com.availelabs"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(26)
}

tasks.test {
    useJUnitPlatform()
}