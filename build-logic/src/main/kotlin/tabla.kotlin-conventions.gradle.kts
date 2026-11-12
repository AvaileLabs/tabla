import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

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
        compilerOptions.configureTablaConventions()
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        compilerOptions.configureTablaConventions()
    }
}

pluginManager.withPlugin("com.android.application") {
    extensions.configure<KotlinAndroidExtension> {
        compilerOptions.configureTablaConventions()
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
