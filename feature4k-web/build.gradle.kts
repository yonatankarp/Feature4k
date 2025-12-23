plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}

dependencies {
    implementation(project(":feature4k-core"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.html.builder)
    testImplementation(libs.kotlin.test)
}