import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val javaVersion = extensions.getByType<VersionCatalogsExtension>()
    .named("libs").findVersion("java").get().requiredVersion.toInt()

fun KotlinCommonCompilerOptions.configureTablaConventions() {
    freeCompilerArgs.addAll(
        "-Xreturn-value-checker=full",
        "-Xconsistent-data-class-copy-visibility",
        "-Xcompanion-blocks-and-extensions",
        "-Xstrict-equality-for-structural-classes",
        "-Xfull-value-classes"
    )
    extraWarnings.set(true)
    progressiveMode.set(true)
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    extensions.configure<KotlinJvmExtension> {
        jvmToolchain(javaVersion)
        compilerOptions.configureTablaConventions()
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        jvmToolchain(javaVersion)
        compilerOptions.configureTablaConventions()
    }
}

pluginManager.withPlugin("com.android.application") {
    extensions.configure<KotlinAndroidExtension> {
        jvmToolchain(javaVersion)
        compilerOptions.configureTablaConventions()
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
