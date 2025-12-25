plugins {
    id("feature4k.multiplatform")
    id("feature4k.publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }

        jvmMain.dependencies {
            implementation(libs.slf4j.api)
        }

        commonTest.dependencies {
            implementation(project(":feature4k-test"))
        }
    }
}