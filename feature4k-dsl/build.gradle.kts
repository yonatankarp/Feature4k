plugins {
    id("feature4k.multiplatform")
    id("feature4k.publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature4k-core"))
        }
    }
}