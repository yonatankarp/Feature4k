plugins {
    id("feature4k.multiplatform")
    id("feature4k.documentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            implementation(project(":feature4k-core"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}
