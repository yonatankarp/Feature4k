import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.diffplug.spotless")
    // Android disabled for now
    // id("com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(
        libs.findVersion("jvm-toolchain").get().requiredVersion.toInt()
    )

    // Android disabled for now
    /*
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    */

    jvm()

    /*
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    */

    linuxX64()
    // macosX64()
    // macosArm64()
    // mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}

// Android disabled for now
/*
android {
    namespace = "org.feature4k.${project.name.replace("-", ".")}"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
*/

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_filename" to "disabled"
            )
        )
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint()
    }
}
