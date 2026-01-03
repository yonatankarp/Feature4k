plugins {
    alias(libs.plugins.kotlin.jvm)
    id("feature4k.coverage")
    id("feature4k.documentation")
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