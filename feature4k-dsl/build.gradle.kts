plugins {
    id("feature4k.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature4k-core"))
        }
    }
}