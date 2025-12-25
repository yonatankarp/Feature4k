plugins {
    id("feature4k.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            implementation(project(":feature4k-core"))
        }
    }
}
