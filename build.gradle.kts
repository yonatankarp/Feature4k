plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

dependencies {
    kover(project(":feature4k-core"))
    kover(project(":feature4k-dsl"))
    kover(project(":feature4k-web"))
}
